/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.dao.impl;

import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.END;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.THEN;
import static net.sovrinhealth.fhir.persistence.jdbc.JDBCConstants.WHEN;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.TransactionSynchronizationRegistry;

import net.sovrinhealth.fhir.database.utils.common.CalendarHelper;
import net.sovrinhealth.fhir.database.utils.query.QueryUtil;
import net.sovrinhealth.fhir.database.utils.query.Select;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceDataAccessException;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.jdbc.FHIRPersistenceJDBCCache;
import net.sovrinhealth.fhir.persistence.jdbc.connection.FHIRDbFlavor;
import net.sovrinhealth.fhir.persistence.jdbc.dao.api.ResourceDAO;
import net.sovrinhealth.fhir.persistence.jdbc.dto.Resource;
import net.sovrinhealth.fhir.persistence.jdbc.exception.FHIRPersistenceDBConnectException;
import net.sovrinhealth.fhir.persistence.jdbc.impl.ParameterTransactionDataImpl;
import net.sovrinhealth.fhir.persistence.util.InputOutputByteStream;

/**
 * This Data Access Object implements the ResourceDAO interface for creating, updating,
 * and retrieving rows in the IBM FHIR Server resource tables.
 */
public abstract class ResourceDAOImpl extends FHIRDbDAOImpl implements ResourceDAO {

    private static final Logger log = Logger.getLogger(ResourceDAOImpl.class.getName());
    private static final String CLASSNAME = ResourceDAOImpl.class.getName();

    public static final String DEFAULT_VALUE_REINDEX_TSTAMP = "1970-01-01 00:00:00";

    // column indices for all our resource reading queries
    public static final int IDX_RESOURCE_ID = 1;
    public static final int IDX_LOGICAL_RESOURCE_ID = 2;
    public static final int IDX_VERSION_ID = 3;
    public static final int IDX_LAST_UPDATED = 4;
    public static final int IDX_IS_DELETED = 5;
    public static final int IDX_DATA = 6;
    public static final int IDX_LOGICAL_ID = 7;
    public static final int IDX_RESOURCE_PAYLOAD_KEY = 8;
    public static final int IDX_RESOURCE_TYPE_ID = 9;

    // Read the current version of the resource (even if the resource has been deleted)
    private static final String SQL_READ = "SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY " +
            "FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE " +
            "LR.LOGICAL_ID = ? AND R.RESOURCE_ID = LR.CURRENT_RESOURCE_ID";

    // Read a specific version of the resource
    private static final String SQL_VERSION_READ =
            "SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY " +
                    "FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE " +
                    "LR.LOGICAL_ID = ? AND R.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID AND R.VERSION_ID = ?";

    // Read version history of the resource identified by its logical-id
    private static final String SQL_HISTORY =
            "SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY " +
                    "FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE " +
                    "LR.LOGICAL_ID = ? AND R.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID " +
                    "ORDER BY R.VERSION_ID DESC ";

    // Count the number of versions we have for the resource identified by its logical-id
    private static final String SQL_HISTORY_COUNT = "SELECT COUNT(R.VERSION_ID) FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE LR.LOGICAL_ID = ? AND " +
            "R.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID";

    private static final String SQL_HISTORY_FROM_DATETIME =
            "SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY " +
                    "FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE " +
                    "LR.LOGICAL_ID = ? AND R.LAST_UPDATED >= ? AND R.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID " +
                    "ORDER BY R.VERSION_ID DESC ";

    private static final String SQL_HISTORY_FROM_DATETIME_COUNT =
            "SELECT COUNT(R.VERSION_ID) FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE LR.LOGICAL_ID = ? AND " +
                    "R.LAST_UPDATED >= ? AND R.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID";

    private static final String SQL_READ_ALL_RESOURCE_TYPE_NAMES = "SELECT RESOURCE_TYPE_ID, RESOURCE_TYPE FROM RESOURCE_TYPES";

    private static final String SQL_READ_RESOURCE_TYPE = "CALL %s.add_resource_type(?, ?)";

