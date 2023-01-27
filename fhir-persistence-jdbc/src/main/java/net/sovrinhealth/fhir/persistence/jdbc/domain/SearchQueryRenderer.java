/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.domain;

import static net.sovrinhealth.fhir.config.FHIRConfiguration.PROPERTY_SEARCH_ENABLE_LEGACY_WHOLE_SYSTEM_SEARCH_PARAMS;
import static net.sovrinhealth.fhir.database.utils.query.expression.ExpressionSupport.alias;
import static net.sovrinhealth.fhir.database.utils.query.expression.ExpressionSupport.bind;
import static net.sovrinhealth.fhir.database.utils.query.expression.ExpressionSupport.col;
import static net.sovrinhealth.fhir.database.utils.query.expression.ExpressionSupport.on;
import static net.sovrinhealth.fhir.database.utils.query.expression.ExpressionSupport.string;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.CODE_SYSTEM_ID;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.COMMON_TOKEN_VALUE_ID;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.DATE_START;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.DESCENDING;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.EQ;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.ESCAPE_PERCENT;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.ESCAPE_UNDERSCORE;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.IS_DELETED;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.LEFT_PAREN;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.LOGICAL_ID;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.MAX;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.MIN;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.NUMBER_VALUE;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.PARAMETER_NAME_ID;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.PERCENT_WILDCARD;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.QUANTITY_VALUE;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.REF_LOGICAL_RESOURCE_ID;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.REF_VALUE;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.RESOURCE_TYPE_ID;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.RIGHT_PAREN;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.TOKEN_VALUE;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.UNDERSCORE_WILDCARD;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants._LOGICAL_RESOURCES;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants._RESOURCES;
import static net.sovrinhealth.fhir.search.SearchConstants.CANONICAL_COMPONENT_URI;
import static net.sovrinhealth.fhir.search.SearchConstants.CANONICAL_COMPONENT_VERSION;
import static net.sovrinhealth.fhir.search.SearchConstants.CANONICAL_SUFFIX;
import static net.sovrinhealth.fhir.search.SearchConstants.ID;
import static net.sovrinhealth.fhir.search.SearchConstants.LAST_UPDATED;
import static net.sovrinhealth.fhir.search.SearchConstants.PROFILE;
import static net.sovrinhealth.fhir.search.SearchConstants.SECURITY;
import static net.sovrinhealth.fhir.search.SearchConstants.TAG;
import static net.sovrinhealth.fhir.search.SearchConstants.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.sovrinhealth.fhir.config.FHIRConfigHelper;
import net.sovrinhealth.fhir.database.utils.api.IDatabaseTranslator;
import net.sovrinhealth.fhir.database.utils.api.SchemaType;
import net.sovrinhealth.fhir.database.utils.common.DataDefinitionUtil;
import net.sovrinhealth.fhir.database.utils.query.Operator;
import net.sovrinhealth.fhir.database.utils.query.Select;
import net.sovrinhealth.fhir.database.utils.query.SelectAdapter;
import net.sovrinhealth.fhir.database.utils.query.WhereAdapter;
import net.sovrinhealth.fhir.database.utils.query.WhereFragment;
import net.sovrinhealth.fhir.database.utils.query.expression.ColumnExpNodeVisitor;
import net.sovrinhealth.fhir.database.utils.query.expression.StringExpNodeVisitor;
import net.sovrinhealth.fhir.database.utils.query.node.ExpNode;
import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceNotSupportedException;
import net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants;
import net.sovrinhealth.fhir.persistence.jdbc.dao.api.JDBCIdentityCache;
import net.sovrinhealth.fhir.persistence.jdbc.dao.impl.ResourceProfileRec;
import net.sovrinhealth.fhir.persistence.jdbc.dto.CommonTokenValue;
import net.sovrinhealth.fhir.persistence.jdbc.dto.ResourceReferenceValue;
import net.sovrinhealth.fhir.persistence.jdbc.util.CanonicalSupport;
import net.sovrinhealth.fhir.persistence.jdbc.util.CanonicalValue;
import net.sovrinhealth.fhir.persistence.jdbc.util.NewUriModifierUtil;
import net.sovrinhealth.fhir.persistence.jdbc.util.SqlParameterEncoder;
import net.sovrinhealth.fhir.persistence.jdbc.util.type.NewDateParmBehaviorUtil;
import net.sovrinhealth.fhir.persistence.jdbc.util.type.NewLastUpdatedParmBehaviorUtil;
import net.sovrinhealth.fhir.persistence.jdbc.util.type.NewLocationParmBehaviorUtil;
import net.sovrinhealth.fhir.persistence.jdbc.util.type.NewNumberParmBehaviorUtil;
import net.sovrinhealth.fhir.persistence.jdbc.util.type.NewQuantityParmBehaviorUtil;
import net.sovrinhealth.fhir.persistence.jdbc.util.type.OperatorUtil;
import net.sovrinhealth.fhir.search.SearchConstants;
import net.sovrinhealth.fhir.search.SearchConstants.Modifier;
import net.sovrinhealth.fhir.search.SearchConstants.Prefix;
import net.sovrinhealth.fhir.search.SearchConstants.Type;
import net.sovrinhealth.fhir.search.exception.FHIRSearchException;
import net.sovrinhealth.fhir.search.location.NearLocationHandler;
import net.sovrinhealth.fhir.search.location.bounding.Bounding;
import net.sovrinhealth.fhir.search.parameters.InclusionParameter;
import net.sovrinhealth.fhir.search.parameters.QueryParameter;
import net.sovrinhealth.fhir.search.parameters.QueryParameterValue;
import net.sovrinhealth.fhir.search.sort.Sort.Direction;
import net.sovrinhealth.fhir.search.util.ReferenceUtil;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;
import net.sovrinhealth.fhir.term.util.ValueSetSupport;

/**
 * Used to render the domain model into a physical, executable query
 * modeled as a Select statement. The domain model knows about resources
 * and parameters. This class is used to translate the logical structure
 * of the query into a physical one, using the correct table names, join
 * predicates and filter expressions.
 */