    private static final String SQL_SEARCH_BY_IDS =
            "SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY " +
                    "FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE R.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID AND " +
                    "R.RESOURCE_ID IN ";
    private static final String SQL_SEARCH_BY_IDS_NO_DATA =
            "SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, CAST(NULL AS BLOB) AS DATA, LR.LOGICAL_ID, R.RESOURCE_PAYLOAD_KEY " +
                    "FROM %s_RESOURCES R, %s_LOGICAL_RESOURCES LR WHERE R.LOGICAL_RESOURCE_ID = LR.LOGICAL_RESOURCE_ID AND " +
                    "R.RESOURCE_ID IN ";

    private static final String SQL_GET_LOGICAL_RESOURCE_IDENT = ""
            + "SELECT logical_resource_id "
            + "  FROM logical_resource_ident "
            + " WHERE resource_type_id = ? "
            + "   AND logical_id = ?";

    // Get all records matching the given logical_id (multiple resource types)
    private static final String SQL_GET_LOGICAL_RESOURCE_IDENT_LIST = ""
            + "     SELECT logical_resource_id "
            + "       FROM logical_resource_ident "
            + "      WHERE logical_id = ?";

    private static final String SQL_ORDER_BY_IDS = "ORDER BY CASE R.RESOURCE_ID ";

    private static final String DERBY_PAGINATION_PARMS = "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String DB2_PAGINATION_PARMS = "LIMIT ? OFFSET ?";

    @SuppressWarnings("unused")
    private FHIRPersistenceContext context;

    private Map<String, Integer> newResourceTypeIds = new HashMap<>();
    private boolean runningInTrx = false;
    private TransactionSynchronizationRegistry trxSynchRegistry;

    private final FHIRPersistenceJDBCCache cache;

    private final ParameterTransactionDataImpl transactionData;

    /**
     * Constructs a DAO instance suitable for acquiring connections from a JDBC Datasource object.
     *
     * @param c
     * @param schemaName
     * @param flavor
     * @param trxSyncRegistry
     */
    public ResourceDAOImpl(Connection c, String schemaName, FHIRDbFlavor flavor, TransactionSynchronizationRegistry trxSynchRegistry,
            FHIRPersistenceJDBCCache cache, ParameterTransactionDataImpl ptdi) {
        super(c, schemaName, flavor);
        this.runningInTrx = true;
        this.trxSynchRegistry = trxSynchRegistry;
        this.cache = cache;
        this.transactionData = ptdi;
    }

    /**
     * Constructs a DAO instance for use outside a managed transaction (JEE) environment
     *
     * @param c
     * @param schemaName
     * @param flavor
     */
    public ResourceDAOImpl(Connection c, String schemaName, FHIRDbFlavor flavor, FHIRPersistenceJDBCCache cache) {
        super(c, schemaName, flavor);
        this.runningInTrx = false;
        this.trxSynchRegistry = null;
        this.cache = cache;
        this.transactionData = null; // not supported outside JEE
    }

    /**
     * Get the ParameterTransactionDataImpl held by this.
     *
     * @return the transactionData object. Can be null.
     */
    protected ParameterTransactionDataImpl getTransactionData() {
        return this.transactionData;
    }

    @Override
    public Resource read(String logicalId, String resourceType) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "read";
        log.entering(CLASSNAME, METHODNAME);

        Resource resource = null;
        List<Resource> resources;
        String stmtString = null;

        try {
            stmtString = String.format(SQL_READ, resourceType, resourceType);
            resources = this.runQuery(stmtString, logicalId);
            if (!resources.isEmpty()) {
                resource = resources.get(0);
            }
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return resource;
    }

    @Override
    public Resource versionRead(String logicalId, String resourceType, int versionId) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "versionRead";
        log.entering(CLASSNAME, METHODNAME);

        Resource resource = null;
        List<Resource> resources;
        String stmtString = null;

        try {
            stmtString = String.format(SQL_VERSION_READ, resourceType, resourceType);
            resources = this.runQuery(stmtString, logicalId, versionId);
            if (!resources.isEmpty()) {
                resource = resources.get(0);
            }
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return resource;

    }