public class SearchQueryRenderer implements SearchQueryVisitor<QueryData> {
    private static final String CLASSNAME = SearchQueryRenderer.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);

    private final static String STR_VALUE = "STR_VALUE";
    private final static String STR_VALUE_LCASE = "STR_VALUE_LCASE";
    private final static String LOGICAL_RESOURCES = "LOGICAL_RESOURCES";

    // Database translator to handle SQL syntax variations among databases
    private final IDatabaseTranslator translator;

    // A cache providing access to various database reference ids
    private final JDBCIdentityCache identityCache;

    // pagination page number
    private final int rowOffset;

    // pagination page size
    private final int rowsPerPage;

    // Counter so we can allocate unique alias names
    private int paramCounter = 0;

    // Enable use of legacy whole-system search parameters for the search request
    private final boolean legacyWholeSystemSearchParamsEnabled;

    // Include DATA in the data fetch queries
    private final boolean includeResourceData;

    // The schema variant which can affect how we construct some statements
    private final SchemaType schemaType;

    /**
     * Public constructor
     * @param translator
     * @param identityCache
     * @param rowOffset
     * @param rowsPerPage
     * @param includeResourceData
     * @param schemaType
     */
    public SearchQueryRenderer(IDatabaseTranslator translator, JDBCIdentityCache identityCache,
            int rowOffset, int rowsPerPage, boolean includeResourceData, SchemaType schemaType) {
        this.translator = translator;
        this.identityCache = identityCache;
        this.rowOffset = rowOffset;
        this.rowsPerPage = rowsPerPage;
        this.includeResourceData = includeResourceData;
        this.schemaType = schemaType;
        this.legacyWholeSystemSearchParamsEnabled =
                FHIRConfigHelper.getBooleanProperty(PROPERTY_SEARCH_ENABLE_LEGACY_WHOLE_SYSTEM_SEARCH_PARAMS, false);
    }

    /**
     * Get the next index number to use as a parameter table alias
     * @return
     */
    protected int getNextAliasIndex() {
        return ++paramCounter;
    }

    /**
     * Get the table name for the xx_logical_resources table where xx is the
     * resource type name
     * @param resourceType
     * @return the table name
     */
    protected String resourceLogicalResources(String resourceType) {
        if (isWholeSystemSearch(resourceType)) {
            return LOGICAL_RESOURCES;
        } else {
            return resourceType + _LOGICAL_RESOURCES;
        }
    }
    
    protected String resourceTypeField(String resourceType, int resourceTypeId) {
        // If the query is a whole-system-search running at the global
        // logical_resources level, we can get the resource type directly
        // from the logical_resources table
        if (isWholeSystemSearch(resourceType)) {
            return "LR.RESOURCE_TYPE_ID";
        } else {
            // Use a literal value for the resource_type_id value
            return Integer.toString(resourceTypeId);
        }
    }

    /**
     * Get the table name for the xx_resources table where xx is the resource type name
     * @param resourceType
     * @return
     */
    protected String resourceResources(String resourceType) {
        return resourceType + _RESOURCES;
    }

    /**
     * Get the id for the given parameter name (cache lookup)
     * @param parameterName
     * @return
     */
    protected int getParameterNameId(String parameterName) throws FHIRPersistenceException {
        return this.identityCache.getParameterNameId(parameterName);
    }

    /**
     * Get the common token value id matching the unique tuple {system, code}
     * @param system
     * @param code
     * @return
     * @throws FHIRPersistenceException
     */
    protected Long getCommonTokenValueId(String system, String code) throws FHIRPersistenceException {
        return this.identityCache.getCommonTokenValueId(system, code);
    }

    /**
     * Get the common token value ids for the passed list of token values {system, code}.
     * @param tokenValues
     * @return
     * @throws FHIRPersistenceException
     */
    protected Set<Long> getCommonTokenValueIds(Collection<CommonTokenValue> tokenValues) throws FHIRPersistenceException {
        return this.identityCache.getCommonTokenValueIds(tokenValues);
    }

    /**
     * Obtain the logical_resource_id values for each of the given ResourceReferenceValues.
     * @param referenceValues
     * @return
     * @throws FHIRPersistenceException
     */
    protected Set<Long> getLogicalResourceIds(Collection<ResourceReferenceValue> referenceValues) throws FHIRPersistenceException {
        return this.identityCache.getLogicalResourceIds(referenceValues);
    }

    /**
     * Obtain the list of logical_resource_id values that match the given logicalId.
     * @param logicalId
     * @return
     */
    protected List<Long> getLogicalResourceIdList(String logicalId) throws FHIRPersistenceException {
        return this.identityCache.getLogicalResourceIdList(logicalId);
    }

    /**
     * Get a list of common token values matching the given code
     * @param code
     * @return
     * @throws FHIRPersistenceException
     */
    protected List<Long> getCommonTokenValueIdList(String code) throws FHIRPersistenceException {
        return this.identityCache.getCommonTokenValueIdList(code);
    }

    /**
     * Get the id for the given code system name (cache lookup)
     * @param codeSystemName
     * @return
     * @throws FHIRPersistenceException
     */
    protected int getCodeSystemId(String codeSystemName) throws FHIRPersistenceException {
        return this.identityCache.getCodeSystemId(codeSystemName);
    }

    /**
     * Get the id for the given canonicalValue (cache lookup).
     * @param canonicalValue
     * @return the database id, or -1 if the value does not exist
     * @throws FHIRPersistenceException
     */
    protected long getCanonicalId(String canonicalValue) throws FHIRPersistenceException {
        return this.identityCache.getCanonicalId(canonicalValue);
    }

    @Override
    public QueryData countRoot(String rootResourceType) {
        final int aliasIndex = 0;
        final String xxLogicalResources = resourceLogicalResources(rootResourceType);
        final String lrAliasName = getLRAlias(aliasIndex);

        // The basic count query from the xx_LOGICAL_RESOURCES table. Query
        // parameters are bolted on as exists statements in the WHERE clause.
        // No need to join with xx_RESOURCES, because we only need to count
        // undeleted logical resources, not individual resource versions
        /*
          SELECT COUNT(*) AS CNT
            FROM Patient_LOGICAL_RESOURCES AS LR0
           WHERE LR0.IS_DELETED = 'N'
             AND EXISTS (
          SELECT 1
            FROM Patient_LOGICAL_RESOURCES AS LR1
      INNER JOIN Patient_STR_VALUES AS P2 ON P2.LOGICAL_RESOURCE_ID = LR1.LOGICAL_RESOURCE_ID
             AND P2.PARAMETER_NAME_ID = 1246
             AND (P2.STR_VALUE = ?)
           WHERE LR1.IS_DELETED = 'N'
             AND LR1.LOGICAL_RESOURCE_ID = LR0.LOGICAL_RESOURCE_ID)
         */
        SelectAdapter select = Select.select().addColumn(null, "COUNT(*)", alias("CNT"));
        select.from(xxLogicalResources, alias(lrAliasName))
            .where(lrAliasName, IS_DELETED).eq(string("N"));
        return new QueryData(select, lrAliasName, null, rootResourceType, 0);
    }

    @Override
    public QueryData dataRoot(String rootResourceType, int resourceTypeId) {
        /*
        // The data root query is formed as an inner select statement which we
        // then inner join to the xx_RESOURCES table as a final step. This is
        // crucial to enable the optimizer to generate the correct plan.
        // The final query looks something like this:
              SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY
                FROM (
              SELECT LR0.LOGICAL_RESOURCE_ID, LR0.LOGICAL_ID, LR0.CURRENT_RESOURCE_ID
                FROM Patient_LOGICAL_RESOURCES AS LR0
               WHERE LR0.IS_DELETED = 'N'
                 AND EXISTS (
              SELECT 1
                FROM Patient_LOGICAL_RESOURCES AS LR1
          INNER JOIN Patient_STR_VALUES AS P2 ON P2.LOGICAL_RESOURCE_ID = LR1.LOGICAL_RESOURCE_ID
                 AND P2.PARAMETER_NAME_ID = 1246
                 AND (P2.STR_VALUE = ?)
               WHERE LR1.IS_DELETED = 'N'
                 AND LR1.LOGICAL_RESOURCE_ID = LR0.LOGICAL_RESOURCE_ID)) AS LR
          INNER JOIN Patient_RESOURCES AS R ON LR.CURRENT_RESOURCE_ID = R.RESOURCE_ID
            ORDER BY LR.LOGICAL_RESOURCE_ID
         FETCH FIRST 10 ROWS ONLY
        */
        final String xxLogicalResources = resourceLogicalResources(rootResourceType);
        final String lrAliasName = "LR0";

        // The core data query joining together the logical resources table. Query
        // parameters are bolted on as exists statements in the WHERE clause. The final
        // query is constructed when joinResources is called.
        SelectAdapter select = Select.select("LR0.LOGICAL_RESOURCE_ID", "LR0.LOGICAL_ID", "LR0.CURRENT_RESOURCE_ID");
        if (resourceTypeId >= 0) {
            // needed for whole system search where the resource type is required
            // in order to process the resource payload (which may be offloaded)
            select.addColumn(Integer.toString(resourceTypeId), alias("RESOURCE_TYPE_ID"));
        }
        select.from(xxLogicalResources, alias(lrAliasName))
            .where(lrAliasName, IS_DELETED).eq().literal("N");
        return new QueryData(select, lrAliasName, null, rootResourceType, 0);
    }

    @Override
    public QueryData getParameterBaseQuery(QueryData parent) {
        final int aliasIndex = getNextAliasIndex();
        final String xxLogicalResources = resourceLogicalResources(parent.getResourceType());
        final String lrAlias = getLRAlias(aliasIndex);
        final String parentLRAlias = parent.getLRAlias();

        // SELECT 1 FROM xx_LOGICAL_RESOURCES LRn
        //    INNER JOIN ...
        //    INNER JOIN ...
        //         WHERE LRn.LOGICAL_RESOURCE_ID = LRp.LOGICAL_RESOURCE_ID
        //
        // Note: IS_DELETED is not checked in this sub-query - if necessary, that is
        //       the responsibility of the parent query.
        SelectAdapter exists = Select.select("1");
        exists.from(xxLogicalResources, alias(lrAlias))
           .where(lrAlias, "LOGICAL_RESOURCE_ID").eq(parentLRAlias, "LOGICAL_RESOURCE_ID"); // correlate to parent query

        // Add this exists to the parent query
        parent.getQuery().from().where().and().exists(exists.build());

        // This bit is important to understanding how this works. We return the
        // sub-query here, not the main query. The sub-query is returned because
        // it is the query to which we attach all the parameter table joins
        return new QueryData(exists, lrAlias, null, parent.getResourceType(), 0);
    }

    @Override
    public QueryData joinResources(QueryData queryData, boolean includeResourceTypeId) {
        final SelectAdapter logicalResources = queryData.getQuery();
        final String xxResources = resourceResources(queryData.getResourceType());
        final String lrAliasName = "LR";
        SelectAdapter select = Select.select("R.RESOURCE_ID", "R.LOGICAL_RESOURCE_ID", "R.VERSION_ID", "R.LAST_UPDATED",
                "R.IS_DELETED", getDataCol(), "LR.LOGICAL_ID", "R.RESOURCE_PAYLOAD_KEY");
        
        // Resource type id is used for whole-system-search cases where the query
        // can return resources of different types (e.g. both Patient and Observation)
        if (includeResourceTypeId) {
            select.addColumn(lrAliasName, "RESOURCE_TYPE_ID", alias("RESOURCE_TYPE_ID"));
        }
        select.from(logicalResources.build(), alias(lrAliasName))
            .innerJoin(xxResources, alias("R"), on(lrAliasName, "CURRENT_RESOURCE_ID").eq("R", "RESOURCE_ID"));

        // The final query still needs ordering/pagination to be applied
        return new QueryData(select, lrAliasName, null, queryData.getResourceType(), queryData.getChainDepth());
    }

    @Override
    public QueryData includeRoot(String rootResourceType) {

        /* Final query should like this:
        SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID
                FROM (
              SELECT LR0.LOGICAL_RESOURCE_ID, LR0.LOGICAL_ID, LR0.CURRENT_RESOURCE_ID
                FROM Patient_LOGICAL_RESOURCES AS LR0
               WHERE LR0.IS_DELETED = 'N'
                 AND EXISTS (
              SELECT 1
                FROM Patient_LOGICAL_RESOURCES AS LR1
          INNER JOIN Patient_STR_VALUES AS P2 ON P2.LOGICAL_RESOURCE_ID = LR1.LOGICAL_RESOURCE_ID
                 AND P2.PARAMETER_NAME_ID = 1246
                 AND (P2.STR_VALUE = ?)
               WHERE LR1.IS_DELETED = 'N'
                 AND LR1.LOGICAL_RESOURCE_ID = LR0.LOGICAL_RESOURCE_ID)) AS LR
          INNER JOIN Patient_RESOURCES AS R ON LR.CURRENT_RESOURCE_ID = R.RESOURCE_ID
            ORDER BY LR.LOGICAL_RESOURCE_ID
         FETCH FIRST 10 ROWS ONLY
         */

        // The root query is just the inner distinct piece. The overall query is built by wrapInclude
        final boolean distinct = true;
        SelectAdapter select = Select.select(distinct, "R0.RESOURCE_ID", "R0.LOGICAL_RESOURCE_ID", "R0.VERSION_ID", "R0.LAST_UPDATED", "R0.IS_DELETED", "LR0.LOGICAL_ID");
        return new QueryData(select, null, null, rootResourceType, 0);
    }

    @Override
    public QueryData wrapInclude(QueryData query) {
        // Need to join the RESOURCES table again to get the DATA column after the DISTINCT.
        final String lrAlias = "LR";
        final String rAlias = "R";
        final String rTable = query.getResourceType() + "_RESOURCES";
        SelectAdapter select = Select.select("LR.RESOURCE_ID", "LR.LOGICAL_RESOURCE_ID", "LR.VERSION_ID",
                "LR.LAST_UPDATED", "LR.IS_DELETED", getDataCol(), "LR.LOGICAL_ID", "R.RESOURCE_PAYLOAD_KEY");
        select.from(query.getQuery().build(), alias(lrAlias))
            .innerJoin(rTable, alias(rAlias), on(lrAlias, "RESOURCE_ID").eq(rAlias, "RESOURCE_ID"));
        return new QueryData(select, lrAlias, null, query.getResourceType(), 0);
    }

    @Override
    public QueryData sortRoot(String rootResourceType) {
        final String xxLogicalResources = resourceLogicalResources(rootResourceType);
        final String lrAliasName = "LR0";

        // The core data query joining together the logical resources table. Query
        // parameters are bolted on as exists statements in the WHERE clause. The final
        // query is constructed when joinResources is called.
        SelectAdapter select = Select.select("LR0.CURRENT_RESOURCE_ID");
        select.from(xxLogicalResources, alias(lrAliasName))
            .where(lrAliasName, IS_DELETED).eq().literal("N");

        // We need to group the sort parameters to address any duplicates
        select.from().groupBy("LR0.CURRENT_RESOURCE_ID");
        return new QueryData(select, lrAliasName, null, rootResourceType, 0);
    }

    @Override
    public QueryData wholeSystemFilterRoot() {
        /* Final query should look like this:
        SELECT LR0.RESOURCE_TYPE_ID, LR0.LOGICAL_RESOURCE_ID
                FROM LOGICAL_RESOURCES AS LR0
               WHERE LR0.IS_DELETED = 'N'
                 AND EXISTS (
              SELECT 1
                FROM LOGICAL_RESOURCES AS LR1
          INNER JOIN RESOURCE_TOKEN_REFS AS P2 ON P2.LOGICAL_RESOURCE_ID = LR1.LOGICAL_RESOURCE_ID
                 AND P2.PARAMETER_NAME_ID = 1008
                 AND ((P2.COMMON_TOKEN_VALUE_ID = 4))
               WHERE LR1.IS_DELETED = 'N'
                 AND LR1.LOGICAL_RESOURCE_ID = LR0.LOGICAL_RESOURCE_ID)
            ORDER BY LR0.LOGICAL_RESOURCE_ID
         FETCH FIRST 10 ROWS ONLY
         */

        final String lrAliasName = "LR0";

        // The core data query joining together the logical resources table. Query
        // parameters are bolted on as exists statements in the WHERE clause.
        SelectAdapter select = Select.select("LR0.RESOURCE_TYPE_ID", "LR0.LOGICAL_RESOURCE_ID");
        select.from(LOGICAL_RESOURCES, alias(lrAliasName))
            .where(lrAliasName, IS_DELETED).eq().literal("N");
        return new QueryData(select, lrAliasName, null, Resource.class.getSimpleName(), 0);
    }

    @Override
    public QueryData wholeSystemDataRoot(String rootResourceType, int rootResourceTypeId) {
        /* Final query should look something like this (where [RTFIELD] depends on the type of the
         * whole system search):
              SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY, [RTFIELD] AS RESOURCE_TYPE_ID
                FROM (
              SELECT LR.LOGICAL_RESOURCE_ID, LR.LOGICAL_ID, LR.CURRENT_RESOURCE_ID
                FROM Patient_LOGICAL_RESOURCES AS LR
               WHERE LR.IS_DELETED = 'N') AS LR
                 AND LR.LOGICAL_RESOURCE_ID IN (2,4,6,10,12,14,20,24,26,29)) AS LR
          INNER JOIN Patient_RESOURCES AS R ON LR.CURRENT_RESOURCE_ID = R.RESOURCE_ID
            ORDER BY LR.LOGICAL_RESOURCE_ID
         FETCH FIRST 10 ROWS ONLY
         */
        final String xxLogicalResources = resourceLogicalResources(rootResourceType);
        final String resourceTypeIdStr = Integer.toString(rootResourceTypeId);
        final String lrAliasName = "LR";

        // The core data query joining together the logical resources table. The final
        // query is constructed when joinResources is called.
        SelectAdapter select = Select.select("LR.LOGICAL_RESOURCE_ID", "LR.LOGICAL_ID", "LR.CURRENT_RESOURCE_ID");
        select.addColumn(resourceTypeIdStr, alias("RESOURCE_TYPE_ID"));
        select.from(xxLogicalResources, alias(lrAliasName))
            .where(lrAliasName, IS_DELETED).eq().literal("N");
        return new QueryData(select, lrAliasName, null, rootResourceType, 0);
    }

    @Override
    public QueryData wrapWholeSystem(List<QueryData> queries, boolean isCountQuery) {
        /* We need to either sum the counts of each individual count query or
           aggregate the data of each individual data query.
           Final query should look something like this for a count query:
        SELECT SUM(CNT)
                FROM (
                   <count-query-1>
           UNION ALL
                   <count-query-2>
           UNION ALL
                   ...
           UNION ALL
                   <count-query-n>
                ) AS COMBINED RESULTS

           Final query should look something like this for a data query:
        SELECT RESOURCE_ID, LOGICAL_RESOURCE_ID, VERSION_ID, LAST_UPDATED, IS_DELETED, DATA, LOGICAL_ID, RESOURCE_PAYLOAD_KEY, RESOURCE_TYPE_ID
                FROM (
                   <data-query-1>
           UNION ALL
                   <data-query-2>
           UNION ALL
                   ...
           UNION ALL
                   <data-query-n>
                ) AS COMBINED RESULTS
            ORDER BY COMBINED RESULTS.LOGICAL_RESOURCE_ID
         FETCH FIRST 10 ROWS ONLY
         */
        SelectAdapter select;
        if (isCountQuery) {
            select = Select.select("SUM(CNT)");
        } else {
            select = Select.select("RESOURCE_ID", "LOGICAL_RESOURCE_ID", "VERSION_ID", "LAST_UPDATED", "IS_DELETED", "DATA", "LOGICAL_ID", "RESOURCE_PAYLOAD_KEY", "RESOURCE_TYPE_ID");
        }
        SelectAdapter first = null;
        SelectAdapter previous = null;
        for (QueryData query : queries) {
            SelectAdapter subSelect = query.getQuery();
            if (previous == null) {
                // Save head of UNION'd selects
                first = subSelect;
            } else {
                // Add current select as union of previous
                previous.unionAll(subSelect.getSelect());
            }
            previous = subSelect;
        }
        select.from(first.getSelect(), alias("COMBINED_RESULTS"));

        return new QueryData(select, null, null, Resource.class.getSimpleName(), 0);
    }

    /**
     * Get the filter predicate for the given token query parameter.
     * @param queryParm the token query parameter
     * @param paramAlias the alias used for the token values table
     * @throws FHIRPersistenceException
     */
    protected WhereFragment getTokenFilter(QueryParameter queryParm, String paramAlias) throws FHIRPersistenceException {
        final Operator operator = getOperator(queryParm, EQ);
        WhereFragment where = new WhereFragment();

        boolean first = true;
        where.leftParen();

        // Append the suffix for :text modifier
        String parameterName = queryParm.getCode();
        if (Modifier.TEXT.equals(queryParm.getModifier())) {
            parameterName += SearchConstants.TEXT_MODIFIER_SUFFIX;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getTokenFilter: '" + parameterName + "'" + ", Operator: " + operator + ", modifier: " + queryParm.getModifier());
        }

        for (QueryParameterValue value : queryParm.getValues()) {
            // If multiple values are present, we need to OR them together.
            if (first) {
                first = false;
            } else {
                where.or();
            }

            // The expression may be complex, and we may need to OR them together. To avoid any
            // precedence drama, we simply wrap everything in parens just to be safe
            where.leftParen();

            if (Modifier.IN.equals(queryParm.getModifier()) ||
                    Modifier.NOT_IN.equals(queryParm.getModifier()) ||
                    Modifier.ABOVE.equals(queryParm.getModifier()) ||
                    Modifier.BELOW.equals(queryParm.getModifier())) {
                // IN and NOT_IN can follow the same code path here because of our assumption that
                // if there's any matching parameter values then we'll also find a matching common_token_value_id
                //
                // Assume we have a valueset vs with codes like s1|c1 and s2|c2 and we have Basic resources `a` and `b`.
                // Now the query comes like Basic?p:not-in=vs
                //
                // Scenario 1: `a` has the value `s1|c1` for the coding targeted by parameter p.
                //
                //    WHERE NOT EXISTS(
                //    SELECT 1
                //    FROM Basic_RESOURCE_TOKEN_REFS AS P2
                //    WHERE P2.LOGICAL_RESOURCE_ID = LR1.LOGICAL_RESOURCE_ID
                //      AND P2.PARAMETER_NAME_ID = 20046
                //      AND ((P2.COMMON_TOKEN_VALUE_ID IN (1)))))) AS LR
                //
                // the select subquery selects 1 for `a`
                // the select subquery selects empty for `b`
                // so as a result it selects `b` and not `a`
                //
                //
                // Scenario 2: none of the resources have any of the values from the valueset
                //
                //    WHERE NOT EXISTS(
                //    SELECT 1
                //    FROM Basic_RESOURCE_TOKEN_REFS AS P2
                //    WHERE P2.LOGICAL_RESOURCE_ID = LR1.LOGICAL_RESOURCE_ID
                //      AND P2.PARAMETER_NAME_ID = 20046
                //      AND ((P2.COMMON_TOKEN_VALUE_ID IN (-1)))))) AS LR
                //
                // The select subquery will always return empty (we never have a -1 in the db)
                // so as a result it selects `a` and `b` (and any other resources)
                //
                // This is what we want because it matches the semantics of `:not`; it select
                // resources with other values AND resources with no value for this parameter at all
                //
                populateCodesSubSegment(where, queryParm.getModifier(), value, paramAlias);
            } else {
                String system = value.getValueSystem();
                final String code = value.getValueCode();

                // Determine code normalization based on code system case-sensitivity
                String normalizedCode = null;
                if (code != null) {
                    if (system != null) {
                        boolean codeSystemIsCaseSensitive = CodeSystemSupport.isCaseSensitive(system);
                        normalizedCode = SqlParameterEncoder.encode(codeSystemIsCaseSensitive ?
                                            code : SearchHelper.normalizeForSearch(code));
                    } else {
                        normalizedCode = SqlParameterEncoder.encode(SearchHelper.normalizeForSearch(code));
                    }
                }

                // Replace an empty system with our default-token-system
                if (system != null && system.isEmpty()) {
                    system = JDBCConstants.DEFAULT_TOKEN_SYSTEM;
                }

                // Include code
                if (operator == Operator.EQ && code != null) {
                    if (system == null || system.equals("*")) {
                        // Even though we don't have a system, we can still use a list of
                        // common_token_value_ids matching the value-code, allowing a similar optimization
                        Set<Long> ctvs = new HashSet<>();
                        fetchCommonTokenValues(ctvs, SqlParameterEncoder.encode(code));
                        fetchCommonTokenValues(ctvs, SqlParameterEncoder.encode(SearchHelper.normalizeForSearch(code)));
                        addCommonTokenValueIdFilter(where, paramAlias, ctvs);
                    } else {
                        Long commonTokenValueId = identityCache.getCommonTokenValueId(system, normalizedCode);
                        where.col(paramAlias, COMMON_TOKEN_VALUE_ID).eq(nullCheck(commonTokenValueId));
                    }
                } else {
                    // Traditional approach, using a join to xx_TOKEN_VALUES_V

                    // Include code if present
                    if (code != null) {
                        where.col(paramAlias, TOKEN_VALUE).operator(operator);
                        if (operator == Operator.LIKE) {
                            // Must escape special wildcard characters _ and % in the parameter value string
                            // as well as the escape character itself.
                            String textSearchString = normalizedCode
                                    .replace("+", "++")
                                    .replace(PERCENT_WILDCARD, ESCAPE_PERCENT)
                                    .replace(UNDERSCORE_WILDCARD, ESCAPE_UNDERSCORE) + PERCENT_WILDCARD;
                            where.bind(SearchHelper.normalizeForSearch(textSearchString)).escape("+");

                        } else {
                            where.bind(normalizedCode);
                        }
                    }

                    // Include system if present
                    if (system != null) {
                        if (code != null) {
                            where.and();
                        }

                        // Filter on the code system for the given parameter
                        Integer codeSystemId = identityCache.getCodeSystemId(system);
                        where.col(paramAlias, CODE_SYSTEM_ID).eq().literal(nullCheck(codeSystemId));
                    }
                }
            }

            where.rightParen();
        }

        where.rightParen();

        return where;
    }

    /**
     * Adds a filter predicate for COMMON_TOKEN_VALUE_ID. Fetches the list of possible matches (there's no code-system,
     * so there could be multiple). If no match, then -1 is used to make sure the row isn't produced. If there is a
     * single match, the predicate is COMMON_TOKEN_VALUE_ID = {n}. If there are multiple matches, the predicate is
     * COMMON_TOKEN_VALUE_ID IN (1, 2, 3, ...).
     * The query uses literal values not bind variables on purpose (better performance).
     * @param where
     * @param paramAlias
     * @param searchValue
     * @throws FHIRPersistenceException
     */
    private void addCommonTokenValueIdFilter(WhereFragment where, String paramAlias, String searchValue) throws FHIRPersistenceException {
        // grab the list of all matching common_token_value_id values
        Set<Long> ctvs = new HashSet<>();
        fetchCommonTokenValues(ctvs, searchValue);

        // and add a filter expression paramAlias IN (...) for the values
        addCommonTokenValueIdFilter(where, paramAlias, ctvs);
    }

    /**
     * Adds a filter predicate for ref_logical_resource_id. Fetches the list of posible matches (there's no resourceType,
     * so there could be multiple matches. If no match, then -1 is used to make sure the row isn't produced. If there is 
     * single match, the predicate uses an equality, otherwise an IN-LIST.
     * The query uses literal values not bind variables on purpose (better performance).
     * @param where
     * @param paramAlias
     * @param searchValue
     * @throws FHIRPersistenceException
     */
    private void addLogicalResourceIdFilter(WhereFragment where, String paramAlias, String searchValue) throws FHIRPersistenceException {
        // grab the list of all matching common_token_value_id values
        Set<Long> ctvs = new HashSet<>();
        fetchLogicalResourceValues(ctvs, searchValue);

        // and add a filter expression paramAlias IN (...) for the values
        addRefLogicalResourceIdFilter(where, paramAlias, ctvs);
    }

    /**
     * Add all common_token_value_id matching the given searchValue to the ctvs set.
     * @param ctvs
     * @param searchValue
     * @throws FHIRPersistenceException
     */
    private void fetchCommonTokenValues(Set<Long> ctvs, String searchValue) throws FHIRPersistenceException {
        List<Long> ctvList = this.identityCache.getCommonTokenValueIdList(searchValue);
        ctvs.addAll(ctvList);
    }

    /**
     * All all matching logical_resource_id values to the given set
     * @param lrids
     * @param searchValue
     * @throws FHIRPersistenceException
     */
    private void fetchLogicalResourceValues(Set<Long> lrids, String searchValue) throws FHIRPersistenceException {
        List<Long> tmpList = this.identityCache.getLogicalResourceIdList(searchValue);
        lrids.addAll(tmpList);        
    }

    /**
     * Adds a filter predicate for COMMON_TOKEN_VALUE_ID. If the ctvs list is empty, then -1 is used to make
     * sure the row isn't produced. If there is a single match, the predicate is COMMON_TOKEN_VALUE_ID = {n}.
     * If there are multiple matches, the predicate is COMMON_TOKEN_VALUE_ID IN (1, 2, 3, ...).
     * The query uses literal values not bind variables on purpose (better performance).
     * @param where
     * @param paramAlias
     * @param ctvs
     * @throws FHIRPersistenceException
     */
    private void addCommonTokenValueIdFilter(WhereFragment where, String paramAlias, Collection<Long> ctvs) throws FHIRPersistenceException {
        final List<Long> ctvList = new ArrayList<>(ctvs);
        if (ctvList.isEmpty()) {
            // use -1...resulting in no data
            where.col(paramAlias, COMMON_TOKEN_VALUE_ID).eq(-1L);
        } else if (ctvList.size() == 1) {
            where.col(paramAlias, COMMON_TOKEN_VALUE_ID).eq(ctvList.get(0));
        } else {
            where.col(paramAlias, COMMON_TOKEN_VALUE_ID).inLiteralLong(ctvList);
        }
    }

    /**
     * Adds a filter predicate for REF_LOGICAL_RESOURCE_ID. If the ctvs list is empty, then -1 is used to make
     * sure the row isn't produced. If there is a single match, the predicate is REF_LOGICAL_RESOURCE_ID = {n}.
     * If there are multiple matches, the predicate is REF_LOGICAL_RESOURCE_ID IN (1, 2, 3, ...).
     * The query uses literal values not bind variables on purpose (better performance).
     * @param where
     * @param paramAlias
     * @param ctvs
     * @throws FHIRPersistenceException
     */
    private void addRefLogicalResourceIdFilter(WhereFragment where, String paramAlias, Collection<Long> ctvs) throws FHIRPersistenceException {
        final List<Long> ctvList = new ArrayList<>(ctvs);
        if (ctvList.isEmpty()) {
            // use -1...resulting in no data
            where.col(paramAlias, REF_LOGICAL_RESOURCE_ID).eq(-1L);
        } else if (ctvList.size() == 1) {
            where.col(paramAlias, REF_LOGICAL_RESOURCE_ID).eq(ctvList.get(0));
        } else {
            where.col(paramAlias, REF_LOGICAL_RESOURCE_ID).inLiteralLong(ctvList);
        }
    }

    /**
     * Builds an SQL segment which populates an IN clause with codes for a token search parameter
     * specifying the :in, :not-in, :above, or :below modifier.
     *
     * @param whereClauseSegment  - the segment to which the sub-segment will be added
     * @param modifier            - the search parameter modifier (:in | :not-in | :above | :below)
     * @param parameterValue      - the search parameter value - a ValueSet URL or a CodeSystem URL + code
     * @param parameterTableAlias - the alias for the parameter table e.g. CPx
     * @throws FHIRPersistenceException
     */
    private void populateCodesSubSegment(WhereFragment whereClauseSegment, Modifier modifier,
            QueryParameterValue parameterValue, String parameterTableAlias) throws FHIRPersistenceException {

        // Get the codes to populate the IN clause.
        // Note: validation of the value set or the code system + code specified in parameterValue
        // was done when the search parameter was parsed, so does not need to be done here.
        Map<String, Set<String>> codeSetMap = null;
        if (Modifier.IN.equals(modifier) || Modifier.NOT_IN.equals(modifier)) {
            codeSetMap = ValueSetSupport.getCodeSetMap(ValueSetSupport.getValueSet(parameterValue.getValueCode()));
        } else if (Modifier.ABOVE.equals(modifier) || Modifier.BELOW.equals(modifier)) {
            CodeSystem codeSystem = CodeSystemSupport.getCodeSystem(parameterValue.getValueSystem());
            Code code = Code.builder().value(parameterValue.getValueCode()).build();
            Set<String> codes;
            if (Modifier.ABOVE.equals(modifier)) {
                codes = CodeSystemSupport.getAncestorsAndSelf(codeSystem, code);
            } else {
                codes = CodeSystemSupport.getDescendantsAndSelf(codeSystem, code);
            }
            codeSetMap = Collections.singletonMap(parameterValue.getValueSystem(), codes);
        }

        List<CommonTokenValue> commonTokenValues = new ArrayList<>();
        for (String codeSetUrl : codeSetMap.keySet()) {
            // get the codes for this URL before stripping off the version part
            Set<String> codes = codeSetMap.get(codeSetUrl);

            // Strip version from canonical codeSet URL. We don't store version in TOKEN_VALUES
            // table so will just ignore it.
            int index = codeSetUrl.lastIndexOf("|");
            if (index != -1) {
                codeSetUrl = codeSetUrl.substring(0, index);
            }

            Integer codeSystemId = identityCache.getCodeSystemId(codeSetUrl);

            if (codeSystemId == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Skipping codes from system '" + codeSetUrl + "' as there are no such indexed values in the db");
                }
                continue;
            }

            for (String code : codes) {
                commonTokenValues.add(new CommonTokenValue(codeSetUrl, codeSystemId, code));
            }
        }

        Set<Long> commonTokenValueIds = getCommonTokenValueIds(commonTokenValues);
        addCommonTokenValueIdFilter(whereClauseSegment, parameterTableAlias, commonTokenValueIds);
    }

    /**
     * Add a filter expression to the given parameter sub-query (which is used as an EXISTS clause)
     * @param paramExists the query statement to which we need to add the filter predicate
     * @param queryParm the query parameter for which we need to compute and add the filter predicate
     * @param paramAlias the alias for the query parameter table
     * @return
     * @throws FHIRPersistenceException
     */
    protected WhereFragment getStringFilter(QueryParameter queryParm, String paramAlias) throws FHIRPersistenceException {

        // Process the values from the queryParameter to produce
        // the predicates we need to pass to the visitor (which is
        // responsible for building the full query).
        final Operator operator = getOperator(queryParm);
        final String parameterName = queryParm.getCode();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getStringFilter: " + parameterName + ", op=" + operator.name() + ", modifier=" + queryParm.getModifier());
        }
        WhereFragment whereFragment = new WhereFragment();
        whereFragment.leftParen();

        boolean multiple = false;
        for (QueryParameterValue value : queryParm.getValues()) {
            // Concatenate multiple matches with an OR
            if (multiple) {
                whereFragment.or();
            } else {
                multiple = true;
            }
            if (operator == Operator.LIKE) {
                // Must escape special wildcard characters _ and % in the parameter value string
                // as well as the escape character itself.
                String tempSearchValue =
                        SqlParameterEncoder.encode(value.getValueString()
                                .replace("+", "++")
                                .replace(PERCENT_WILDCARD, ESCAPE_PERCENT)
                                .replace(UNDERSCORE_WILDCARD, ESCAPE_UNDERSCORE));

                if (Modifier.CONTAINS.equals(queryParm.getModifier())) {
                    String searchValue = PERCENT_WILDCARD + tempSearchValue + PERCENT_WILDCARD;
                    searchValue = SearchHelper.normalizeForSearch(searchValue);
                    whereFragment.col(paramAlias, STR_VALUE_LCASE).like(bind(searchValue)).escape("+");
                } else {
                    // If there is not a CONTAINS modifier on the query parm, construct
                    // a 'starts with' search value.
                    String searchValue = tempSearchValue + PERCENT_WILDCARD;

                    // Specific processing for
                    if (queryParm.getModifier() != null && queryParm.getType() == Type.URI) {
                        if (queryParm.getModifier() == Modifier.BELOW) {
                            searchValue = tempSearchValue + "/" + PERCENT_WILDCARD;

                            whereFragment.leftParen()
                            .col(paramAlias, STR_VALUE).eq(bind(tempSearchValue))
                            .or(paramAlias, STR_VALUE).like(bind(searchValue)).escape("+")
                            .rightParen();

                        } else if (queryParm.getModifier() == Modifier.ABOVE) {
                            NewUriModifierUtil.generateAboveValuesQuery(whereFragment, paramAlias, STR_VALUE, searchValue, operator);
                        } else {
                            // neither above nor below, so an exact match for URI
                            whereFragment.col(paramAlias, STR_VALUE).eq(bind(searchValue));
                        }
                    } else {
                        // Simple STARTS WITH
                        searchValue = SearchHelper.normalizeForSearch(searchValue);
                        logger.fine("LIKE: " + searchValue);
                        whereFragment.col(paramAlias, STR_VALUE_LCASE).like(bind(searchValue)).escape("+");
                    }
                }
            } else if (queryParm.getType() == Type.URI) {
                // need to handle above/below modifier
                if (queryParm.getModifier() == Modifier.BELOW) {
                    String tempSearchValue =
                            SqlParameterEncoder.encode(value.getValueString()
                                    .replace("+", "++")
                                    .replace(PERCENT_WILDCARD, ESCAPE_PERCENT)
                                    .replace(UNDERSCORE_WILDCARD, ESCAPE_UNDERSCORE));

                    String searchValue = tempSearchValue + "/" + PERCENT_WILDCARD;

                    whereFragment.leftParen()
                    .col(paramAlias, STR_VALUE).eq(bind(tempSearchValue))
                    .or(paramAlias, STR_VALUE).like(bind(searchValue)).escape("+")
                    .rightParen();

                } else if (queryParm.getModifier() == Modifier.ABOVE) {
                    String searchValue = SqlParameterEncoder.encode(value.getValueString());
                    NewUriModifierUtil.generateAboveValuesQuery(whereFragment, paramAlias, STR_VALUE, searchValue, operator);
                } else {
                    // neither above nor below, so an exact match for URI
                    String searchValue = SqlParameterEncoder.encode(value.getValueString());
                    whereFragment.col(paramAlias, STR_VALUE).eq(bind(searchValue));
                }
            } else if (operator == Operator.EQ) {
                // Exact match
                String searchValue = SqlParameterEncoder.encode(value.getValueString());
                whereFragment.col(paramAlias, STR_VALUE).eq(bind(searchValue));
            } else {
                // For anything other than an exact match, we search against the STR_VALUE_LCASE column in the
                // Resource's string values table.
                // Also, the search value is "normalized"; it has accents removed and is lower-cased. This enables a
                // case-insensitive, accent-insensitive search.
                // Build this piece: pX.str_value_lcase {operator} search-attribute-value
                String searchValue = SqlParameterEncoder.encode(value.getValueString());
                searchValue = SearchHelper.normalizeForSearch(searchValue);
                whereFragment.col(paramAlias, STR_VALUE_LCASE).operator(operator).bind(searchValue);
                addEscapeIfRequired(whereFragment, operator);
            }
        }

        whereFragment.rightParen();
        final ExpNode filter = whereFragment.getExpression();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("string filter[" + parameterName + "] := " + StringExpNodeVisitor.stringify(filter));
        }

        return whereFragment;
    }

    /**
     * Add the ESCAPE modified to the LIKE clause if needed
     * @param wf
     * @param op
     */
    private void addEscapeIfRequired(WhereFragment wf, Operator op) {
        if (op == Operator.LIKE) {
            wf.escape("+"); // adds ESCAPE '+'
        }
    }

    @Override
    public QueryData addSorting(QueryData queryData, String lrAlias) {
        final String lrLogicalResourceId = DataDefinitionUtil.getQualifiedName(lrAlias, "LOGICAL_RESOURCE_ID");
        queryData.getQuery().from().orderBy(lrLogicalResourceId);
        return queryData;
    }

    @Override
    public QueryData addWholeSystemSorting(QueryData queryData, List<DomainSortParameter> sortParms, String lrAlias) {
        if (sortParms == null || sortParms.isEmpty()) {
            return addSorting(queryData, lrAlias);
        } else {
            for (DomainSortParameter sortParm : sortParms) {
                // for whole-system searches, sort parameters can only be _id or _lastUpdated
                StringBuilder expression = new StringBuilder();
                expression.append(ID.equals(sortParm.getSortParameter().getCode()) ?
                        DataDefinitionUtil.getQualifiedName(lrAlias, "LOGICAL_ID") :
                        DataDefinitionUtil.getQualifiedName(lrAlias, "LAST_UPDATED"))
                    .append(" ")
                    .append(Direction.DECREASING.equals(sortParm.getSortParameter().getDirection()) ? DESCENDING : "");

                queryData.getQuery().from().orderBy(expression.toString());
            }
        }
        return queryData;
    }

    @Override
    public QueryData addPagination(QueryData queryData) {
        queryData.getQuery().pagination(rowOffset, rowsPerPage);
        return queryData;
    }

    /**
     * Get the parameter values table name (e.g. Patient_STR_VALUES) for the
     * given resource and parameter type. Note that this is now different from
     * the original QuerySegmentAggregator implementation - it does not differentiate
     * on chaining...that is left up to the building logic.
     * @param resourceType
     * @param paramType
     * @return
     */
    public String paramValuesTableName(String resourceType, QueryParameter queryParm) {
        boolean wholeSystemSearch = isWholeSystemSearch(resourceType);

        StringBuilder name = new StringBuilder(wholeSystemSearch ? "" : resourceType + "_");
        switch (queryParm.getType()) {
        case URI:
        case STRING:
            if (!this.legacyWholeSystemSearchParamsEnabled && PROFILE.equals(queryParm.getCode())) {
                name.append(wholeSystemSearch ? "LOGICAL_RESOURCE_PROFILES" : "PROFILES");
            } else {
                name.append("STR_VALUES");
            }
            break;
        case NUMBER:
            name.append("NUMBER_VALUES");
            break;
        case QUANTITY:
            name.append("QUANTITY_VALUES");
            break;
        case DATE:
            name.append("DATE_VALUES");
            break;
        case SPECIAL:
            name.append("LATLNG_VALUES");
            break;
        case REFERENCE:
            name.append("REF_VALUES");
            break;
        case TOKEN:
            if (!this.legacyWholeSystemSearchParamsEnabled && TAG.equals(queryParm.getCode())) {
                name.append(wholeSystemSearch ? "LOGICAL_RESOURCE_TAGS" : "TAGS");
            } else if (!this.legacyWholeSystemSearchParamsEnabled && SECURITY.equals(queryParm.getCode())) {
                name.append(wholeSystemSearch ? "LOGICAL_RESOURCE_SECURITY" : "SECURITY");
            } else {
                name.append("RESOURCE_TOKEN_REFS"); // bypass the xx_TOKEN_VALUES_V for performance reasons
            }
            break;
        case COMPOSITE:
            name.append("LOGICAL_RESOURCES");
            break;
        }
        return name.toString();
    }

    /**
     * Get the column name to use for the given paramType
     * @param paramType
     * @return
     */
    public String paramValuesColumnName(Type paramType) {
        final String result;
        switch (paramType) {
        case URI:
        case STRING:
            result = "STR_VALUES";
            break;
        case NUMBER:
            result = "NUMBER_VALUE";
            break;
        case QUANTITY:
            result = "QUANTITY_VALUE";
            break;
        case DATE:
            result = "DATE_VALUE";
            break;
        case SPECIAL:
            result = "LATLNG_VALUES";
            break;
        case REFERENCE:
            result = "REF_LOGICAL_RESOURCE_ID";
            break;
        case TOKEN:
            result = "TOKEN_VALUE";
            break;
        case COMPOSITE:
            result = null;
            break;
        default:
            result = null;
        }
        return result;
    }

    /**
     * Get a simple filter predicate which can be used in the WHERE clause of a search query.
     * This is used at the "leaf level" of parameter processing, where the queryParm relates
     * to a single parameter (i.e. it is the caller's responsibility to handle chaining and
     * other more complex behavior.
     * @param queryData
     * @param queryParm
     * @return
     * @throws FHIRPersistenceException
     */
    protected WhereFragment getFilterPredicate(QueryData queryData, QueryParameter queryParm) throws FHIRPersistenceException {
        WhereFragment filter = new WhereFragment();

        final String code = queryParm.getCode();
        final String parentAlias = queryData.getLRAlias();

        if (ID.equals(code)) {
            List<String> values = queryParm.getValues().stream().map(p -> p.getValueCode()).collect(Collectors.toList());
            if (values.size() == 1) {
                filter.col(parentAlias, "LOGICAL_ID").eq().bind(values.get(0));
            } else if (values.size() > 1) {
                // the values are converted to bind-markers, so this is secure
                filter.col(parentAlias, "LOGICAL_ID").in(values);
            } else {
                throw new FHIRPersistenceException("_id parameter value list is empty");
            }
        } else if (LAST_UPDATED.equals(code)) {
            // Compute the _lastUpdated filter predicate for the given query parameter
            NewLastUpdatedParmBehaviorUtil util = new NewLastUpdatedParmBehaviorUtil(parentAlias);
            util.executeBehavior(filter, queryParm);
        } else {
            // A simple filter added as an exists clause to the current query
            // AND EXISTS (SELECT 1
            //               FROM fhirdata.Patient_STR_VALUES AS P3                 -- 'Patient string parameters'
            //              WHERE P3.LOGICAL_RESOURCE_ID = LR2.LOGICAL_RESOURCE_ID  -- 'correlate to parent'
            //                AND P3.PARAMETER_NAME_ID = 123                        -- 'name parameter'
            //                AND P3.STR_VALUE = 'Jones')                           -- 'name filter'
            final int aliasIndex = getNextAliasIndex();
            final String paramTable = paramValuesTableName(queryData.getResourceType(), queryParm);
            final String paramAlias = getParamAlias(aliasIndex);
            SelectAdapter exists = Select.select("1");
            exists.from(paramTable, alias(paramAlias))
                .where(paramAlias, "LOGICAL_RESOURCE_ID").eq(parentAlias, "LOGICAL_RESOURCE_ID")
                .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(code))
                .and(paramFilter(queryParm, paramAlias).getExpression());
            filter.exists(exists.build());
        }

        return filter;
    }

    /**
     * Add a filter on the LOGICAL_ID for the given query parameter values
     * @param queryData
     * @param resourceType
     * @param queryParm
     */
    protected void addIdFilter(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        final SelectAdapter currentSubQuery = queryData.getQuery();
        final String parentAlias = queryData.getLRAlias();
        List<String> values = queryParm.getValues().stream().map(p -> p.getValueCode()).collect(Collectors.toList());
        if (values.size() == 1) {
            currentSubQuery.from().where().and(parentAlias, "LOGICAL_ID").eq().bind(values.get(0));
        } else if (values.size() > 1) {
            // the values are converted to bind-markers, so this is secure
            currentSubQuery.from().where().and(parentAlias, "LOGICAL_ID").in(values);
        } else {
            throw new FHIRPersistenceException("_id parameter value list is empty");
        }
        // If this is a whole-system search, add the following predicate in order to take
        // advantage of index on the LOGICAL_RESOURCES table:
        // AND <parentAlias>.RESOURCE_TYPE_ID IN (<list-of-all-resource-type-ids>)
        if (isWholeSystemSearch(resourceType)) {
            List<Long> resourceTypeIds = this.identityCache.getResourceTypeIds().stream().map(Long::valueOf).collect(Collectors.toList());
            currentSubQuery.from().where().and(parentAlias, "RESOURCE_TYPE_ID").inLiteralLong(resourceTypeIds);
        }
    }

    /**
     * Get a filter predicate for the given number query parameter
     * @param queryParm
     * @param paramAlias
     */
    protected WhereFragment getNumberFilter(QueryParameter queryParm, String paramAlias) throws FHIRPersistenceException {
        WhereFragment where = new WhereFragment();
        NewNumberParmBehaviorUtil behaviorUtil = new NewNumberParmBehaviorUtil();
        behaviorUtil.executeBehavior(where, queryParm, paramAlias);
        return where;
    }

    /**
     * Add a filter predicate to the given exists sub-query
     * @param queryParm
     * @param paramAlias
     */
    protected WhereFragment getQuantityFilter(QueryParameter queryParm, String paramAlias) throws FHIRPersistenceException {
        WhereFragment where = new WhereFragment();
        NewQuantityParmBehaviorUtil behaviorUtil = new NewQuantityParmBehaviorUtil(this.identityCache);
        behaviorUtil.executeBehavior(where, queryParm, paramAlias);
        return where;
    }

    /**
     * Add a filter predicate to the given exists sub-query
     * @param queryParm
     * @param paramAlias
     */
    protected WhereFragment getDateFilter(QueryParameter queryParm, String paramAlias) {
        WhereFragment where = new WhereFragment();
        NewDateParmBehaviorUtil util = new NewDateParmBehaviorUtil();

        // It is possible that multiple date parameters could be chained together if there were
        // multiple query parameters specified for the same date search parameter and we were
        // able to consolidate them. Check specifically if we have a consolidated parameter which
        // specifies a range, and if so, build a range filter.
        if (queryParm.isChained() && queryParm.getChain().size() == 1) {
            List<Prefix> lowerBoundPrefixes = Arrays.asList(Prefix.GT, Prefix.GE, Prefix.SA);
            List<Prefix> upperBoundPrefixes = Arrays.asList(Prefix.LT, Prefix.LE, Prefix.EB);
            Prefix prefix1 = queryParm.getValues().get(0).getPrefix();
            Prefix prefix2 = queryParm.getNextParameter().getValues().get(0).getPrefix();
            if (lowerBoundPrefixes.contains(prefix1) && upperBoundPrefixes.contains(prefix2)) {
                // The consolidated parameter specifies a range, so build a date range filter
                util.buildCustomRangeClause(where, paramAlias, queryParm, queryParm.getNextParameter());
                return where;
            } else if (lowerBoundPrefixes.contains(prefix2) && upperBoundPrefixes.contains(prefix1)) {
                // The consolidated parameter specifies a range, so build a date range filter
                util.buildCustomRangeClause(where, paramAlias, queryParm.getNextParameter(), queryParm);
                return where;
            }
        }

        boolean first = true;
        while (queryParm != null) {
            if (first) {
                first = false;
            } else {
                where.and();
            }
            util.executeBehavior(where, queryParm, paramAlias);
            queryParm = queryParm.getNextParameter();
        }
        return where;
    }

    /**
     * Add a filter predicate to the given exists sub-query
     * @param queryParm
     * @param paramAlias
     */
    protected WhereFragment getLocationFilter(QueryParameter queryParm, String paramAlias) throws FHIRPersistenceException {
        WhereFragment where = new WhereFragment();

        NearLocationHandler handler = new NearLocationHandler();
        List<Bounding> boundingAreas;
        try {
            boundingAreas = handler.generateLocationPositionsFromParameters(Arrays.asList(queryParm));
        } catch (FHIRSearchException e) {
            throw new FHIRPersistenceException("input parameter is invalid bounding area, bad prefix, or bad units", e);
        }

        NewLocationParmBehaviorUtil behaviorUtil = new NewLocationParmBehaviorUtil();
        behaviorUtil.buildLocationSearchQuery(where, boundingAreas, paramAlias);

        return where;
    }

    /**
     * Get the string to use as a parameter table alias for the given aliasIndex value
     * @param aliasIndex
     * @return
     */
    protected String getParamAlias(int aliasIndex) {
        return "P" + aliasIndex;
    }

    /**
     * Get the string to use as a logical resource alias for the given aliasIndex value
     * @param aliasIndex
     * @return
     */
    protected String getLRAlias(int aliasIndex) {
        return "LR" + aliasIndex;
    }

    /**
     * Compute the token parameter table name we want to use to join with. This method
     * inspects the content of the given filter {@link ExpNode}. If the filter contains
     * a reference to the TOKEN_VALUE column, the returned table name will be based
     * on xx_TOKEN_VALUES_V, otherwise it will be based on xx_RESOURCE_TOKEN_REFS. The
     * latter is preferable because it eliminates an unnecessary join, improves cardinality
     * estimation and (usually) results in a better execution plan.
     * @param filter
     * @param resourceType
     * @param paramAlias
     * @return
     */
    protected String getTokenParamTable(ExpNode filter, String resourceType, String paramAlias, boolean filterIsOptimized) {

        final String xxTokenValues;
        final String xxResourceTokenRefs = resourceType + "_RESOURCE_TOKEN_REFS";
        if (filterIsOptimized) {
            // only filters on COMMON_TOKEN_VALUE_ID so we can optimize by using only the
            // xx_resource_token_refs table. This results in much better cardinality
            // estimation and an a better plan
            xxTokenValues = xxResourceTokenRefs;
        } else {
            // can't optimize because we filter on TOKEN_VALUE. If the schema type is distributed,
            // this becomes a special case because COMMON_TOKEN_VALUES is distributed on TOKEN_VALUE which
            // is not a common column in the join predicate. In this case, we have to use a WITH clause
            // which then gets joined with the parameter table (Citus will aggregate the results of the WITH
            // select statement on the coordinator then pass those results to the worker nodes
            if (schemaType == SchemaType.DISTRIBUTED) {
                xxTokenValues = xxResourceTokenRefs;
            } else {
                // just use the standard view
                xxTokenValues = resourceType + "_TOKEN_VALUES_V";
            }
        }
        return xxTokenValues;
    }

    /**
     * Inspect the filter expression to see if it mentions the TOKEN_VALUE
     * or CODE_SYSTEM_ID columns. If neither of these columns is mentioned, 
     * we can optimize to use only the xx_resource_token_refs which leads to much 
     * better overall cardinality estimation by the query optimizer.
     * @param filter
     * @param paramAlias
     * @return
     */
    protected boolean isOptimizedTokenParamFilter(ExpNode filter, String paramAlias) {
        ColumnExpNodeVisitor visitor = new ColumnExpNodeVisitor(); // gathers all columns used in the filter expression
        Set<String> columns = filter.visit(visitor);
        boolean usesTokenValue = columns.contains(DataDefinitionUtil.getQualifiedName(paramAlias, TOKEN_VALUE)) ||
                                    columns.contains(DataDefinitionUtil.getQualifiedName(paramAlias, CODE_SYSTEM_ID));
        
        return !usesTokenValue;
    }

    /**
     * Compute the reference parameter table name we want to use to join with. This method
     * inspects the content of the given filter {@link ExpNode}. If the filter contains
     * a reference to the LOGICAL_ID column, the returned table name will be based
     * on xx_REF_VALUES_V, otherwise it will be based on xx_REF_VALUES. The
     * latter is preferable because it eliminates an unnecessary join, improves cardinality
     * estimation and (usually) results in a better execution plan.
     * @param filter
     * @param resourceType
     * @param paramAlias
     * @return
     */
    protected String getRefParamTable(ExpNode filter, String resourceType, String paramAlias) {
        ColumnExpNodeVisitor visitor = new ColumnExpNodeVisitor(); // gathers all columns used in the filter expression
        Set<String> columns = filter.visit(visitor);
        boolean usesLogicalIdValue = columns.contains(DataDefinitionUtil.getQualifiedName(paramAlias, LOGICAL_ID)) ||
                                    columns.contains(DataDefinitionUtil.getQualifiedName(paramAlias, RESOURCE_TYPE_ID));

        final String xxRefValues;
        if (usesLogicalIdValue) {
            // can't optimize because we filter on LOGICAL_ID
            xxRefValues = resourceType + "_REF_VALUES_V";
        } else {
            // only filters on REF_LOGICAL_RESOURCE_ID so we can optimize
            xxRefValues = resourceType + "_REF_VALUES";
        }
        return xxRefValues;
    }

    protected WhereFragment getIdentifierFilter(QueryParameter queryParm, String paramAlias) throws FHIRPersistenceException {
        WhereFragment whereClause = new WhereFragment();
        whereClause.leftParen();
        handleIdentifier(queryParm, paramAlias, whereClause);
        whereClause.rightParen();
        return whereClause;        
    }

    /**
     * Create a filter predicate for the given reference query parameter
     * @param queryParm
     * @param paramAlias
     * @throws FHIRPersistenceException
     */
    protected WhereFragment getReferenceFilter(QueryParameter queryParm, String paramAlias) throws FHIRPersistenceException {
        WhereFragment whereClause = new WhereFragment();
        whereClause.leftParen();

        if (Modifier.IDENTIFIER.equals(queryParm.getModifier())) {
            handleIdentifier(queryParm, paramAlias, whereClause);
            whereClause.rightParen();
            return whereClause;
        }

        List<Pair<String, String>> resourceTypesAndIds = new ArrayList<>(queryParm.getValues().size());
        for (QueryParameterValue value : queryParm.getValues()) {
            resourceTypesAndIds.add(getResourceTypeAndId(queryParm, value));
        }

        List<ResourceReferenceValue> refValues = new ArrayList<>(queryParm.getValues().size());
        List<String> ambiguousResourceReferenceTokenValues = new ArrayList<>();
        for (Pair<String, String> resourceTypeAndId : resourceTypesAndIds) {
            String targetResourceType = resourceTypeAndId.getLeft();
            String targetResourceId = resourceTypeAndId.getRight();

            if (targetResourceType != null) {
                Integer resourceTypeId = identityCache.getResourceTypeId(targetResourceType);
                // targetResourceType is treated as the code-system for references
                refValues.add(new ResourceReferenceValue(targetResourceType, resourceTypeId, targetResourceId));
            } else {
                ambiguousResourceReferenceTokenValues.add(targetResourceId);
            }
        }

        // For unambiguous resource references, look up the logical_resource_ids
        Set<Long> resourceReferenceTokenIds = getLogicalResourceIds(refValues);
        addRefLogicalResourceIdFilter(whereClause, paramAlias, resourceReferenceTokenIds);

        for (String targetResourceId : ambiguousResourceReferenceTokenValues) {
            whereClause.or();

            // grab the list of all matching common_token_value_id values
            addLogicalResourceIdFilter(whereClause, paramAlias, targetResourceId);
        }

        whereClause.rightParen();
        return whereClause;
    }

    protected WhereFragment getReferenceFilter(QueryParameter queryParm, String paramAlias, List<Long> logicalResourceIdList) throws FHIRPersistenceException {
        WhereFragment whereClause = new WhereFragment();
        whereClause.leftParen();

        // For unambiguous resource references, look up the logical_resource_ids
        addRefLogicalResourceIdFilter(whereClause, paramAlias, logicalResourceIdList);

        whereClause.rightParen();
        return whereClause;
    }

    /**
     * Create a filter predicate for the given reference query parameter using
     * the ambiguous 
     * @param queryParm
     * @param paramAlias
     * @throws FHIRPersistenceException
     */
    protected WhereFragment getReferenceStrFilter(QueryParameter queryParm, String paramAlias, List<String> ambiguousResourceReferenceTokenValues) throws FHIRPersistenceException {
        WhereFragment whereClause = new WhereFragment();
        whereClause.leftParen();
        whereClause.col(paramAlias, "str_value").in(ambiguousResourceReferenceTokenValues);
        whereClause.rightParen();
        return whereClause;
    }

    private Pair<String,String> getResourceTypeAndId(QueryParameter queryParm, QueryParameterValue value) {
        String targetResourceType = null;
        String searchValue = SqlParameterEncoder.encode(value.getValueString());

        // Make sure we split out the resource type if it is included in the search value
        String[] parts = value.getValueString().split("/");
        if (parts.length == 2) {
            targetResourceType = parts[0];
            searchValue = parts[1];
        }

        // Handle query parm representing this name/value pair construct:
        // <code>{name}:{Resource Type} = {resource-id}</code>
        if (queryParm.getModifier() != null && queryParm.getModifier().equals(Modifier.TYPE)) {
            if (!SearchConstants.Type.REFERENCE.equals(queryParm.getType())) {
                // Not a Reference
                searchValue = queryParm.getModifierResourceTypeName() + "/"
                            + SqlParameterEncoder.encode(value.getValueString());
            } else {
                // This is a Reference type.
                if (parts.length != 2) {
                    // fallback to get the target resource type using the modifier
                    targetResourceType = queryParm.getModifierResourceTypeName();
                }
            }
        }

        return new ImmutablePair<>(targetResourceType, searchValue);
    }

    private void handleIdentifier(QueryParameter queryParm, String paramAlias, WhereFragment whereClause) throws FHIRPersistenceException {
        boolean parmValueProcessed = false;
        for (QueryParameterValue value : queryParm.getValues()) {
             // If multiple values are present, we need to OR them together.
            if (parmValueProcessed) {
                whereClause.or();
            } else {
                parmValueProcessed = true;
            }

            // Determine code system case-sensitivity
            boolean codeSystemIsCaseSensitive = false;
            if (value.getValueSystem() != null && !value.getValueSystem().isEmpty()) {

                // Normalize code if code system is not case-sensitive. Otherwise leave code as is.
                codeSystemIsCaseSensitive = CodeSystemSupport.isCaseSensitive(value.getValueSystem());
                final String searchValue = SqlParameterEncoder.encode(codeSystemIsCaseSensitive ?
                        value.getValueCode() : SearchHelper.normalizeForSearch(value.getValueCode()));

                // We have a code-system and a code so we must have a common_token_value if the tuple exists
                Long commonTokenValueId = getCommonTokenValueId(value.getValueSystem(), searchValue);
                whereClause.col(paramAlias, COMMON_TOKEN_VALUE_ID).eq(nullCheck(commonTokenValueId)); // use literal
            } else {
                // No code system specified, search against both normalized code and unmodified code.
                // Build equivalent of: pX.token_value IN (search-attribute-value, normalized-search-sttribute-value)
                final String normalizedValue = SearchHelper.normalizeForSearch(value.getValueCode());
                Set<Long> ctvs = new HashSet<>();
                fetchCommonTokenValues(ctvs, value.getValueCode());
                fetchCommonTokenValues(ctvs, normalizedValue);
                addCommonTokenValueIdFilter(whereClause, paramAlias, ctvs);
            }
        }
    }

    /**
     * Use -1 as a simple substitute for null literal ids because we know -1 will never exist
     * as a value in the database (for fields populated by sequence values).
     * @param value
     * @return
     */
    protected int nullCheck(Integer value) {
        return value == null ? -1 : value;
    }

    /**
     * Use -1 as a simple substitute for null literal ids because we know -1 will never exist
     * as a value in the database (for fields populated by sequence values).
     * @param value
     * @return
     */
    protected long nullCheck(Long value) {
        return value == null ? -1L : value;
    }

    /**
     * Get the operator we need to use for matching values for this parameter
     * @param queryParameter
     * @return
     */
    protected Operator getOperator(QueryParameter queryParameter) {
        return OperatorUtil.getOperator(queryParameter);
    }

    /**
     * Map the Modifier in the passed Parameter to a supported query operator. If
     * the mapping results in the default
     * operator, override the default operator with the passed operator if the
     * passed operator is not null.
     *
     * @param queryParm
     *                        - A valid query Parameter.
     * @param defaultOverride
     *                        - An operator that should override the default
     *                        operator.
     * @return A supported operator.
     */
    protected Operator getOperator(QueryParameter queryParm, String defaultOverride) {
        return OperatorUtil.getOperator(queryParm, defaultOverride);
    }

    /**
     * Get the filter predicate expression for the given query parameter taking into account its type,
     * modifiers etc.
     * @param paramTableAlias
     * @param queryParm
     * @return a valid expression
     */
    protected WhereFragment paramFilter(QueryParameter queryParm, String paramTableAlias) throws FHIRPersistenceException {
        final WhereFragment result;

        switch (queryParm.getType()) {
        case URI:
        case STRING:
            result = getStringFilter(queryParm, paramTableAlias);
            break;
        case NUMBER:
            result = getNumberFilter(queryParm, paramTableAlias);
            break;
        case QUANTITY:
            result = getQuantityFilter(queryParm, paramTableAlias);
            break;
        case DATE:
            result = getDateFilter(queryParm, paramTableAlias);
            break;
        case SPECIAL:
            result = getLocationFilter(queryParm, paramTableAlias);
            break;
        case REFERENCE:
            result = getReferenceFilter(queryParm, paramTableAlias);
            break;
        case TOKEN:
            result = getTokenFilter(queryParm, paramTableAlias);
            break;
        default:
            result = null;
            break;
        }

        if (result == null) {
            throw new FHIRPersistenceException("Nested composite parameters are not supported");
        }

        return result;
    }

    @Override
    public QueryData addLocationPosition(QueryData queryData, List<QueryParameter> queryParameters) throws FHIRPersistenceException {
        // Special handling for location position extension logic
        NearLocationHandler handler = new NearLocationHandler();
        List<Bounding> boundingAreas;
        try {
            boundingAreas = handler.generateLocationPositionsFromParameters(queryParameters);
        } catch (FHIRSearchException e) {
            throw new FHIRPersistenceException("input parameter is invalid bounding area, bad prefix, or bad units", e);
        }

        if (!boundingAreas.isEmpty()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("buildLocationQuerySegment no longer needed");
            }
        }

        return null;
    }

    @Override
    public QueryData addInclusionParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {

        QueryParameter currentParm = queryParm;
        if (queryParm.getNextParameter() == null) {
            // just a single inclusion parameter, so we can optimize and treat as a simple join
            // to the main parameter filter block
            addFilter(queryData, resourceType, queryParm);
        } else {
            // Attach a series of exists clauses to the parameter query block
            final WhereAdapter where = queryData.getQuery().from().where();
            where.and().leftParen();
            while (currentParm != null) {
                // Add an exists clause for the given parameter
                WhereFragment filter = getFilterPredicate(queryData, queryParm);
                where.filter(filter.getExpression());

                currentParm = currentParm.getNextParameter();
                if (currentParm != null) {
                    where.or();
                }
            }
            where.rightParen();
        }
        return queryData;
    }

    @Override
    public QueryData addIncludeFilter(QueryData queryData, InclusionParameter inclusionParm, List<Long> logicalResourceIds) throws FHIRPersistenceException {
        // Build the entire join for the include query (everything after the FROM)
        // Versioned reference support. From the spec:
        // > If a resource has a reference that is versioned and _include is performed,
        // > the specified version SHOULD be provided.
        /*
SELECT R0.RESOURCE_ID, R0.LOGICAL_RESOURCE_ID, R0.VERSION_ID, R0.LAST_UPDATED, R0.IS_DELETED, R0.DATA, R0.RESOURCE_PAYLOAD_KEY, LR0.LOGICAL_ID
        FROM fhirdata.ExplanationOfBenefit_REF_VALUES AS P1
  INNER JOIN fhirdata.Claim_LOGICAL_RESOURCES AS LR0
          ON LR0.LOGICAL_RESOURCE_ID = P1.REF_LOGICAL_RESOURCE_ID
         AND P1.PARAMETER_NAME_ID = 9263
         AND P1.LOGICAL_RESOURCE_ID IN (135010606,135010540,135010498,135010412,135010428)
  INNER JOIN fhirdata.Claim_RESOURCES AS R0
          ON LR0.LOGICAL_RESOURCE_ID = R0.LOGICAL_RESOURCE_ID
         AND COALESCE(P1.REF_VERSION_ID,LR0.VERSION_ID) = R0.VERSION_ID
         AND R0.IS_DELETED = 'N'
         *
         */

        final String joinResourceType = inclusionParm.getJoinResourceType();
        final String targetResourceType = inclusionParm.getSearchParameterTargetType();
        final int aliasIndex = getNextAliasIndex();
        final String xxLogicalResources = targetResourceType + "_LOGICAL_RESOURCES";
        final String xxResources = targetResourceType + "_RESOURCES";
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = "LR0";
        final String rAlias = "R0";

        SelectAdapter select = queryData.getQuery();
        if (inclusionParm.isCanonical()) {
            final String joinStrValues = joinResourceType + "_STR_VALUES";
            final String targetStrValues = targetResourceType + "_STR_VALUES";
            final String nextParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus1ParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus2ParamAlias = getParamAlias(getNextAliasIndex());
            final String sourceUriCode = SearchHelper.makeCompositeSubCode(inclusionParm.getSearchParameter() +
                CANONICAL_SUFFIX, CANONICAL_COMPONENT_URI);
            final String sourceVersionCode = SearchHelper.makeCompositeSubCode(inclusionParm.getSearchParameter() +
                CANONICAL_SUFFIX, CANONICAL_COMPONENT_VERSION);
            final String targetUriCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_URI);
            final String targetVersionCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_VERSION);
            select.from(joinStrValues, alias(paramAlias))
                .innerJoin(targetStrValues, alias(nextParamAlias),
                    on(nextParamAlias, "STR_VALUE").eq(paramAlias, "STR_VALUE")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceUriCode))
                    .and(nextParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetUriCode))
                    .and(paramAlias, "LOGICAL_RESOURCE_ID").inLiteralLong(logicalResourceIds))
                .innerJoin(joinStrValues, alias(nextPlus1ParamAlias),
                    on(nextPlus1ParamAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus1ParamAlias, "COMPOSITE_ID").eq(paramAlias, "COMPOSITE_ID")
                    .and(nextPlus1ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceVersionCode)))
                .innerJoin(targetStrValues, alias(nextPlus2ParamAlias),
                    on(nextPlus2ParamAlias, "LOGICAL_RESOURCE_ID").eq(nextParamAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus2ParamAlias, "COMPOSITE_ID").eq(nextParamAlias, "COMPOSITE_ID")
                    .and(nextPlus2ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetVersionCode))
                    .and()
                    .leftParen().col(nextPlus1ParamAlias, "STR_VALUE").isNull()
                    .or().col(nextPlus2ParamAlias, "STR_VALUE").eq().col(nextPlus1ParamAlias, "STR_VALUE")
                    .rightParen())
                .innerJoin(xxLogicalResources, alias(lrAlias),
                    on(nextParamAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID"))
                .innerJoin(xxResources, alias(rAlias),
                    on(lrAlias, "LOGICAL_RESOURCE_ID").eq(rAlias, "LOGICAL_RESOURCE_ID")
                    .and(lrAlias, "VERSION_ID").eq(rAlias, "VERSION_ID")
                    .and(rAlias, IS_DELETED).eq().literal("N"));
        } else {
            final String tokenValues = joinResourceType + "_REF_VALUES";
            select.from(tokenValues, alias(paramAlias))
                .innerJoin(xxLogicalResources, alias(lrAlias),
                    on(lrAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "REF_LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(inclusionParm.getSearchParameter()))
                    .and(paramAlias, "LOGICAL_RESOURCE_ID").inLiteralLong(logicalResourceIds))
                .innerJoin(xxResources, alias(rAlias),
                    on(lrAlias, "LOGICAL_RESOURCE_ID").eq(rAlias, "LOGICAL_RESOURCE_ID")
                    .and().coalesce(col(paramAlias, "REF_VERSION_ID"), col(lrAlias, "VERSION_ID")).eq(rAlias, "VERSION_ID")
                    .and(rAlias, IS_DELETED).eq().literal("N"));
        }

        return queryData;
    }

    @Override
    public QueryData addRevIncludeFilter(QueryData queryData, InclusionParameter inclusionParm, List<Long> logicalResourceIds) throws FHIRPersistenceException {
        /*  old query
        *   EXISTS (SELECT 1 FROM
        *     (
        *       SELECT
        *         LOGICAL_ID, VERSION_ID
        *       FROM
        *         <targetResourceType>_LOGICAL_RESOURCES LR
        *       WHERE
        *         LR.LOGICAL_RESOURCE_ID IN (<list-of-logical-resource_ids>)
        *     ) REFS
        *     JOIN <joinResourceType>_TOKEN_VALUES_V P1
        *       ON REFS.LOGICAL_ID = P1.TOKEN_VALUE
        *      AND COALESCE(P1.REF_VERSION_ID, REFS.VERSION_ID) = REFS.VERSION_ID
        *      AND P1.PARAMETER_NAME_ID = {n}
        *      AND P1.CODE_SYSTEM_ID = {n}
        *     JOIN <targetResourceType>_LOGICAL_RESOURCES LR
        *       ON P1.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID
        *      AND LR.IS_DELETED = 'N'
        */
        final String joinResourceType = inclusionParm.getJoinResourceType();
        final String targetResourceType = inclusionParm.getSearchParameterTargetType();
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String targetLR = targetResourceType + "_LOGICAL_RESOURCES";
        final String parentLR = joinResourceType +"_LOGICAL_RESOURCES";
        final String parentR = joinResourceType + "_RESOURCES";
        final String paramAlias = getParamAlias(aliasIndex);
        final String parentLRAlias = "LR0";
        final String rAlias = "R0";
        final String lrAlias = getLRAlias(aliasIndex);

        // parentLR <- token_values <- logical_resources IN (123,456)
        query.from(parentLR, alias(parentLRAlias));
        if (inclusionParm.isCanonical()) {
            final String joinStrValues = joinResourceType + "_STR_VALUES";
            final String targetStrValues = targetResourceType + "_STR_VALUES";
            final String nextParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus1ParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus2ParamAlias = getParamAlias(getNextAliasIndex());
            final String sourceUriCode = SearchHelper.makeCompositeSubCode(inclusionParm.getSearchParameter() +
                CANONICAL_SUFFIX, CANONICAL_COMPONENT_URI);
            final String sourceVersionCode = SearchHelper.makeCompositeSubCode(inclusionParm.getSearchParameter() +
                CANONICAL_SUFFIX, CANONICAL_COMPONENT_VERSION);
            final String targetUriCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_URI);
            final String targetVersionCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_VERSION);
            query.from()
                .innerJoin(joinStrValues, alias(paramAlias),
                    on(parentLRAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceUriCode)))
                .innerJoin(targetStrValues, alias(nextParamAlias),
                    on(nextParamAlias, "STR_VALUE").eq(paramAlias, "STR_VALUE")
                    .and(nextParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetUriCode))
                    .and(nextParamAlias, "LOGICAL_RESOURCE_ID").inLiteralLong(logicalResourceIds))
                .innerJoin(targetStrValues, alias(nextPlus1ParamAlias),
                    on(nextPlus1ParamAlias, "LOGICAL_RESOURCE_ID").eq(nextParamAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus1ParamAlias, "COMPOSITE_ID").eq(nextParamAlias, "COMPOSITE_ID")
                    .and(nextPlus1ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetVersionCode)))
                .innerJoin(joinStrValues, alias(nextPlus2ParamAlias),
                    on(nextPlus2ParamAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus2ParamAlias, "COMPOSITE_ID").eq(paramAlias, "COMPOSITE_ID")
                    .and(nextPlus2ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceVersionCode))
                    .and()
                    .leftParen().col(nextPlus2ParamAlias, "STR_VALUE").isNull()
                    .or().col(nextPlus2ParamAlias, "STR_VALUE").eq().col(nextPlus1ParamAlias, "STR_VALUE")
                    .rightParen());
        } else {
            final String tokenValues = joinResourceType + "_REF_VALUES";
            query.from()
                .innerJoin(tokenValues, alias(paramAlias),
                    on(parentLRAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(inclusionParm.getSearchParameter())))
                .innerJoin(targetLR, alias(lrAlias),
                    on(lrAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "REF_LOGICAL_RESOURCE_ID")
                    .and().coalesce(col(paramAlias, "REF_VERSION_ID"), col(lrAlias, "VERSION_ID")).eq(lrAlias, "VERSION_ID")
                    .and(lrAlias, "LOGICAL_RESOURCE_ID").inLiteralLong(logicalResourceIds));
        }
        query.from().innerJoin(parentR, alias(rAlias), on(parentLRAlias, "CURRENT_RESOURCE_ID").eq(rAlias, "RESOURCE_ID"));

        return queryData;
    }

    @Override
    public QueryData addWholeSystemDataFilter(QueryData queryData, String resourceType, List<Long> logicalResourceIds) throws FHIRPersistenceException {
        // Build the IN clause for the logical resource IDs.
        SelectAdapter select = queryData.getQuery();
        select.from().where().and("LR", "LOGICAL_RESOURCE_ID").inLiteralLong(logicalResourceIds);

        return queryData;
    }

    @Override
    public QueryData addWholeSystemResourceTypeFilter(QueryData queryData, List<Integer> resourceTypeIds) throws FHIRPersistenceException {
        // Build the IN clause for the resource type IDs.
        List<Long> longResourceTypeIds = resourceTypeIds.stream().map(i -> i.longValue()).collect(Collectors.toList());
        SelectAdapter select = queryData.getQuery();
        select.from().where().and("LR0", "RESOURCE_TYPE_ID").inLiteralLong(longResourceTypeIds);

        return queryData;
    }

    @Override
    public QueryData addTokenParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        // Add a join to the query. The NOT/NOT_IN modifiers are trickier because
        // they need to be handled as a NOT EXISTS clause.
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias(); // join to LR at the same query level
        final ExpNode filter;
        filter = getTokenFilter(queryParm, paramAlias).getExpression();
        // which table we join against depends on the fields used by the filter expression
        boolean isFilterOptimized = isOptimizedTokenParamFilter(filter, paramAlias);
        final String xxTokenValues = getTokenParamTable(filter, resourceType, paramAlias, isFilterOptimized);

        String parameterName = queryParm.getCode();
        // Append the suffix for :text modifier
        if (Modifier.TEXT.equals(queryParm.getModifier())) {
            parameterName += SearchConstants.TEXT_MODIFIER_SUFFIX;
        }

        if (queryParm.getModifier() == Modifier.NOT || queryParm.getModifier() == Modifier.NOT_IN) {
            // Use a nested NOT EXISTS (...) instead of a simple join
            if (schemaType == SchemaType.DISTRIBUTED && !isFilterOptimized) {
                // remember that the common token value filter happens within the WITH subquery
                final String withAlias = addDistributedWithCommonTokenValue(query, filter, paramAlias);
                SelectAdapter exists = Select.select("1");
                exists.from(xxTokenValues, alias(paramAlias))
                    .from(withAlias, alias(withAlias))
                    .where(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID") // correlate with the main query
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
                    .and(withAlias, "COMMON_TOKEN_VALUE_ID").eq(paramAlias, "COMMON_TOKEN_VALUE_ID")
                    ;
    
                query.from().where().and().notExists(exists.build());
            } else {
                SelectAdapter exists = Select.select("1");
                exists.from(xxTokenValues, alias(paramAlias))
                    .where(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID") // correlate with the main query
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName));
    
                // add the filter predicate to the exists where clause
                exists.from().where().and(filter);
                query.from().where().and().notExists(exists.build());
            }
        } else {
            // Attach the parameter table to the single parameter exists join
            if (schemaType == SchemaType.DISTRIBUTED && !isFilterOptimized) {
                // remember that the common token value filter happens within the WITH subquery
                final String withAlias = addDistributedWithCommonTokenValue(query, filter, paramAlias);
                query.from().innerJoin(xxTokenValues, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
                    )
                .innerJoin(withAlias, alias(withAlias), on(withAlias, "COMMON_TOKEN_VALUE_ID").eq(paramAlias, "COMMON_TOKEN_VALUE_ID"));
            } else {
                query.from().innerJoin(xxTokenValues, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
                    .and(filter));
            }
        }

        // We're not changing the level, so we return the same queryData we were given
        return queryData;
    }

    @Override
    public QueryData addTagParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        return addGlobalTokenParam(queryData, queryParm,
            Modifier.TEXT.equals(queryParm.getModifier()) ? "RESOURCE_TOKEN_REFS" :
                isWholeSystemSearch(resourceType) ? "LOGICAL_RESOURCE_TAGS" : resourceType + "_TAGS");
    }

    @Override
    public QueryData addSecurityParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        return addGlobalTokenParam(queryData, queryParm,
            Modifier.TEXT.equals(queryParm.getModifier()) ? "RESOURCE_TOKEN_REFS" :
                isWholeSystemSearch(resourceType) ? "LOGICAL_RESOURCE_SECURITY" : resourceType + "_SECURITY");
    }

    private QueryData addGlobalTokenParam(QueryData queryData, QueryParameter queryParm, String parameterTable) throws FHIRPersistenceException {
        // Add a join to the query. The NOT/NOT_IN modifiers are trickier because
        // they need to be handled as a NOT EXISTS clause.
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias(); // join to LR at the same query level

        // Global tokens are stored in their own parameter table and therefore don't need a
        // parameter_name_id but otherwise are just like any other token.
        final ExpNode filter = getTokenFilter(queryParm, paramAlias).getExpression();

        boolean tokenValueSearch = false;
        String tokenValuesAlias = paramAlias;
        if (Modifier.IN.equals(queryParm.getModifier()) || Modifier.NOT_IN.equals(queryParm.getModifier()) ||
                Modifier.ABOVE.equals(queryParm.getModifier()) || Modifier.BELOW.equals(queryParm.getModifier()) ||
                Modifier.TEXT.equals(queryParm.getModifier())) {
            // For a search against a global search parameter where we need access to the search parameter value, we
            // have to join the common token values table (which contains the parameter value) to the parameter table,
            // which only contains a common_token_value_id.
            // Since the filter is using paramAlias and the filter will get ANDed to the common token values join, we
            // need to use paramAlias as the common token values alias and generate a new alias for the parameter table.
            tokenValuesAlias = getParamAlias(getNextAliasIndex());
            tokenValueSearch = true;
        } else {
            ColumnExpNodeVisitor visitor = new ColumnExpNodeVisitor(); // gathers all columns used in the filter expression
            Set<String> columns = filter.visit(visitor);
            if (columns.contains(DataDefinitionUtil.getQualifiedName(paramAlias, CODE_SYSTEM_ID)) ||
                    columns.contains(DataDefinitionUtil.getQualifiedName(paramAlias, TOKEN_VALUE))) {
                tokenValuesAlias = getParamAlias(getNextAliasIndex());
                tokenValueSearch = true;
            }
        }

        if (Modifier.NOT.equals(queryParm.getModifier()) || Modifier.NOT_IN.equals(queryParm.getModifier())) {
            // Use a nested NOT EXISTS (...) instead of a simple join
            SelectAdapter exists = Select.select("1");
            exists.from(parameterTable, alias(tokenValuesAlias))
                .where(tokenValuesAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID"); // correlate with the main query
            if (tokenValueSearch) {
                // Join to common token values table and add filter predicate to the common token values join on clause
                exists.from().innerJoin("COMMON_TOKEN_VALUES", alias(paramAlias), on(paramAlias, "COMMON_TOKEN_VALUE_ID")
                    .eq(tokenValuesAlias, "COMMON_TOKEN_VALUE_ID")
                    .and(filter));
            } else {
                // Add the filter predicate to the exists where clause
                exists.from().where().and(filter);
            }
            // Add as a not exists to the main query
            query.from().where().and().notExists(exists.build());
        } else {
            if (tokenValueSearch) {
                if (Modifier.TEXT.equals(queryParm.getModifier())) {
                    query.from().where().and(tokenValuesAlias, "PARAMETER_NAME_ID")
                        .eq(getParameterNameId(queryParm.getCode() + SearchConstants.TEXT_MODIFIER_SUFFIX));
                }
                // Join the xx_TAGS table and the common token values table to the exists
                query.from().innerJoin(parameterTable, alias(tokenValuesAlias), on(tokenValuesAlias, "LOGICAL_RESOURCE_ID")
                    .eq(lrAlias, "LOGICAL_RESOURCE_ID"));
                query.from().innerJoin("COMMON_TOKEN_VALUES", alias(paramAlias), on(paramAlias, "COMMON_TOKEN_VALUE_ID")
                    .eq(tokenValuesAlias, "COMMON_TOKEN_VALUE_ID")
                    .and(filter));
            } else {
                // Attach the parameter table to the single parameter exists join
                query.from().innerJoin(parameterTable, alias(tokenValuesAlias), on(tokenValuesAlias, "LOGICAL_RESOURCE_ID")
                    .eq(lrAlias, "LOGICAL_RESOURCE_ID")
                    .and(filter));
            }
        }

        // We're not changing the level, so we return the same queryData we were given
        return queryData;
    }

    @Override
    public QueryData addStringParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        // Join to the string parameter table
        // Attach an exists clause to filter the result based on the string query parameter definition
        final int aliasIndex = getNextAliasIndex();
        final String lrAlias = queryData.getLRAlias();
        final String paramTableName;
        if (isWholeSystemSearch(resourceType)) {
            paramTableName = "STR_VALUES";
        } else {
            paramTableName = resourceType + "_STR_VALUES";
        }
        final String paramAlias = getParamAlias(aliasIndex);
        final String parameterName = queryParm.getCode();

        // Add the (non-trivial) filter predicate for string parameters
        ExpNode filter = getStringFilter(queryParm, paramAlias).getExpression();

        SelectAdapter query = queryData.getQuery();
        query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
            .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
            .and(filter));

        return queryData;
    }

    @Override
    public QueryData addCanonicalParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        String code = queryParm.getCode();
        if (PROFILE.equals(code)) {
            return addProfileParam(queryData, resourceType, queryParm);
        }

        // Convert all values into CanonicalValues
        boolean versionNotSpecified = true;
        List<CanonicalValue> canonicalValues = new ArrayList<>();
        for (QueryParameterValue parmValue : queryParm.getValues()) {
            CanonicalValue canonicalValue = CanonicalSupport.createCanonicalValueFrom(parmValue.getValueString());
            if (canonicalValue.getVersion() != null) {
                versionNotSpecified = false;
            }
            canonicalValues.add(canonicalValue);
        }

        if ((versionNotSpecified && URL.equals(code))) {
            // If no version specified and 'url' search parameter, we can process as a normal string parameter
            return addStringParam(queryData, resourceType, queryParm);
        } else {
            // Process as a composite parameter
            String compositeCode = code + CANONICAL_SUFFIX;
            QueryParameter compositeParameter = new QueryParameter(Type.COMPOSITE, compositeCode, null, null);

            // For each value in the original query parameter, build a value in the composite parameter
            for (CanonicalValue canonicalValue : canonicalValues) {
                QueryParameterValue compositeValue = new QueryParameterValue();

                // Build query parameter for the uri value
                QueryParameterValue uriParameterValue = new QueryParameterValue();
                uriParameterValue.setValueString(canonicalValue.getUri());
                QueryParameter uriParameter = new QueryParameter(Type.URI,
                    SearchHelper.makeCompositeSubCode(compositeCode, CANONICAL_COMPONENT_URI),
                    null, null, Collections.singletonList(uriParameterValue));
                compositeValue.addComponent(uriParameter);

                // Build query parameter for the version value if specified
                if (canonicalValue.getVersion() != null) {
                    QueryParameterValue versionParameterValue = new QueryParameterValue();
                    versionParameterValue.setValueString(canonicalValue.getVersion());
                    QueryParameter versionParameter = new QueryParameter(Type.URI,
                        SearchHelper.makeCompositeSubCode(compositeCode, CANONICAL_COMPONENT_VERSION),
                        null, null, Collections.singletonList(versionParameterValue));
                    compositeValue.addComponent(versionParameter);
                }

                compositeParameter.getValues().add(compositeValue);
            }

            // Now process as a composite parameter
            return addCompositeParam(queryData, compositeParameter);
        }
    }

    private QueryData addProfileParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        // Join to the canonical parameter table...which in this case means the xx_profiles table
        final int aliasIndex = getNextAliasIndex();
        final String lrAlias = queryData.getLRAlias();
        final String paramTableName;
        if (isWholeSystemSearch(resourceType)) {
            paramTableName = "LOGICAL_RESOURCE_PROFILES";
        } else {
            paramTableName = resourceType + "_PROFILES";
        }
        final String paramAlias = getParamAlias(aliasIndex);

        // Build the filter predicate for the canonical values, handling the parse
        WhereFragment whereFragment = new WhereFragment();
        whereFragment.leftParen();

        boolean multiple = false;
        for (QueryParameterValue value : queryParm.getValues()) {
            // Concatenate multiple matches with an OR
            if (multiple) {
                whereFragment.or();
            } else {
                multiple = true;
            }

            // Reuse the same CanonicalSupport code used for param extraction to parse the search value
            ResourceProfileRec rpc = CanonicalSupport.makeResourceProfileRec(null, resourceType, -1, -1, value.getValueString(), false);
            long canonicalId = getCanonicalId(rpc.getCanonicalValue());
            whereFragment.col(paramAlias, "CANONICAL_ID").eq(canonicalId);

            // TODO double-check semantics of ABOVE and BELOW in this context
            if (rpc.getVersion() != null && !rpc.getVersion().isEmpty()) {
                if (queryParm.getModifier() == Modifier.ABOVE) {
                    whereFragment.and(paramAlias, "VERSION").gte().bind(rpc.getVersion());
                } else if (queryParm.getModifier() == Modifier.BELOW) {
                    whereFragment.and(paramAlias, "VERSION").lt().bind(rpc.getVersion());
                } else {
                    whereFragment.and(paramAlias, "VERSION").eq().bind(rpc.getVersion());
                }
            }

            if (rpc.getFragment() != null && !rpc.getFragment().isEmpty()) {
                whereFragment.and(paramAlias, "FRAGMENT").eq().bind(rpc.getFragment());
            }
        }

        whereFragment.rightParen();

        SelectAdapter query = queryData.getQuery();
        query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
            .and(whereFragment.getExpression()));

        return queryData;
    }

    @Override
    public QueryData addMissingParam(QueryData queryData, QueryParameter queryParm, boolean isMissing) throws FHIRPersistenceException {
        // note that there's no filter here to look for a specific value. We simply want to know
        // whether or not the parameter exists for a given resource
        final String parameterName = queryParm.getCode();
        final int parameterNameId = getParameterNameId(parameterName);
        final int aliasIndex = getNextAliasIndex();
        final String resourceType = queryData.getResourceType();
        final String paramTableName = paramValuesTableName(resourceType, queryParm);
        final String lrAlias = queryData.getLRAlias();
        final String paramAlias = getParamAlias(aliasIndex);

        SelectAdapter exists = Select.select("1");
        exists.from(paramTableName, alias(paramAlias))
                .where(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID"); // correlate with the main query

        // Do not need PARAMETER_NAME_ID clause for _profile, _tag, or _security parameters since they have
        // their own tables.
        if (this.legacyWholeSystemSearchParamsEnabled ||
                (!PROFILE.equals(parameterName) && !SECURITY.equals(parameterName) && !TAG.equals(parameterName))) {
            exists.from().where().and(paramAlias, PARAMETER_NAME_ID).eq(parameterNameId);
        }

        // Add the exists to the where clause of the main query which already has a predicate
        // so we need to AND the exists
        SelectAdapter query = queryData.getQuery();
        if (isMissing) {
            // parameter should be missing, i.e. not exist
            query.from().where().and().notExists(exists.build());
        } else {
            // parameter should be not missing...i.e. it exists
            query.from().where().and().exists(exists.build());
        }
        return queryData;
    }

    @Override
    public QueryData addChained(QueryData queryData, QueryParameter currentParm) throws FHIRPersistenceException {
        logger.entering(CLASSNAME, "addChained");
        // In this variant, each chained element is added as join to the current statement. We still need
        // to add the EXISTS clause when depth == 0 (the first element in the chain)

        // Because logical_resource_id is already unique across all resources, we don't need to constrain
        // with resource_type_id.
        // AND EXISTS (SELECT 1
        //               FROM fhirdata.Observation_REF_VALUES AS P1                 -- Observation references to
        //         INNER JOIN fhirdata.Device_LOGICAL_RESOURCES AS LR1              -- Device
        //                 ON LR1.LOGICAL_RESOURCE_ID = P1.REF_LOGICAL_RESOURCE_ID  -- Device.LOGICAL_RESOURCE_ID = Observation.device
        //                AND P1.PARAMETER_NAME_ID = 1234                           -- Observation.device reference param
        //                AND LR1.IS_DELETED = 'N'                                  -- referenced Device is not deleted
        //              WHERE P1.LOGICAL_RESOURCE_ID = LR0.LOGICAL_RESOURCE_ID      -- correlate parameter to parent

        final String sourceResourceType = queryData.getResourceType();
        final SelectAdapter currentSubQuery = queryData.getQuery();
        final int aliasIndex = getNextAliasIndex();
        final String targetResourceType = currentParm.getModifierResourceTypeName();
        final String xxLogicalResources = targetResourceType + "_LOGICAL_RESOURCES";
        final String lrAlias = getLRAlias(aliasIndex);
        String paramAlias = getParamAlias(aliasIndex);

        // Add this chain element as a join to the current query. For forward chaining,
        // we need to join logical-resources and token-values
        if (currentParm.isCanonical()) {
            // Chain via the 'url' and 'version' search parameter values
            final String sourceStrValues = sourceResourceType + "_STR_VALUES";
            final String targetStrValues = targetResourceType + "_STR_VALUES";
            final String nextParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus1ParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus2ParamAlias = getParamAlias(getNextAliasIndex());
            final String sourceUriCode = SearchHelper.makeCompositeSubCode(currentParm.getCode() + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_URI);
            final String sourceVersionCode = SearchHelper.makeCompositeSubCode(currentParm.getCode() + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_VERSION);
            final String targetUriCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_URI);
            final String targetVersionCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_VERSION);
            currentSubQuery.from()
                .innerJoin(sourceStrValues, alias(paramAlias),
                    on(paramAlias, "LOGICAL_RESOURCE_ID").eq(queryData.getLRAlias(), "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceUriCode)))
                .innerJoin(targetStrValues, alias(nextParamAlias),
                    on(nextParamAlias, "STR_VALUE").eq(paramAlias, "STR_VALUE")
                    .and(nextParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetUriCode)))
                .innerJoin(sourceStrValues, alias(nextPlus1ParamAlias),
                    on(nextPlus1ParamAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus1ParamAlias, "COMPOSITE_ID").eq(paramAlias, "COMPOSITE_ID")
                    .and(nextPlus1ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceVersionCode)))
                .innerJoin(targetStrValues, alias(nextPlus2ParamAlias),
                    on(nextPlus2ParamAlias, "LOGICAL_RESOURCE_ID").eq(nextParamAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus2ParamAlias, "COMPOSITE_ID").eq(nextParamAlias, "COMPOSITE_ID")
                    .and(nextPlus2ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetVersionCode))
                    .and()
                    .leftParen().col(nextPlus1ParamAlias, "STR_VALUE").isNull()
                    .or().col(nextPlus2ParamAlias, "STR_VALUE").eq().col(nextPlus1ParamAlias, "STR_VALUE")
                    .rightParen())
                .innerJoin(xxLogicalResources, alias(lrAlias),
                    on(lrAlias, "LOGICAL_RESOURCE_ID").eq(nextPlus2ParamAlias, "LOGICAL_RESOURCE_ID")
                    .and(lrAlias, "IS_DELETED").eq().literal("N"));
            paramAlias = nextPlus2ParamAlias;
        } else {
            // Chain via the logical ID
            final String refValues = sourceResourceType + "_REF_VALUES";
            currentSubQuery.from()
                .innerJoin(refValues, alias(paramAlias),
                    on(paramAlias, "LOGICAL_RESOURCE_ID").eq(queryData.getLRAlias(), "LOGICAL_RESOURCE_ID"))
                .innerJoin(xxLogicalResources, alias(lrAlias),
                    on(lrAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "REF_LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(currentParm.getCode()))
                    .and(lrAlias, "IS_DELETED").eq().literal("N"));
        }

        logger.exiting(CLASSNAME, "addChained");
        // Return details of the aliases needed for future chain elements
        return new QueryData(currentSubQuery, lrAlias, paramAlias, targetResourceType, queryData.getChainDepth()+1);
    }

    @Override
    public void addFilter(QueryData queryData, String resourceType, QueryParameter currentParm) throws FHIRPersistenceException {
        // A variant where we just use a simple join instead of an exists (sub-select) to implement
        // the parameter filter.
        logger.fine("chainDepth: " + queryData.getChainDepth());
        final SelectAdapter currentSubQuery = queryData.getQuery();
        final String code = currentParm.getCode();
        final String lrAlias = queryData.getLRAlias();

        if (ID.equals(code)) {
            addIdFilter(queryData, resourceType, currentParm);
        } else if (LAST_UPDATED.equals(code)) {
            // Compute the _lastUpdated filter predicate for the given query parameter
            NewLastUpdatedParmBehaviorUtil util = new NewLastUpdatedParmBehaviorUtil(lrAlias);
            WhereFragment filter = new WhereFragment();
            util.executeBehavior(filter, currentParm);

            // Add the filter predicate to the where clause of the base query
            currentSubQuery.from().where().and(filter.getExpression());
        } else {
            // A simple filter added as an exists clause to the current query
            // AND EXISTS (SELECT 1
            //               FROM fhirdata.Patient_STR_VALUES AS P3                 -- 'Patient string parameters'
            //              WHERE P3.LOGICAL_RESOURCE_ID = LR2.LOGICAL_RESOURCE_ID  -- 'correlate to parent'
            //                AND P3.PARAMETER_NAME_ID = 123                        -- 'name parameter'
            //                AND P3.STR_VALUE = 'Jones')                           -- 'name filter'
            final int aliasIndex = getNextAliasIndex();
            final String paramAlias = getParamAlias(aliasIndex);
            WhereFragment pf = paramFilter(currentParm, paramAlias);

            final String paramTable;
            final boolean isFilterOptimized;
            final ExpNode filter = pf.getExpression();
            if (Type.TOKEN.equals(currentParm.getType()) &&
                    !(TAG.equals(currentParm.getCode()) || SECURITY.equals(currentParm.getCode()))) {
                isFilterOptimized = isOptimizedTokenParamFilter(filter, paramAlias);
                paramTable = getTokenParamTable(filter, queryData.getResourceType(), paramAlias, isFilterOptimized);
            } else {
                paramTable = paramValuesTableName(queryData.getResourceType(), currentParm);
                isFilterOptimized = true;
            }

            if (Type.REFERENCE.equals(currentParm.getType())) {
                // V0027, reference filters now need to look at both xx_ref_values and xx_str_values
                // so we use a full correlated sub-query for exists/not-exists
                final String anchorAlias = "LR" + getNextAliasIndex();
                SelectAdapter exists = Select.select("1");
                exists.from("LOGICAL_RESOURCES", alias(anchorAlias))
                    .where(anchorAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID"); // correlate to parent query
                QueryData subQuery = new QueryData(exists, anchorAlias, null, resourceType, 0);
                addReferenceParam(subQuery, queryData.getResourceType(), currentParm);
                if (currentParm.getModifier() == Modifier.NOT) {
                    currentSubQuery.from().where().and().notExists(exists.build());
                } else {
                    currentSubQuery.from().where().and().exists(exists.build());
                }
            } else if (currentParm.getModifier() == Modifier.NOT) {
                // Needs to be handled as a NOT EXISTS correlated subquery
                SelectAdapter exists = Select.select("1");
                if (currentParm.getType() == Type.TOKEN && schemaType == SchemaType.DISTRIBUTED && !isFilterOptimized) {
                    // special case for Citus which needs a WITH clause to fetch rows from COMMON_TOKEN_VALUES
                    addDistributedCommonTokenValueFilter(exists, resourceType, code, lrAlias, filter, paramTable, paramAlias, null);
                } else {
                    exists.from(paramTable, alias(paramAlias))
                        .where(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID"); // correlate to parent query
                    if (this.legacyWholeSystemSearchParamsEnabled ||
                            (!PROFILE.equals(code) && !SECURITY.equals(code) && !TAG.equals(code))) {
                        exists.from().where().and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(currentParm.getCode()));
                    }
                    exists.from().where().and(filter);
                }

                // Add the sub-query as a NOT EXISTS filter to the main query
                currentSubQuery.from().where().and().notExists(exists.build());
            } else {
                // Filter the query by adding a join
                if (this.legacyWholeSystemSearchParamsEnabled ||
                        (!PROFILE.equals(code) && !SECURITY.equals(code) && !TAG.equals(code))) {
                    if (currentParm.getType() == Type.TOKEN && schemaType == SchemaType.DISTRIBUTED && !isFilterOptimized) {
                        // special case for Citus which needs a WITH clause to fetch rows from COMMON_TOKEN_VALUES
                        addDistributedCommonTokenValueFilter(currentSubQuery, resourceType, code, lrAlias, filter, paramTable, paramAlias, null);
                    } else {
                        currentSubQuery.from()
                            .innerJoin(paramTable, alias(paramAlias),
                                on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                                .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(currentParm.getCode()))
                                .and(filter));
                    }
                } else {
                    // join with one of xx_profiles, xx_tags or xx_security which get their own table
                    // and therefore do not have a parameter_name_id column
                    currentSubQuery.from()
                        .innerJoin(paramTable, alias(paramAlias),
                            on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                            .and(filter));
                }
            }
        }
    }

    @Override
    public QueryData addReverseChained(QueryData queryData, QueryParameter currentParm) throws FHIRPersistenceException {
        logger.entering(CLASSNAME, "addReverseChained");
        // For reverse chaining, we connect the token-value (reference)
        // back to the parent query LOGICAL_ID and an xx_LOGICAL_RESOURCES
        // to provide the LOGICAL_ID as the target for future chain elements
        
        // INNER JOIN fhirdata.Observation_REF_VALUES AS P1
        //        AND LR0.LOGICAL_RESOURCE_ID = P1.REF_LOGICAL_RESOURCE_ID   -- 'Patient.LOGICAL_ID = Observation.patient'
        //        AND LR0.VERSION_ID = COALESCE(P1.REF_VERSION_ID, LR0.VERSION_ID)
        //        AND P1.PARAMETER_NAME_ID = 1246       -- 'Observation.patient'
        // INNER JOIN fhirdata.Observation_LOGICAL_RESOURCES LR1
        //         ON LR1.LOGICAL_RESOURCE_ID = P1.LOGICAL_RESOURCE_ID

        final String refResourceType = queryData.getResourceType();
        final SelectAdapter currentSubQuery = queryData.getQuery();
        final int aliasIndex = getNextAliasIndex();
        final String resourceTypeName = currentParm.getModifierResourceTypeName();
        final String xxLogicalResources = resourceTypeName + "_LOGICAL_RESOURCES";
        final String lrAlias = getLRAlias(aliasIndex);
        final String lrPrevAlias = queryData.getLRAlias();
        String paramAlias = getParamAlias(aliasIndex);

        if (currentParm.isCanonical()) {
            final String strValues = resourceTypeName + "_STR_VALUES";
            final String refStrValues = refResourceType + "_STR_VALUES";
            final String nextParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus1ParamAlias = getParamAlias(getNextAliasIndex());
            final String nextPlus2ParamAlias = getParamAlias(getNextAliasIndex());
            final String sourceUriCode = SearchHelper.makeCompositeSubCode(currentParm.getCode() + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_URI);
            final String sourceVersionCode = SearchHelper.makeCompositeSubCode(currentParm.getCode() + SearchConstants.CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_VERSION);
            final String targetUriCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_URI);
            final String targetVersionCode = SearchHelper.makeCompositeSubCode(URL + CANONICAL_SUFFIX,
                CANONICAL_COMPONENT_VERSION);
            currentSubQuery.from()
                .innerJoin(refStrValues, alias(paramAlias),
                    on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrPrevAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetUriCode)))
                .innerJoin(strValues, alias(nextParamAlias),
                    on(nextParamAlias, "STR_VALUE").eq(paramAlias, "STR_VALUE")
                    .and(nextParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceUriCode)))
                .innerJoin(refStrValues, alias(nextPlus1ParamAlias),
                    on(nextPlus1ParamAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus1ParamAlias, "COMPOSITE_ID").eq(paramAlias, "COMPOSITE_ID")
                    .and(nextPlus1ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(targetVersionCode)))
                .innerJoin(strValues, alias(nextPlus2ParamAlias),
                    on(nextPlus2ParamAlias, "LOGICAL_RESOURCE_ID").eq(nextParamAlias, "LOGICAL_RESOURCE_ID")
                    .and(nextPlus2ParamAlias, "COMPOSITE_ID").eq(nextParamAlias, "COMPOSITE_ID")
                    .and(nextPlus2ParamAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(sourceVersionCode))
                    .and()
                    .leftParen().col(nextPlus2ParamAlias, "STR_VALUE").isNull()
                    .or().col(nextPlus2ParamAlias, "STR_VALUE").eq().col(nextPlus1ParamAlias, "STR_VALUE")
                    .rightParen());
            paramAlias = nextPlus2ParamAlias;
        } else {
            final String refValues = resourceTypeName + "_REF_VALUES";
            currentSubQuery.from()
                .innerJoin(refValues, alias(paramAlias),
                    on(lrPrevAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "REF_LOGICAL_RESOURCE_ID") // correlate with the main query
                    .and(lrPrevAlias, "VERSION_ID").eq().coalesce(col(paramAlias, "REF_VERSION_ID"), col(lrPrevAlias, "VERSION_ID"))
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(currentParm.getCode())));
        }
        currentSubQuery.from()
              .innerJoin(xxLogicalResources, alias(lrAlias),
                  on(lrAlias, "LOGICAL_RESOURCE_ID").eq(paramAlias, "LOGICAL_RESOURCE_ID")
                  .and(lrAlias, "IS_DELETED").eq().literal("N"));

        // Return a new QueryData with the aliases configured to use by the next element in the chain
        logger.exiting(CLASSNAME, "addReverseChained");
        return new QueryData(currentSubQuery, lrAlias, paramAlias, resourceTypeName, queryData.getChainDepth()+1);
    }


    @Override
    public QueryData addNumberParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        // Attach an exists clause to the query
        final String parameterName = queryParm.getCode();
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramTableName = resourceType + "_NUMBER_VALUES";
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias();

        ExpNode filter = getNumberFilter(queryParm, paramAlias).getExpression();

        query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
            .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
            .and(filter));

        return queryData;
    }

    @Override
    public QueryData addQuantityParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        final String parameterName = queryParm.getCode();
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramTableName = resourceType + "_QUANTITY_VALUES";
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias();

        ExpNode filter = getQuantityFilter(queryParm, paramAlias).getExpression();

        query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
            .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
            .and(filter));

        return queryData;
    }

    @Override
    public QueryData addDateParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        final String parameterName = queryParm.getCode();
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramTableName = resourceType + "_DATE_VALUES";
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias();
        ExpNode filter = getDateFilter(queryParm, paramAlias).getExpression();
        query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
            .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
            .and(filter));

        return queryData;
    }

    @Override
    public QueryData addLocationParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        final String parameterName = queryParm.getCode();
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramTableName = resourceType + "_LATLNG_VALUES";
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias();
        ExpNode filter = getLocationFilter(queryParm, paramAlias).getExpression();
        query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
            .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
            .and(filter));

        return queryData;
    }

    @Override
    public QueryData addReferenceParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {

        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias();
        final boolean isIdentifier = Modifier.IDENTIFIER.equals(queryParm.getModifier());
        final ExpNode filter;
        final String paramTableName;
        if (isIdentifier) {
            // Identifiers are tokens so we need to join with token_values.
            // Grab the filter expression first. We can then inspect the expression to
            // look for use of the TOKEN_VALUE column. If use of this column isn't found,
            // we can apply an optimization by joining against the RESOURCE_TOKEN_REFS
            // table directly.
            filter = getIdentifierFilter(queryParm, paramAlias).getExpression();
            boolean isFilterOptimized = isOptimizedTokenParamFilter(filter, paramAlias);
            paramTableName = getTokenParamTable(filter, resourceType, paramAlias, isFilterOptimized);
            String queryParmCode = queryParm.getCode();
            queryParmCode += SearchConstants.IDENTIFIER_MODIFIER_SUFFIX;
            if (schemaType == SchemaType.DISTRIBUTED && !isFilterOptimized) {
                // Special case for the Citus/distributed variant to collect all the common_token_value_id values
                // on the coordinator node which are then pushed down to the main query which is run on each
                // of the worker nodes
                addDistributedCommonTokenValueFilter(query, resourceType, queryParmCode, lrAlias, filter, paramTableName, paramAlias, null);                
            } else {
                query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(queryParmCode))
                    .and(filter));
            }
        } else {
            // For V0027 we need to handle parameters that may come from xx_ref_values or xx_str_values
            return processRealReferenceParam(queryData, resourceType, queryParm);
        }

        return queryData;
    }

    /**
     * Add the special token value filter we use with the DISTRIBUTED variant of the schema. Because we distribute
     * COMMON_TOKEN_VALUES using a different distribution column, we need to perform the token_value select/filter
     * in a WITH subquery which is then referenced in the main join. This method adds both the WITH clause and the
     * join.
     * @param query
     * @param resourceType
     * @param queryParmCode
     * @param lrAlias
     * @param filter
     * @param paramTableName the xx_resource_token_refs table name
     * @param paramAlias
     * @param firstCompositeTableAlias table alias to join composite_id, or null if not composite
     * @throws FHIRPersistenceException if the queryParmCode is not a valid parameter name
     */
    private void addDistributedCommonTokenValueFilter(SelectAdapter query, String resourceType, String queryParmCode, 
            String lrAlias, ExpNode filter, String paramTableName, String paramAlias, String firstCompositeTableAlias) throws FHIRPersistenceException {

        // Add the WITH subquery to the main select statement
        final String withAlias = addDistributedWithCommonTokenValue(query, filter, paramAlias);

        // Now we can use the withAlias as a source table in the main join
        if (firstCompositeTableAlias == null) {
            query.from()
                .innerJoin(paramTableName, alias(paramAlias), 
                    on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(queryParmCode)))
                .innerJoin(withAlias, alias(withAlias), 
                    on(paramAlias, "COMMON_TOKEN_VALUE_ID").eq(withAlias, "COMMON_TOKEN_VALUE_ID"))
                ;
        } else {
            // Include an additional predicate to make this a composite parameter query
            query.from()
            .innerJoin(paramTableName, alias(paramAlias), 
                on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(queryParmCode))
                .and(paramAlias, "COMPOSITE_ID").eq(firstCompositeTableAlias, "COMPOSITE_ID"))
            .innerJoin(withAlias, alias(withAlias),
                on(paramAlias, "COMMON_TOKEN_VALUE_ID").eq(withAlias, "COMMON_TOKEN_VALUE_ID"))
            ;
        }
    }

    /**
     * Add the select from common_token_values into a WITH clause attached to the main 
     * select statement
     * 
     * @param query
     * @param filter
     * @param paramAlias
     * @return the WITH alias name
     */
    private String addDistributedWithCommonTokenValue(SelectAdapter query, ExpNode filter, String paramAlias) {
        final String withAlias = "w" + paramAlias; // the alias used for the WITH sub-select
        Select selectFromCommonTokenValues = Select.select("common_token_value_id")
                .from("common_token_values", alias(paramAlias))
                .where(filter)
                .build();
        query.with(selectFromCommonTokenValues, alias(withAlias));        
        return withAlias;
    }

    /**
     * FHIR Specification: 
     *     A reference parameter refers to references between resources. For 
     *     example, find all Conditions where the subject reference is a 
     *     particular patient, where the patient is selected by name or 
     *     identifier. The interpretation of a reference parameter is either:
     *         [1] [parameter]=[id] the logical [id] of a resource using a local reference (i.e. a relative reference)
     *         [2] [parameter]=[type]/[id] the logical [id] of a resource of a specified type using a local reference (i.e. a relative reference), for when the reference can point to different types of resources (e.g. Observation.subject)
     *         [3] [parameter]=[url] where the [url] is an absolute URL - a reference to a resource by its absolute location, or by its canonical URL
     * 
     * For [1], the target resource type isn't known. This shouldn't matter, because
     * we still look up the logical_resource_id by its logical_id. If there are
     * multiple matches, they are by definition of different type, so this would be
     * an error. Therefore the query still only needs to deal with a single logical_resource_id.
     * For [2], we are guaranteed a single logical_resource_id because resourceType/logicalId
     * is unique.
     * For [3], we need to identify the value string as a url and not a local reference.
     * 
     * @param queryData
     * @param resourceType
     * @param queryParm
     * @return
     * @throws FHIRPersistenceException
     */
    private QueryData processRealReferenceParam(QueryData queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException {
        final int aliasIndex = getNextAliasIndex();
        final SelectAdapter query = queryData.getQuery();
        final String paramAlias = getParamAlias(aliasIndex);
        final String lrAlias = queryData.getLRAlias();

        // For V0027 reference parameters are stored in xx_ref_values using the
        // logical_id values stored in logical_resource_ident. Absolute references
        // are stored using a resource_type of "Resource" (similar to the default
        // code-system we used to use with common_token_values).

        // Firstly we need to split the query parm values into separate lists
        List<Pair<String, String>> resourceTypesAndIds = new ArrayList<>(queryParm.getValues().size());
        for (QueryParameterValue value : queryParm.getValues()) {
            resourceTypesAndIds.add(getResourceTypeAndId(queryParm, value));
        }

        List<Long> logicalResourceIdList = new ArrayList<>();
        for (Pair<String, String> resourceTypeAndId : resourceTypesAndIds) {
            String targetResourceType = resourceTypeAndId.getLeft();
            String referenceValue = resourceTypeAndId.getRight();

            if (targetResourceType != null) {
                Integer resourceTypeId = identityCache.getResourceTypeId(targetResourceType);
                if (resourceTypeId != null) {
                    // It's a valid resource type, so we treat as a local reference
                    logger.fine(() -> "reference search value: type[local] value[" + targetResourceType + "/" + referenceValue + "]");
                    Long logicalResourceId = identityCache.getLogicalResourceId(targetResourceType, referenceValue);
                    logicalResourceIdList.add(logicalResourceId != null ? logicalResourceId : -1);
                } else {
                    // Treat this as an error because it's not a valid local reference
                    throw new FHIRPersistenceException("Local reference specified with invalid resource type").withIssue(
                        Issue.builder()
                            .code(IssueType.INVALID)
                            .diagnostics("Local reference specified with invalid resource type")
                            .severity(IssueSeverity.ERROR)
                            .build());
                }
            } else {
                // Determine if the target value is an absolute or local reference
                if (ReferenceUtil.isAbsolute(referenceValue)) {
                    logger.fine(() -> "reference search value: type[absolute] value[" + referenceValue + "]");
                    Long logicalResourceId = identityCache.getLogicalResourceId("Resource", referenceValue);
                    logicalResourceIdList.add(logicalResourceId != null ? logicalResourceId : -1);
                } else {
                    // treat as a local reference where we don't know the type.
                    List<Long> localLogicalResourceIds = getLogicalResourceIdList(referenceValue);
                    if (localLogicalResourceIds.size() == 1) {
                        logger.fine(() -> "reference search value: type[local] value[" + referenceValue + "]");
                        logicalResourceIdList.add(localLogicalResourceIds.get(0));
                    } else if (localLogicalResourceIds.size() == 0) {
                        logger.fine(() -> "reference search value: type[local] value[" + referenceValue + "] notFound[true]");
                        if (logicalResourceIdList.isEmpty()) {
                            logicalResourceIdList.add(-1L); // need at least one value
                        }
                    } else {
                        // We may match multiple resource types here, but it's only an error
                        // if we join with the xx_ref_value table and still get multiple rows
                        logicalResourceIdList.addAll(localLogicalResourceIds);
                    }
                }
            }
        }

        // Only need to join with xx_ref_values
        final String queryParmCode = queryParm.getCode();
        final ExpNode filter = getReferenceFilter(queryParm, paramAlias, logicalResourceIdList).getExpression();
        final String paramTableName = getRefParamTable(filter, resourceType, paramAlias);
        query.from().innerJoin(paramTableName, alias(paramAlias), on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
            .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(queryParmCode))
            .and(filter));

        return queryData;
    }

    @Override
    public QueryData addCompositeParam(QueryData queryData, QueryParameter queryParm) throws FHIRPersistenceException {
        final String lrAlias = queryData.getLRAlias();

        final WhereAdapter where = queryData.getQuery().from().where();

        // Each query parm value gets its own EXISTS OR'd together
        if (queryParm.getValues().size() == 1) {
            // Simple optimization. Only one composite value, so add
            // as inner joins to the core parameter exists query
            QueryParameterValue compositeValue = queryParm.getValues().get(0);
            List<QueryParameter> components = compositeValue.getComponent();
            int firstAliasIndex = -1;
            for (int componentNum = 1; componentNum <= components.size(); componentNum++) {
                QueryParameter component = components.get(componentNum - 1);
                int aliasIndex = addCompositeParamTable(queryData.getQuery(), queryData.getResourceType(), lrAlias, component, componentNum, firstAliasIndex);

                if (componentNum == 1) {
                    // Remember the alias we use for the first component so we can join subsequent
                    // component tables to the first
                    firstAliasIndex = aliasIndex;
                }
            }
        } else {
            // Each value gets its own EXISTS clause which we combine together
            // with OR. The whole thing needs to be wrapped in parens to ensure
            // the correct precedence.
            // AND ( EXISTS (...) OR EXISTS (...) )
            where.and().leftParen();
            boolean first = true;
            for (QueryParameterValue compositeValue : queryParm.getValues()) {
                SelectAdapter exists = Select.select("1");

                List<QueryParameter> components = compositeValue.getComponent();
                for (int componentNum = 1; componentNum <= components.size(); componentNum++) {
                    QueryParameter component = components.get(componentNum - 1);

                    addParamTableToCompositeExists(exists, queryData.getResourceType(), lrAlias,
                        component, componentNum, true);
                }

                // Add the exists sub-query we just built to the where clause of the main query
                if (first) {
                    first = false;
                } else {
                    where.or();
                }
                where.exists(exists.build());
            }
            // AND ( EXISTS (...) OR EXISTS (...) )  <== close the paren
            where.rightParen();
        }

        // The only thing we can return which makes any sense is the original query
        return queryData;
    }

    /**
     * Add a parameter table filter for a composite parameter
     * @param query
     * @param resourceType
     * @param lrAlias
     * @param component
     * @param componentNum
     * @param firstAliasIndex
     * @return the parameter alias, so we can find the first composite param table alias
     * @throws FHIRPersistenceException
     */
    private int addCompositeParamTable(SelectAdapter query, String resourceType, String lrAlias, QueryParameter component, int componentNum,
        int firstAliasIndex) throws FHIRPersistenceException {
        final int aliasIndex = getNextAliasIndex();
        String paramTableAlias = getParamAlias(aliasIndex);
        String parameterName = component.getCode();

        // Grab the parameter filter expression first so that we can see if it's safe to apply
        // the COMMON_TOKEN_VALUES_ID optimization
        final ExpNode filter = paramFilter(component, paramTableAlias).getExpression();
        boolean isFilterOptimized = isOptimizedTokenParamFilter(filter, paramTableAlias);

        final String valuesTable;
        if (component.getType() == Type.TOKEN && filter != null) {
            // optimize token parameter joins if the expression lets us
            valuesTable = getTokenParamTable(filter, resourceType, paramTableAlias, isFilterOptimized);
        } else {
            valuesTable = paramValuesTableName(resourceType, component);
        }

        if (schemaType == SchemaType.DISTRIBUTED && component.getType() == Type.TOKEN && filter != null && !isFilterOptimized) {
            // only add the composite_id join for subsequent tables
            final String firstTableAlias = componentNum == 1 ? null : getParamAlias(firstAliasIndex);
            addDistributedCommonTokenValueFilter(query, resourceType, parameterName, lrAlias, filter, valuesTable, paramTableAlias, firstTableAlias);
        } else {
            if (componentNum == 1) {
                query.from().innerJoin(valuesTable, alias(paramTableAlias),
                    on(paramTableAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramTableAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
                    .and(filter));
    
            } else {
                // also join to the first parameter table
                final String firstTableAlias = getParamAlias(firstAliasIndex);
                query.from().innerJoin(valuesTable, alias(paramTableAlias),
                    on(paramTableAlias, "LOGICAL_RESOURCE_ID").eq(firstTableAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramTableAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
                    .and(paramTableAlias, "COMPOSITE_ID").eq(firstTableAlias, "COMPOSITE_ID")
                    .and(filter));
            }
        }
        return aliasIndex;
    }

    /**
     * Build the composite join by adding the parameter table for the given
     * component number in the composite definition.
     * @param exists
     * @param resourceType
     * @param lrAlias
     * @param component
     * @param componentNum
     * @param addParamFilter
     * @throws FHIRPersistenceException
     */
    private void addParamTableToCompositeExists(SelectAdapter exists, String resourceType, String lrAlias,
        QueryParameter component, int componentNum, boolean addParamFilter) throws FHIRPersistenceException {

        final String componentTableAlias = "comp" + componentNum;
        final String firstTableAlias = "comp1";
        final String parameterName = component.getCode();

        // Grab the parameter filter expression first so that we can see if it's safe to apply
        // the COMMON_TOKEN_VALUES_ID optimization
        final ExpNode filter;
        final boolean isFilterOptimized;
        if (addParamFilter) {
            filter = paramFilter(component, componentTableAlias).getExpression();
            // which table we join against depends on the fields used by the filter expression
            isFilterOptimized = isOptimizedTokenParamFilter(filter, componentTableAlias);
        } else {
            filter = null;
            isFilterOptimized = false;
        }

        final String valuesTable;
        if (component.getType() == Type.TOKEN && filter != null) {
            // optimize token parameter joins if the expression lets us
            valuesTable = getTokenParamTable(filter, resourceType, componentTableAlias, isFilterOptimized);
        } else {
            // not a token or there's no filter expression, so we just need the param table
            valuesTable = paramValuesTableName(resourceType, component);
        }

        if (componentNum == 1) {
            if (component.getType() == Type.TOKEN && schemaType == SchemaType.DISTRIBUTED && addParamFilter && !isFilterOptimized) {
                // Need to use a WITH statement so we can access COMMON_TOKEN_VALUES
                // only add the composite_id join for subsequent tables
                addDistributedCommonTokenValueFilter(exists, resourceType, parameterName, lrAlias, filter, valuesTable, componentTableAlias, null);
            } else {
                exists.from(valuesTable, alias(componentTableAlias))
                .where(componentTableAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID") // correlate with the main query
                .and(componentTableAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName));
    
                // Parameter filter is skipped if this is coming from a missing/not missing search
                if (addParamFilter) {
                    exists.from().where().and(filter);
                }
            }
        } else {
            if (component.getType() == Type.TOKEN && schemaType == SchemaType.DISTRIBUTED && addParamFilter && !isFilterOptimized) {
                // Need to use a WITH statement so we can access COMMON_TOKEN_VALUES
                // only add the composite_id join for subsequent tables
                addDistributedCommonTokenValueFilter(exists, resourceType, parameterName, lrAlias, filter, valuesTable, componentTableAlias, firstTableAlias);
            } else {
                // Join to the first parameter table
                exists.from().innerJoin(valuesTable, alias(componentTableAlias),
                    on(componentTableAlias, "LOGICAL_RESOURCE_ID").eq(firstTableAlias, "LOGICAL_RESOURCE_ID")
                    .and(componentTableAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(parameterName))
                    .and(componentTableAlias, "COMPOSITE_ID").eq(firstTableAlias, "COMPOSITE_ID"));
    
                // Parameter filter is skipped if this is coming from a missing/not missing search
                if (addParamFilter) {
                    exists.from().where().and(filter);
                }
            }
        }
    }

    @Override
    public QueryData addCompositeParam(QueryData queryData, QueryParameter queryParm, boolean isMissing) throws FHIRPersistenceException {
        final String lrAlias = queryData.getLRAlias();

        // Each value gets its own EXISTS clause which we combine together
        // with OR. The whole thing needs to be wrapped in parens to ensure
        // the correct precedence.
        // AND ( EXISTS (...) OR EXISTS (...)
        final WhereAdapter where = queryData.getQuery().from().where();
        where.and().leftParen();
        boolean first = true;

        // Each query parm value gets its own EXISTS OR'd together
        for (QueryParameterValue compositeValue : queryParm.getValues()) {
            SelectAdapter exists = Select.select("1");

            List<QueryParameter> components = compositeValue.getComponent();
            for (int componentNum = 1; componentNum <= components.size(); componentNum++) {
                QueryParameter component = components.get(componentNum - 1);
                addParamTableToCompositeExists(exists, queryData.getResourceType(), lrAlias,
                    component, componentNum, false); // do not add param filter expression
            }

            // Add the exists sub-query we just built to the where clause of the main query
            if (first) {
                first = false;
            } else {
                where.or();
            }

            if (isMissing) {
                // parameter should be missing, i.e. not exist
                where.notExists(exists.build());
            } else {
                // parameter should be not missing...i.e. it exists
                where.exists(exists.build());
            }
        }

        // AND ( EXISTS (...) OR EXISTS (...) )  <== close the paren
        where.rightParen();
        // The only thing we can return which makes any sense is the original query
        return queryData;
    }

    @Override
    public void addSortParam(QueryData queryData, String code, Type type, Direction direction) throws FHIRPersistenceException {
        // Each sort parameter gets added as parameter table which is outer-joined to the
        // core data query
        SelectAdapter query = queryData.getQuery();
        final int aliasIndex = getNextAliasIndex();
        final String paramAlias = getParamAlias(aliasIndex);

        addAggregateAndOrderByExpressions(queryData, code, type, direction, paramAlias);

        // Now add the parameter table as an outer join
        final String paramTable = getSortParameterTableName(queryData.getResourceType(), code, type);
        final String lrAlias = queryData.getLRAlias();

        if (ID.equals(code) || LAST_UPDATED.equals(code)) {
            // No need to join parameter table - sort column is in LOGICAL_RESOURCES table
            return;
        } else if (!this.legacyWholeSystemSearchParamsEnabled &&
                (PROFILE.equals(code) || SECURITY.equals(code) || TAG.equals(code))) {
            // For a sort by _tag, _profile, or _security we need to join the parameter-specific token
            // table with the common token values table.
            String parameterTableAlias = getParamAlias(getNextAliasIndex());
            query.from()
                .leftOuterJoin(paramTable, alias(parameterTableAlias),
                    on(parameterTableAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID"));
            query.from()
                .innerJoin("COMMON_TOKEN_VALUES", alias(paramAlias),
                    on(paramAlias, "COMMON_TOKEN_VALUE_ID").eq(parameterTableAlias, "COMMON_TOKEN_VALUE_ID"));
        } else {
            query.from()
                .leftOuterJoin(paramTable, alias(paramAlias),
                    on(paramAlias, "LOGICAL_RESOURCE_ID").eq(lrAlias, "LOGICAL_RESOURCE_ID")
                    .and(paramAlias, "PARAMETER_NAME_ID").eq(getParameterNameId(code)));
        }
   }

    /**
     * Returns the name of the database table corresponding to the code and type of the
     * passed sort parameter.
     *
     * @param code A SortParameter code
     * @param type A SortParameter type
     * @return String - A database table name
     * @throws FHIRPersistenceException
     */
    protected String getSortParameterTableName(String resourceType, String code, Type type) throws FHIRPersistenceException {
        final String METHODNAME = "getSortParameterTableName";
        logger.entering(CLASSNAME, METHODNAME);

        StringBuilder sortParameterTableName = new StringBuilder();
        sortParameterTableName.append(resourceType).append("_");

        switch (type) {
        case URI:
        case STRING:
            if (!this.legacyWholeSystemSearchParamsEnabled && PROFILE.equals(code)) {
                sortParameterTableName.append("PROFILES");
            } else {
                sortParameterTableName.append("STR_VALUES");
            }
            break;
        case DATE:
            sortParameterTableName.append("DATE_VALUES");
            break;
        case REFERENCE:
            sortParameterTableName.append("REF_VALUES_V");
            break;
        case TOKEN:
            if (!this.legacyWholeSystemSearchParamsEnabled && TAG.equals(code)) {
                sortParameterTableName.append("TAGS");
            } else if (!this.legacyWholeSystemSearchParamsEnabled && SECURITY.equals(code)) {
                sortParameterTableName.append("SECURITY");
            } else {
                sortParameterTableName.append("TOKEN_VALUES_V");
            }
            break;
        case NUMBER:
            sortParameterTableName.append("NUMBER_VALUES");
            break;
        case QUANTITY:
            sortParameterTableName.append("QUANTITY_VALUES");
            break;
        default:
            throw new FHIRPersistenceNotSupportedException("Parm type not supported: " + type.value());
        }

        logger.exiting(CLASSNAME, METHODNAME);
        return sortParameterTableName.toString();
    }

    /**
     * Add the min/max aggregate and sort expressions to the SORT query
     * @param queryData
     * @param code
     * @param type
     * @param direction
     * @param parmAlias
     * @throws FHIRPersistenceException
     */
    private void addAggregateAndOrderByExpressions(QueryData queryData, String code, Type type, Direction direction, String parmAlias)
            throws FHIRPersistenceException {
        final String METHODNAME = "addAggregateAndOrderByExpressions";
        logger.entering(CLASSNAME, METHODNAME);

        SelectAdapter query = queryData.getQuery();
        List<String> valueAttributeNames;

        if (!this.legacyWholeSystemSearchParamsEnabled &&
                (PROFILE.equals(code) || SECURITY.equals(code) || TAG.equals(code))) {
            valueAttributeNames = Collections.singletonList(TOKEN_VALUE);
        } else {
            valueAttributeNames = this.getValueAttributeNames(type);
        }
        for (String attributeName : valueAttributeNames) {
            StringBuilder expression = new StringBuilder();
            final String dirExp;
            if (direction == Direction.INCREASING) {
                expression.append(MIN);
                dirExp = "ASC";
            } else {
                expression.append(MAX);
                dirExp = "DESC";
            }
            expression.append(LEFT_PAREN);

            if (LAST_UPDATED.equals(code)) {
                expression.append(queryData.getLRAlias() + ".LAST_UPDATED");
            } else if (ID.equals(code)) {
                expression.append(queryData.getLRAlias() + ".LOGICAL_ID");
            } else {
                expression.append(parmAlias).append(".").append(attributeName);
            }
            expression.append(RIGHT_PAREN);

            // add the aggregate column expression to the select list clause
            query.addColumn(null, expression.toString(), null);

            // Add the column to the order by clause
            expression.append(" ").append(dirExp).append(" NULLS LAST");
            query.from().orderBy(expression.toString());
        }

        logger.exiting(CLASSNAME, METHODNAME);
    }

    /**
     * Returns the names of the Parameter attributes containing the values
     * corresponding to the passed sort parameter type.
     * @param type
     * @throws FHIRPersistenceException
     */
    private List<String> getValueAttributeNames(Type type) throws FHIRPersistenceException {
        final String METHODNAME = "getValueAttributeName";
        logger.entering(CLASSNAME, METHODNAME);

        List<String> attributeNames = new ArrayList<>();
        switch (type) {
        case STRING:
            attributeNames.add(STR_VALUE);
            break;
        case REFERENCE:
            attributeNames.add(REF_VALUE); // V0027 using xx_REF_VALUES_V
            break;
        case DATE:
            attributeNames.add(DATE_START);
            break;
        case TOKEN:
            attributeNames.add(TOKEN_VALUE);
            break;
        case NUMBER:
            attributeNames.add(NUMBER_VALUE);
            break;
        case QUANTITY:
            attributeNames.add(QUANTITY_VALUE);
            break;
        case URI:
            attributeNames.add(STR_VALUE);
            break;
        default:
            throw new FHIRPersistenceNotSupportedException("Parm type not supported: " + type.value());
        }

        logger.exiting(CLASSNAME, METHODNAME);
        return attributeNames;
    }

    /**
     * Check if whole-system search.
     * @param resourceType
     * @return true if whole-system search, false otherwise
     */
    private boolean isWholeSystemSearch(String resourceType) {
        return Resource.class.getSimpleName().equals(resourceType);
    }

    /**
     * Get the select column entry for the resource data column. If
     * the includeResourceData flag is false, the column is replaced
     * with a literal NULL, cast to the appropriate type for the database.
     * @return
     */
    private String getDataCol() {
        if (this.includeResourceData) {
            return "R.DATA";
        } else {
            switch (translator.getType()) {
            case DERBY:
                return "CAST(NULL AS BLOB) AS DATA";
            case POSTGRESQL:
            case CITUS:
                return "NULL::TEXT AS DATA";
            default:
                throw new IllegalStateException("Database type not supported: " + translator.getType().name());
            }
        }
    }
}