    /**
     * Creates and returns a Resource DTO based on the contents of the passed ResultSet
     *
     * @param resultSet
     *            A ResultSet containing FHIR persistent object data.
     * @param hasResourceTypeId
     *            True if the ResultSet includes the RESOURCE_TYPE_ID column
     * @return Resource - A Resource DTO
     * @throws FHIRPersistenceDataAccessException
     */
    @Override
    protected Resource createDTO(ResultSet resultSet, boolean hasResourceTypeId) throws FHIRPersistenceDataAccessException {
        final String METHODNAME = "createDTO";
        log.entering(CLASSNAME, METHODNAME);

        Resource resource = new Resource();

        try {
            byte[] payloadData = resultSet.getBytes(IDX_DATA);
            if (payloadData != null) {
                resource.setDataStream(new InputOutputByteStream(payloadData, payloadData.length));
            }
            resource.setResourceId(resultSet.getLong(IDX_RESOURCE_ID));
            resource.setLogicalResourceId(resultSet.getLong(IDX_LOGICAL_RESOURCE_ID));
            resource.setLastUpdated(resultSet.getTimestamp(IDX_LAST_UPDATED, CalendarHelper.getCalendarForUTC()));
            resource.setLogicalId(resultSet.getString(IDX_LOGICAL_ID));
            resource.setVersionId(resultSet.getInt(IDX_VERSION_ID));
            resource.setDeleted(resultSet.getString(IDX_IS_DELETED).equals("Y") ? true : false);
            resource.setResourcePayloadKey(resultSet.getString(IDX_RESOURCE_PAYLOAD_KEY));
            
            if (hasResourceTypeId) {
                resource.setResourceTypeId(resultSet.getInt(IDX_RESOURCE_TYPE_ID));
            }
        } catch (Throwable e) {
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException("Failure creating Resource DTO.");
            throw severe(log, fx, e);
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }

        return resource;
    }

    @Override
    public List<Resource> history(String resourceType, String logicalId, Timestamp fromDateTime, int offset, int maxResults) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "history";
        log.entering(CLASSNAME, METHODNAME);

        List<Resource> resources = null;
        String stmtString = null;

        try {
            if (fromDateTime != null) {
                stmtString = String.format(SQL_HISTORY_FROM_DATETIME, resourceType, resourceType);
                stmtString = stmtString + DERBY_PAGINATION_PARMS;
                resources = this.runQuery(stmtString, logicalId, fromDateTime, offset, maxResults);
            } else {
                stmtString = String.format(SQL_HISTORY, resourceType, resourceType);
                stmtString = stmtString + DERBY_PAGINATION_PARMS;
                resources = this.runQuery(stmtString, logicalId, offset, maxResults);
            }
        } finally {
            log.exiting(CLASSNAME, METHODNAME, Arrays.toString(new Object[] { resources }));
        }
        return resources;
    }

    @Override
    public int historyCount(String resourceType, String logicalId, Timestamp fromDateTime)
            throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "historyCount";
        log.entering(CLASSNAME, METHODNAME);

        int count;
        String stmtString;

        try {
            if (fromDateTime != null) {
                stmtString = String.format(SQL_HISTORY_FROM_DATETIME_COUNT, resourceType, resourceType);
                count = this.runCountQuery(stmtString, logicalId, fromDateTime);
            } else {
                stmtString = String.format(SQL_HISTORY_COUNT, resourceType, resourceType);
                count = this.runCountQuery(stmtString, logicalId);
            }
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return count;
    }

    @Override
    public void setPersistenceContext(FHIRPersistenceContext context) {
        this.context = context;
    }

    @Override
    public Map<String, Integer> readAllResourceTypeNames()
            throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException {
        final String METHODNAME = "readAllResourceTypeNames";
        log.entering(CLASSNAME, METHODNAME);

        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Map<String, Integer> result = new HashMap<>();
        long dbCallStartTime;
        double dbCallDuration;

        try {
            final Connection connection = this.getConnection();
            stmt = connection.prepareStatement(SQL_READ_ALL_RESOURCE_TYPE_NAMES);
            dbCallStartTime = System.nanoTime();
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final int resourceTypeId = resultSet.getInt(1);
                final String resourceType = resultSet.getString(2);
                result.put(resourceType, resourceTypeId);
            }

            if (log.isLoggable(Level.FINE)) {
                dbCallDuration = (System.nanoTime() - dbCallStartTime) / 1e6;
                log.fine("DB read all resource type complete. executionTime=" + dbCallDuration + "ms");
            }
        } catch (Throwable e) {
            final String errMsg = "Failure retrieving all Resource type names.";
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException(errMsg);
            throw severe(log, fx, e);
        } finally {
            this.cleanup(stmt);
            log.exiting(CLASSNAME, METHODNAME);
        }

        return result;
    }

    @Override
    public Integer readResourceTypeId(String resourceType) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException {
        final String METHODNAME = "readResourceTypeId";
        log.entering(CLASSNAME, METHODNAME);

        final Connection connection = getConnection(); // do not close
        CallableStatement stmt = null;
        Integer parameterNameId = null;
        String stmtString;
        long dbCallStartTime;
        double dbCallDuration;

        try {
            stmtString = String.format(SQL_READ_RESOURCE_TYPE, getSchemaName());
            stmt = connection.prepareCall(stmtString);
            stmt.setString(1, resourceType);
            stmt.registerOutParameter(2, Types.INTEGER);
            dbCallStartTime = System.nanoTime();
            stmt.execute();
            dbCallDuration = (System.nanoTime() - dbCallStartTime) / 1e6;
            if (log.isLoggable(Level.FINE)) {
                log.fine("DB read resource type id complete. executionTime=" + dbCallDuration + "ms");
            }
            parameterNameId = stmt.getInt(2);
        } catch (Throwable e) {
            final String errMsg = "Failure storing Resource type name id: name=" + resourceType;
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException(errMsg);
            throw severe(log, fx, e);
        } finally {
            this.cleanup(stmt);
            log.exiting(CLASSNAME, METHODNAME);
        }
        return parameterNameId;
    }

    /**
     * Get the value of the database id for the given resourceType from the
     * JDBCIdentityCache. If the id isn't found, then it is read from the
     * database. Note that the resource type cache is prefilled and so we
     * should never get a miss...but this is here as a just-in-case protection
     * against reading the cache before the prefill is done.
     * @param resourceType
     * @return
     */
    protected Integer getResourceTypeId(String resourceType) throws FHIRPersistenceException {
        Integer resourceTypeId = cache.getResourceTypeCache().getId(resourceType);
        if (resourceTypeId == null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Resource type not found in cache: " + resourceType);
            }
            // cache miss, so read from the database
            resourceTypeId = this.readResourceTypeId(resourceType);
            
            if (resourceTypeId != null) {
                cache.getResourceTypeCache().addEntry(resourceType, resourceTypeId);
                cache.getResourceTypeNameCache().addEntry(resourceTypeId, resourceType);
            } else {
                log.severe("Resource type not found in database: " + resourceType);
                throw new FHIRPersistenceException("Resource type not found in database. Check server log for details");
            }
        }
        return resourceTypeId;
    }

    /**
     * Test for If-None-Match conditional create-on-update behavior. The If-None-Match
     * header value is encoded by the REST layer (minimizes propagation of user input)
     * using the following rules
     * <pre>
     *   If-None-Match: null then ifNoneMatch = null
     *   If-None-Match: * then ifNoneMatch = 0
     * </pre>
     * Other values such as W/"1" are intentionally unsupported because their
     * behavior may be unintuitive, especially around deleted resources. 
     * @param ifNoneMatch the encoded If-None-Match header value
     * @param currentVersionId the current version of the resource to compare with
     * @return
     */
    protected boolean checkIfNoneMatch(Integer ifNoneMatch, int currentVersionId) {
        // we currently don't care about a version match
        return ifNoneMatch != null && ifNoneMatch == 0;
    }

    @Override
    public List<Resource> search(String sqlSelect) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "search";
        log.entering(CLASSNAME, METHODNAME);

        List<Resource> resources;

        try {
            resources = this.runQuery(sqlSelect);
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }

        return resources;
    }

    @Override
    public List<Resource> searchByIds(String resourceType, List<Long> resourceIds, boolean includeResourceData)
            throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "searchByIds";
        log.entering(CLASSNAME, METHODNAME);

        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        final Connection connection = getConnection(); // do not close
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        String errMsg;
        StringBuilder idQuery = new StringBuilder();
        List<Resource> resources = new ArrayList<>();
        String stmtString = null;
        long dbCallStartTime;
        double dbCallDuration;

        try {
            if (includeResourceData) {
                stmtString = getSearchByIdsSql(resourceType);
            } else {
                stmtString = getSearchByIdsNoDataSql(resourceType);
            }
            idQuery.append(stmtString);
            idQuery.append("(");
            // resourceIds should have a max length of 1000 (the max page size)
            StringBuilder caseStmts = new StringBuilder();
            for (int i = 0; i < resourceIds.size(); i++) {
                if (i > 0) {
                    idQuery.append(",");
                }
                idQuery.append(resourceIds.get(i));

                // build up the caseStmts here so we only need to iterate the list once
                caseStmts.append(WHEN + resourceIds.get(i) + THEN + i);
            }
            idQuery.append(") " + SQL_ORDER_BY_IDS + caseStmts + END);

            stmt = connection.prepareStatement(idQuery.toString());
            dbCallStartTime = System.nanoTime();
            resultSet = stmt.executeQuery();
            dbCallDuration = (System.nanoTime() - dbCallStartTime) / 1e6;
            if (log.isLoggable(Level.FINE)) {
                log.fine("DB search by ids complete. SQL=[" + idQuery + "]  executionTime=" + dbCallDuration + "ms");
            }
            resources = this.createDTOs(resultSet);
        } catch (FHIRPersistenceException e) {
            throw e;
        } catch (Throwable e) {
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException("Failure retrieving FHIR Resources");
            errMsg = "Failure retrieving FHIR Resources. SQL=[" + idQuery + "]";
            throw severe(log, fx, errMsg, e);
        } finally {
            this.cleanup(resultSet, stmt);
            log.exiting(CLASSNAME, METHODNAME);
        }
        return resources;
    }

    protected String getSearchByIdsSql(String resourceType) {
        return String.format(SQL_SEARCH_BY_IDS, resourceType, resourceType);
    }

    protected String getSearchByIdsNoDataSql(String resourceType) {
        return String.format(SQL_SEARCH_BY_IDS_NO_DATA, resourceType, resourceType);
    }

    @Override
    public int searchCount(String sqlSelectCount) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "searchCount";
        log.entering(CLASSNAME, METHODNAME);

        int count;

        try {
            count = this.runCountQuery(sqlSelectCount);
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return count;
    }

    /**
     * Getter for access to the {@link FHIRPersistenceJDBCCache} from subclasses
     *
     * @return
     */
    protected FHIRPersistenceJDBCCache getCache() {
        return this.cache;
    }

    @Override
    public int searchCount(Select countQuery) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        return runCountQuery(countQuery);
   }

    @Override
    public List<Resource> search(Select select) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        return runQuery(select);
    }

    @Override
    public List<Long> searchForIds(Select dataQuery) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
        final String METHODNAME = "searchForIds";
        log.entering(CLASSNAME, METHODNAME);

        List<Long> resourceIds = new ArrayList<>();
        Connection connection = getConnection(); // do not close
        ResultSet resultSet = null;
        long dbCallStartTime;
        double dbCallDuration;

        // QueryUtil creates a fully bound executable statement
        try (PreparedStatement stmt = QueryUtil.prepareSelect(connection, dataQuery, getTranslator())) {
            dbCallStartTime = System.nanoTime();
            resultSet = stmt.executeQuery();
            dbCallDuration = (System.nanoTime() - dbCallStartTime) / 1e6;
            if (log.isLoggable(Level.FINE)) {
                log.fine("DB search for ids complete. " + dataQuery.toString() + "  executionTime=" + dbCallDuration + "ms");
            }
            while (resultSet.next()) {
                resourceIds.add(resultSet.getLong(1));
            }
        } catch (Throwable e) {
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException("Failure retrieving FHIR Resource Ids");
            final String errMsg = "Failure retrieving FHIR Resource Ids. SqlQueryData=" + dataQuery.toDebugString();
            throw severe(log, fx, errMsg, e);
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return resourceIds;
    }

    /**
     * Delete all parameters for the given resourceId from the named parameter value table
     * @param conn
     * @param tableName
     * @param logicalResourceId
     * @throws SQLException
     */
    private void deleteFromParameterTable(Connection conn, String tableName, long logicalResourceId) throws SQLException {
        final String delStrValues = "DELETE FROM " + tableName + " WHERE logical_resource_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(delStrValues)) {
            // bind parameters
            stmt.setLong(1, logicalResourceId);
            stmt.executeUpdate();
        }
    }

    @Override
    public Map<Integer, List<Long>> searchWholeSystem(Select wholeSystemQuery) throws FHIRPersistenceDataAccessException,
            FHIRPersistenceDBConnectException {
        final String METHODNAME = "searchWholeSystem";
        log.entering(CLASSNAME, METHODNAME);

        Map<Integer, List<Long>> resultMap = new HashMap<>();
        Connection connection = getConnection(); // do not close
        ResultSet resultSet = null;
        long dbCallStartTime;
        double dbCallDuration;

        try (PreparedStatement stmt = QueryUtil.prepareSelect(connection, wholeSystemQuery, getTranslator())) {
            dbCallStartTime = System.nanoTime();
            resultSet = stmt.executeQuery();
            dbCallDuration = (System.nanoTime() - dbCallStartTime) / 1e6;
            if (log.isLoggable(Level.FINE)) {
                log.fine("Successfully retrieved logical resource Ids [took " + dbCallDuration + " ms]");
            }

            // Transform the resultSet into a map of resource type IDs to logical resource IDs
            while (resultSet.next()) {
                Integer resourceTypeId = resultSet.getInt(1);
                Long logicalResourceId = resultSet.getLong(2);
                resultMap.computeIfAbsent(resourceTypeId, k -> new ArrayList<>()).add(logicalResourceId);
            }
        } catch (Throwable e) {
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException("Failure retrieving logical resource Ids");
            final String errMsg = "Failure retrieving logical resource Ids. SqlQueryData=" + wholeSystemQuery.toDebugString();
            throw severe(log, fx, errMsg, e);
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }

        return resultMap;
    }

    /**
     * Set an int parameter in the statement, handling null as required
     * @param ps
     * @param index
     * @param value
     * @throws SQLException
     */
    protected void setInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    /**
     * Set a String parameter in the statement, handling null as required
     * @param ps
     * @param index
     * @param value
     * @throws SQLException
     */
    protected void setString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    @Override
    public Long readLogicalResourceId(int resourceTypeId, String logicalId) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException {
        Long result = null;
        try (PreparedStatement ps = getConnection().prepareStatement(SQL_GET_LOGICAL_RESOURCE_IDENT)) {
            ps.setInt(1, resourceTypeId);
            ps.setString(2, logicalId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getLong(1);
            }
        } catch (Throwable e) {
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException("Failure retrieving logical_resource_id");
            final String errMsg = "Failure retrieving logical_resource_id from logical_resource_ident for '" + resourceTypeId + "/" + logicalId + "'";
            throw severe(log, fx, errMsg, e);
        }
        return result;
    }

    @Override
    public List<Long> readLogicalResourceIdList(String logicalId) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException {
        List<Long> result = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SQL_GET_LOGICAL_RESOURCE_IDENT_LIST)) {
            ps.setString(1, logicalId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
        } catch (Throwable e) {
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException("Failure retrieving logical_resource_id");
            final String errMsg = "Failure retrieving logical_resource_id list from logical_resource_ident for '" + logicalId + "'";
            throw severe(log, fx, errMsg, e);
        }
        return result;
    }
}