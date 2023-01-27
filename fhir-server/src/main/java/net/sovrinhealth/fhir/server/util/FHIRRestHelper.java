/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.util;

import static net.sovrinhealth.fhir.config.FHIRConfiguration.PROPERTY_VALIDATION_FAIL_FAST;
import static net.sovrinhealth.fhir.core.FHIRConstants.EXT_BASE;
import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.model.util.ModelSupport.getResourceType;
import static net.sovrinhealth.fhir.server.util.FHIRRestSupport.getEtagValue;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.owasp.encoder.Encode;

import net.sovrinhealth.fhir.config.FHIRConfigHelper;
import net.sovrinhealth.fhir.config.FHIRConfiguration;
import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.config.PropertyGroup;
import net.sovrinhealth.fhir.config.PropertyGroup.PropertyEntry;
import net.sovrinhealth.fhir.config.ResourcesConfigAdapter;
import net.sovrinhealth.fhir.core.FHIRConstants;
import net.sovrinhealth.fhir.core.FHIRVersionParam;
import net.sovrinhealth.fhir.core.HTTPHandlingPreference;
import net.sovrinhealth.fhir.core.HTTPReturnPreference;
import net.sovrinhealth.fhir.core.ResourceType;
import net.sovrinhealth.fhir.core.context.FHIRPagingContext;
import net.sovrinhealth.fhir.core.util.ResourceTypeUtil;
import net.sovrinhealth.fhir.database.utils.api.LockException;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.patch.FHIRPatch;
import net.sovrinhealth.fhir.model.patch.exception.FHIRPatchException;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry.Request;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry.Search;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.UnsignedInt;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.Url;
import net.sovrinhealth.fhir.model.type.code.BundleType;
import net.sovrinhealth.fhir.model.type.code.FHIRVersion;
import net.sovrinhealth.fhir.model.type.code.HTTPVerb;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.model.type.code.SearchEntryMode;
import net.sovrinhealth.fhir.model.util.CollectingVisitor;
import net.sovrinhealth.fhir.model.util.FHIRUtil;
import net.sovrinhealth.fhir.model.util.ModelSupport;
import net.sovrinhealth.fhir.model.util.SaltHash;
import net.sovrinhealth.fhir.model.visitor.ResourceFingerprintVisitor;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.path.exception.FHIRPathException;
import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.FHIRPersistenceSupport;
import net.sovrinhealth.fhir.persistence.FHIRPersistenceTransaction;
import net.sovrinhealth.fhir.persistence.HistorySortOrder;
import net.sovrinhealth.fhir.persistence.InteractionStatus;
import net.sovrinhealth.fhir.persistence.MultiResourceResult;
import net.sovrinhealth.fhir.persistence.ResourceChangeLogRecord;
import net.sovrinhealth.fhir.persistence.ResourceChangeLogRecord.ChangeType;
import net.sovrinhealth.fhir.persistence.ResourceEraseRecord;
import net.sovrinhealth.fhir.persistence.ResourceResult;
import net.sovrinhealth.fhir.persistence.SingleResourceResult;
import net.sovrinhealth.fhir.persistence.context.FHIRHistoryContext;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContextFactory;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceEvent;
import net.sovrinhealth.fhir.persistence.context.FHIRSystemHistoryContext;
import net.sovrinhealth.fhir.persistence.context.impl.FHIRPersistenceContextImpl;
import net.sovrinhealth.fhir.persistence.erase.EraseDTO;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceDataAccessException;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceIfNoneMatchException;
import net.sovrinhealth.fhir.persistence.helper.FHIRTransactionHelper;
import net.sovrinhealth.fhir.persistence.payload.PayloadPersistenceResponse;
import net.sovrinhealth.fhir.persistence.util.FHIRPersistenceUtil;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.search.SearchConstants;
import net.sovrinhealth.fhir.search.SummaryValueSet;
import net.sovrinhealth.fhir.search.context.FHIRSearchContext;
import net.sovrinhealth.fhir.search.exception.FHIRSearchException;
import net.sovrinhealth.fhir.search.parameters.QueryParameter;
import net.sovrinhealth.fhir.search.parameters.QueryParameterValue;
import net.sovrinhealth.fhir.search.util.ReferenceUtil;
import net.sovrinhealth.fhir.search.util.ReferenceValue;
import net.sovrinhealth.fhir.search.util.ReferenceValue.ReferenceType;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.exception.FHIRResourceDeletedException;
import net.sovrinhealth.fhir.server.exception.FHIRResourceNotFoundException;
import net.sovrinhealth.fhir.server.interceptor.FHIRPersistenceInterceptorMgr;
import net.sovrinhealth.fhir.server.operation.FHIROperationRegistry;
import net.sovrinhealth.fhir.server.rest.FHIRRestInteraction;
import net.sovrinhealth.fhir.server.rest.FHIRRestInteractionVisitorMeta;
import net.sovrinhealth.fhir.server.rest.FHIRRestInteractionVisitorOffload;
import net.sovrinhealth.fhir.server.rest.FHIRRestInteractionVisitorPersist;
import net.sovrinhealth.fhir.server.rest.FHIRRestInteractionVisitorReferenceMapping;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperation;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationUtil;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;
import net.sovrinhealth.fhir.server.spi.operation.FHIRRestOperationResponse;
import net.sovrinhealth.fhir.validation.FHIRValidator;
import net.sovrinhealth.fhir.validation.exception.FHIRValidationException;

/**
 * Helper methods for performing the "heavy lifting" with respect to implementing
 * FHIR interactions.
 */
public class FHIRRestHelper implements FHIRResourceHelpers {
    private static final Logger log =
            java.util.logging.Logger.getLogger(FHIRRestHelper.class.getName());

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String LOCAL_REF_PREFIX = "urn:";
    private static final net.sovrinhealth.fhir.model.type.String SC_BAD_REQUEST_STRING = string(Integer.toString(SC_BAD_REQUEST));
    private static final net.sovrinhealth.fhir.model.type.String SC_ACCEPTED_STRING = string(Integer.toString(SC_ACCEPTED));
    private static final ZoneId UTC = ZoneId.of("UTC");

    // Convenience constants to make call parameters more readable
    private static final boolean THROW_EXC_ON_NULL = true;
    private static final boolean CHECK_INTERACTION_ALLOWED = true;

    // default number of entries in system history if no _count is given
    private static final int DEFAULT_HISTORY_ENTRIES = 100;

    // clamp the number of entries in system history to 1000
    private static final int MAX_HISTORY_ENTRIES = 1000;

    public static final DateTimeFormatter PARSER_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("EEE")
            .optionalStart()
            // ANSIC date time format for If-Modified-Since
            .appendPattern(" MMM dd HH:mm:ss yyyy")
            .optionalEnd()
            .optionalStart()
            // Touchstone date time format for If-Modified-Since
            .appendPattern(", dd-MMM-yy HH:mm:ss")
            .optionalEnd().toFormatter();

    private final FHIRPersistence persistence;
    private final SearchHelper searchHelper;
    private final ResourcesConfigAdapter resourcesConfig;
    private final FHIRVersionParam fhirVersion;

    // Used for correlating requests within a bundle.
    private String bundleRequestCorrelationId = null;

    private final FHIRValidator validator = FHIRValidator.validator(
            FHIRConfigHelper.getBooleanProperty(PROPERTY_VALIDATION_FAIL_FAST, Boolean.FALSE));

    /**
     * Construct an instance with the passed FHIRPersistence and SearchHelper, and a FHIRVersion of 4.3.0
     * @see #FHIRRestHelper(FHIRPersistence, FHIRVersion)
     */
    public FHIRRestHelper(FHIRPersistence persistence, SearchHelper searchHelper) {
        this(persistence, searchHelper, FHIRVersionParam.VERSION_43);
    }

    /**
     * @param persistence a FHIRPersistence instance to use for the interactions
     * @param searchHelper a SearchHelper instance for working with search parameters
     * @param fhirVersion the fhirVersion to use for the interactions
     * @implNote fhirVersion is used to validate that the interactions are only
     *          performed against resource types compatible with the target version
     */
    public FHIRRestHelper(FHIRPersistence persistence, SearchHelper searchHelper, FHIRVersionParam fhirVersion) {
        this.persistence = persistence;
        this.searchHelper = searchHelper;
        PropertyGroup resourcesPropertyGroup = FHIRConfigHelper.getPropertyGroup(FHIRConfiguration.PROPERTY_RESOURCES);
        this.resourcesConfig = new ResourcesConfigAdapter(resourcesPropertyGroup, fhirVersion);
        this.fhirVersion = fhirVersion;
    }

    @Override
    public FHIRRestOperationResponse doCreate(String type, Resource resource, String ifNoneExist,
            boolean doValidation) throws Exception {

        // Validate that the interaction is allowed for the given resource type
        validateInteraction(Interaction.CREATE, type);

        // Validate the input and, if valid, start collecting supplemental warnings
        List<Issue> warnings = doValidation ? new ArrayList<>(validateInput(resource)) : new ArrayList<>();

        // Manage a transaction, starting a new one if we don't have one already
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        txn.begin();

        FHIRRestOperationResponse response;
        try {
            // Prepare the persistence event
            FHIRPersistenceEvent event =
                    new FHIRPersistenceEvent(resource, buildPersistenceEventProperties(type, null, null, null, null));

            // Run the meta phase to handle ifNoneExist and update the resource meta-data
            response = doCreateMeta(event, warnings, type, resource, ifNoneExist);

            // If we get a response back from doCreateMeta it means conditional create found
            // a match so we can skip further processing
            if (response == null) {
                // Persistence event processing may modify the resource, so make sure we have the latest value
                resource = event.getFhirResource();

                final String resourcePayloadKey = UUID.randomUUID().toString();
                int newVersionNumber = FHIRPersistenceSupport.getMetaVersionId(resource);
                PayloadPersistenceResponse offloadResponse = storePayload(resource, resource.getId(), newVersionNumber, resourcePayloadKey);
                response = doCreatePersist(event, warnings, resource, offloadResponse);
            }

            // At this point, we can be sure the transaction must have been started, so always commit
            txn.commit();
            txn = null;
        } finally {
            // If the transaction is still active and we started it, then roll it back because something
            // has gone wrong
            if (txn != null) {
                txn.rollback();
            }
        }

        return response;
    }

    @Override
    public FHIRRestOperationResponse doCreateMeta(FHIRPersistenceEvent event, List<Issue> warnings, String type, Resource resource,
            String ifNoneExist) throws Exception {
        log.entering(this.getClass().getName(), "doCreateMeta");

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        try {
            // Make sure the expected type (specified in the URL string) is congruent with the actual type
            // of the resource.
            String resourceType = ModelSupport.getTypeName(resource.getClass());
            if (!resourceType.equals(type)) {
                String msg = "Resource type '" + resourceType
                        + "' does not match type specified in request URI: " + type;
                throw buildRestException(msg, IssueType.INVALID);
            }

            // Check to see if we're supposed to perform a conditional 'create'.
            if (ifNoneExist != null && !ifNoneExist.isEmpty()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Performing conditional create with search criteria: " + ifNoneExist);
                }
                Bundle responseBundle = null;

                // Perform the search using the "If-None-Exist" header value.
                try {
                    MultivaluedMap<String, String> searchParameters = getQueryParameterMap(ifNoneExist);
                    responseBundle = doSearch(type, null, null, searchParameters, null, false, true);
                } catch (FHIROperationException e) {
                    throw e;
                } catch (Throwable t) {
                    String msg =
                            "An error occurred while performing the search for a conditional create operation.";
                    log.log(Level.WARNING, msg, t);
                    throw new FHIROperationException(msg, t);
                }

                // Check the search results to determine whether or not to perform the create operation.
                int resultCount = responseBundle.getEntry().size();
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Conditional create search yielded " + resultCount + " results.");
                }

                if (resultCount == 0) {
                    // Do nothing and fall through to process the 'create' request.
                } else if (resultCount == 1) {
                    // If we found a single match, bypass the 'create' request and return information
                    // for the matched resource.
                    final Resource matchedResource = responseBundle.getEntry().get(0).getResource();
                    final FHIRRestOperationResponse ior = new FHIRRestOperationResponse();
                    ior.setLocationURI(FHIRUtil.buildLocationURI(type, matchedResource));
                    ior.setStatus(Response.Status.OK);
                    ior.setResource(matchedResource);
                    ior.setCompleted(true);
                    ior.setOperationOutcome(FHIRUtil.buildOperationOutcome("Found a single match; check the Location header",
                            IssueType.INFORMATIONAL, IssueSeverity.INFORMATION));
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("Returning location URI of matched resource: " + ior.getLocationURI());
                    }

                    return ior;
                } else {
                    String msg =
                            "The search criteria specified for a conditional create operation returned multiple matches.";
                    throw buildRestException(msg, IssueType.MULTIPLE_MATCHES);
                }
            }

            // Resources may contain an id. For create, this value should be ignored (we no longer reject the request).
            if (resource.getId() != null) {
                String msg = "The create request resource included id: '" + resource.getId() + "'; this id has been replaced";
                warnings.add(FHIRUtil.buildOperationOutcomeIssue(IssueSeverity.INFORMATION, IssueType.INFORMATIONAL, msg));
                if (log.isLoggable(Level.FINE)) {
                    log.fine(msg);
                }

                // Null out the id so that interceptors don't get confused by the bogus value passed in
                Resource.Builder builder = resource.toBuilder();
                builder.id(null);
                builder.setValidating(false);
                event.setFhirResource(builder.build());
            }

            // Now we know we are going forward with the create, so fire the 'beforeCreate' event. This may modify the resource
            getInterceptorMgr().fireBeforeCreateEvent(event);

            // We need to assign the identifier during this first phase so that we have all the ids
            // before any of the local reference substitutions are performed in the 'prepare' phase
            String logicalId = generateResourceId();
            final net.sovrinhealth.fhir.model.type.Instant lastUpdated = FHIRPersistenceSupport.getCurrentInstant();
            final int newVersionNumber = 1;
            resource = FHIRPersistenceUtil.copyAndSetResourceMetaFields(event.getFhirResource(), logicalId, newVersionNumber, lastUpdated);
            event.setFhirResource(resource);

        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            log.exiting(this.getClass().getName(), "doCreateMeta");
        }

        return null;
    }

    @Override
    public FHIRRestOperationResponse doCreatePersist(FHIRPersistenceEvent event, List<Issue> warnings, Resource resource,
            PayloadPersistenceResponse offloadResponse) throws Exception {
        log.entering(this.getClass().getName(), "doCreatePersist");

        FHIRRestOperationResponse ior = new FHIRRestOperationResponse();

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        // We'll only start a new transaction here if we don't have one. We'll only
        // commit at the end if we started one here
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        txn.begin();

        try {
            checkIdAndMeta(resource);

            // create the resource and return the location header.
            final FHIRPersistenceContext persistenceContext =
                    FHIRPersistenceContextImpl.builder(event)
                    .withOffloadResponse(offloadResponse)
                    .withRequestShard(requestContext.getRequestShardKey())
                    .build();

            // For 1869 bundle processing, the resource is updated first and is no longer mutated by the
            // persistence layer.
            SingleResourceResult<Resource> result = persistence.create(persistenceContext, resource);
            if (result.isSuccess() && result.getOutcome() != null) {
                warnings.addAll(result.getOutcome().getIssue());
            }
            ior.setStatus(Response.Status.CREATED);
            ior.setResource(resource);
            ior.setOperationOutcome(FHIRUtil.buildOperationOutcome(warnings));

            // Build our location URI and add it to the interceptor event structure since it is now known.
            ior.setLocationURI(FHIRUtil.buildLocationURI(ModelSupport.getTypeName(resource.getClass()), resource));
            event.getProperties().put(FHIRPersistenceEvent.PROPNAME_RESOURCE_LOCATION_URI, ior.getLocationURI().toString());

            // Invoke the 'afterCreate' interceptor methods.
            getInterceptorMgr().fireAfterCreateEvent(event);

            // Commit the transaction if we started it (batch bundle)
            txn.commit();
            txn = null;

            return ior;
        } finally {
            if (txn != null) {
                txn.rollback();
            }
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);
            log.exiting(this.getClass().getName(), "doCreatePersist");
        }
    }

    @Override
    public FHIRRestOperationResponse doPatch(String type, String id, FHIRPatch patch, String ifMatchValue,
            String searchQueryString, boolean skippableUpdate) throws Exception {
        log.entering(this.getClass().getName(), "doPatch");

        // Validate that interaction is allowed for given resource type
        validateInteraction(Interaction.PATCH, type);

        try {
            return doPatchOrUpdate(type, id, patch, null, ifMatchValue, searchQueryString, skippableUpdate, DO_VALIDATION, IF_NOT_MATCH_NULL);
        } finally {
            log.exiting(this.getClass().getName(), "doPatch");
        }
    }

    @Override
    public FHIRRestOperationResponse doUpdate(String type, String id, Resource newResource, String ifMatchValue,
            String searchQueryString, boolean skippableUpdate, boolean doValidation, Integer ifNoneMatch) throws Exception {
        log.entering(this.getClass().getName(), "doUpdate");

        // Validate that interaction is allowed for given resource type
        validateInteraction(Interaction.UPDATE, type);

        try {
            return doPatchOrUpdate(type, id, null, newResource, ifMatchValue, searchQueryString, skippableUpdate, doValidation, ifNoneMatch);
        } finally {
            log.exiting(this.getClass().getName(), "doUpdate");
        }
    }

    /**
     * Common handling of PATCH or UPDATE interactions
     * @param type
     * @param id
     * @param patch
     * @param newResource
     * @param ifMatchValue
     * @param searchQueryString
     * @param skippableUpdate
     * @param doValidation
     * @param ifNoneMatch
     * @return
     * @throws Exception
     */
    private FHIRRestOperationResponse doPatchOrUpdate(String type, String id, FHIRPatch patch, Resource newResource, String ifMatchValue,
            String searchQueryString, boolean skippableUpdate, boolean doValidation, Integer ifNoneMatch) throws Exception {
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        txn.begin();

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();
        try {
            // Do the first phase, which includes updating the meta in the resource
            FHIRPersistenceEvent event = new FHIRPersistenceEvent(newResource, buildPersistenceEventProperties(type, id, null, null, null));
            List<Issue> warnings = new ArrayList<>();
            FHIRRestOperationResponse metaResponse = doUpdateMeta(event, type, id, patch, newResource, ifMatchValue, searchQueryString, skippableUpdate, ifNoneMatch, doValidation, warnings);
            if (metaResponse.isCompleted()) {
                // skip the update, so we can short-circuit here
                txn.commit();
                txn = null;
                return metaResponse;
            }

            // Store the payload if we're offloading
            final String resourcePayloadKey = UUID.randomUUID().toString();
            int newVersionNumber = FHIRPersistenceSupport.getMetaVersionId(metaResponse.getResource());
            PayloadPersistenceResponse offloadResponse = storePayload(metaResponse.getResource(), metaResponse.getResource().getId(), newVersionNumber, resourcePayloadKey);

            // Persist the resource
            FHIRRestOperationResponse ior = doPatchOrUpdatePersist(event, type, id, patch != null,
                    metaResponse.getResource(), metaResponse.getPrevResource(), warnings, metaResponse.isDeleted(),
                    ifNoneMatch, offloadResponse);

            txn.commit();
            txn = null;

            return ior;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            // If we still have a transaction at this point, we need to rollback due to an error.
            if (txn != null) {
                txn.rollback();
            }

            log.exiting(this.getClass().getName(), "doUpdate");
        }

    }

    @Override
    public FHIRRestOperationResponse doUpdateMeta(FHIRPersistenceEvent event, String type, String id, FHIRPatch patch, Resource newResource,
            String ifMatchValue, String searchQueryString, boolean skippableUpdate, Integer ifNoneMatch, boolean doValidation,
            List<Issue> warnings) throws Exception {
        log.entering(this.getClass().getName(), "doUpdateMeta");

        // Do everything we need to get the resource ready for storage. This includes handling
        // update-or-create, conditionals, and the update to the resource meta fields. This
        // is all part of the first phase - for bundle-processing, the second phase will handle
        // any local reference mapping. Note that we don't do any transaction processing inside
        // this method - there's only need for one transaction when processing all the doUpdateMeta
        // calls during a batch bundle because all the operations are reads. We only need individual
        // transactions later on when we're actually persisting stuff. These saves us a lot of
        // transaction overhead, which could be significant when dealing with large bundles.

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        boolean isDeleted; // stash the deleted status of the resource when we first read it
        int currentVersion; // stash the current version of the resource when we first read it
        Instant currentLastUpdated; // the lastUpdated time from the current version if we can read it
        FHIRRestOperationResponse ior = new FHIRRestOperationResponse();

        try {
            // Make sure the type specified in the URL string matches the resource type obtained from the new resource.
            if (patch == null) {
                String resourceType =  ModelSupport.getTypeName(newResource.getClass());
                if (!resourceType.equals(type)) {
                    String msg = "Resource type '" + resourceType
                            + "' does not match type specified in request URI: " + type;
                    throw buildRestException(msg, IssueType.INVALID);
                }
            }

            // Next, if a conditional update was invoked then use the search criteria to find the
            // resource to be updated. Otherwise, we'll use the id value to retrieve the current
            // version of the resource.
            if (searchQueryString != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Performing conditional update/patch with search criteria: "
                            + Encode.forHtml(searchQueryString));
                }
                Bundle responseBundle = null;
                try {
                    MultivaluedMap<String, String> searchParameters = getQueryParameterMap(searchQueryString);
                    responseBundle = doSearch(type, null, null, searchParameters, null, false, true);
                } catch (FHIROperationException e) {
                    throw e;
                } catch (Throwable t) {
                    String msg =
                            "An error occurred while performing the search for a conditional update/patch operation.";
                    throw new FHIROperationException(msg, t);
                }

                // Check the search results to determine whether or not to perform the update operation.
                int resultCount = responseBundle.getEntry().size();
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Conditional update/patch search yielded " + resultCount + " results.");
                }

                if (resultCount == 0) {
                    if (patch != null) {
                        String msg =
                                "The search criteria specified for a conditional patch operation did not return any results.";
                        throw buildRestException(msg, IssueType.NOT_FOUND);
                    }
                    // Search yielded no matches, so we'll do an update-as-create operation below.
                    ior.setPrevResource(null);

                    // if no id provided, then generate an id for the input resource
                    if (newResource.getId() == null) {
                        id = persistence.generateResourceId();
                        newResource = newResource.toBuilder().id(id).build();
                        // No match, so deletion/version status doesn't matter
                        isDeleted = false;
                        currentVersion = 0; // will be a create
                        currentLastUpdated = null;
                    } else {
                        // An id was provided, so we need to perform a read at this point so we know whether
                        // this is going to be an update or create. This also now gives us the version id
                        // needed to correctly update the meta for the new resource
                        id = newResource.getId();
                        SingleResourceResult<? extends Resource> srr = doRead(type, id, false, null, false);
                        ior.setPrevResource(srr.getResource()); // might be null if resource is deleted
                        isDeleted = srr.isDeleted();
                        
                        
                        if (srr.getResource() == null && !persistence.isUpdateCreateEnabled()) {
                            // Check that the resource exists, unless the updateCreate feature is enabled.
                            // No matches, id provided and doesn't already exist and updateCreate feature is not enabled : Should be rejected with error message.
                            String msg = "Resource '" + type + "/" + id + "' not found.";
                            log.log(Level.SEVERE, msg);
                            throw new FHIRResourceNotFoundException(msg);
                        } else if (srr.getResource() != null && !isDeleted) {
                            // No matches, id provided and already exist: The server rejects the update with a 409 Conflict error
                            String msg = "Conflict error! The search criteria specified for a conditional update operation " +
                                    "did not return any results but the input resource with id: " + id + " already exists." ;
                            throw buildRestException(msg, IssueType.CONFLICT);
                        } 
                        currentVersion = srr.getVersion();
                        currentLastUpdated = srr.getLastUpdated();
                    }
                } else if (resultCount == 1) {
                    // If we found a single match, then we'll perform a normal update on the matched resource.
                    ior.setPrevResource(responseBundle.getEntry().get(0).getResource());
                    id = ior.getPrevResource().getId();

                    if (id == null) {
                        // This should never happen, but we protect against it to avoid propagating the issue
                        String msg = "Search result resource 'id' attribute is null.";
                        throw buildRestException(msg, IssueType.VALUE);
                    }

                    // if patch is null then we have a "normal" update, so we need to check the newResource id
                    if (patch == null) {
                        if (newResource.getId() != null) {
                            // If the id of the input resource is provided, it MUST match the id of the previous resource
                            // found by the search
                            if (!newResource.getId().equals(id)) {
                                String msg = "Input resource 'id' attribute must match the id of the search result resource.";
                                throw buildRestException(msg, IssueType.VALUE);
                            }
                        } else {
                            // The new resource does not contain an id, so we set it using the id of the
                            // previous resource found by the search
                            newResource = newResource.toBuilder().id(id).build();
                        }
                    }

                    // Got a match, so definitely can't be deleted
                    isDeleted = false;
                    currentVersion = FHIRPersistenceSupport.getMetaVersionId(responseBundle.getEntry().get(0).getResource());
                    currentLastUpdated = FHIRPersistenceSupport.getLastUpdatedFromResource(responseBundle.getEntry().get(0).getResource());
                } else {
                    String msg =
                            "The search criteria specified for a conditional update/patch operation returned multiple matches.";
                    throw buildRestException(msg, IssueType.MULTIPLE_MATCHES);
                }

            } else {
                // searchQueryString is null

                // Make sure an id value was passed in.
                if (id == null) {
                    String msg = "The 'id' parameter is required for an update/patch operation.";
                    throw buildRestException(msg, IssueType.REQUIRED);
                }

                // If an id value was passed in (i.e. the id specified in the REST API URL string),
                // then make sure it's the same as the value in the resource.
                if (patch == null) {
                    // Make sure the resource has an 'id' attribute.
                    if (newResource.getId() == null) {
                        String msg = "Input resource must contain an 'id' attribute.";
                        throw buildRestException(msg, IssueType.INVALID);
                    }

                    if (!newResource.getId().equals(id)) {
                        String msg = "Input resource 'id' attribute must match 'id' parameter.";
                        throw buildRestException(msg, IssueType.VALUE);
                    }
                }

                // Retrieve the resource to be updated using the type and id values. Include
                // the resource even if it has been deleted
                SingleResourceResult<? extends Resource> srr = doRead(type, id, (patch != null), null, false);
                ior.setPrevResource(srr.getResource());
                isDeleted = srr.isDeleted();
                currentVersion = srr.getVersion();
                currentLastUpdated = srr.getLastUpdated();

                // Since 1869, this check is performed before entering the persistence layer
                // Check that the resource exists, unless the updateCreate feature is enabled and this is not a patch
                if (srr.getResource() == null && (!persistence.isUpdateCreateEnabled() || (patch != null))) {
                    String msg = "Resource '" + type + "/" + id + "' not found.";
                    log.log(Level.SEVERE, msg);
                    throw new FHIRResourceNotFoundException(msg);
                }
            }

            if (patch != null) {
                try {
                    newResource = patch.apply(ior.getPrevResource());
                } catch (FHIRPatchException e) {
                    String msg = "Invalid patch: " + e.getMessage();
                    String path = e.getPath() != null ? e.getPath() : "<no-path>";
                    throw new FHIROperationException(msg, e).withIssue(Issue.builder()
                            .severity(IssueSeverity.ERROR)
                            .code(IssueType.INVALID)
                            .details(CodeableConcept.builder()
                                    .text(string(msg))
                                    .build())
                            .expression(string(path))
                            .build());
                }
            }

            // Validate the input and, if valid, start collecting supplemental warnings
            if (doValidation) {
                warnings.addAll(validateInput(newResource));
            }

            // Perform the "version-aware" update check
            if (ior.getPrevResource() != null) {
                if (ifNoneMatch != null && ifNoneMatch.equals(0)) {
                    Integer existingVersion = Integer.parseInt(ior.getPrevResource().getMeta().getVersionId().getValue());
                    handleIfNoneMatchExisted(type, id, ior, existingVersion);

                    ior.setCompleted(true);
                    return ior; // early exit, before firing any update events
                }
                performVersionAwareUpdateCheck(ior.getPrevResource(), ifMatchValue);
            }

            // Configure the persistence event ready to fire the "before create|update|patch" events.
            event.setFhirResource(newResource);
            event.setPrevFhirResource(ior.getPrevResource());

            // Next, invoke the 'beforeCreate', 'beforePatch', or 'beforeUpdate' interceptor methods as appropriate.
            boolean updateCreate = (ior.getPrevResource() == null);
            if (updateCreate) {
                getInterceptorMgr().fireBeforeCreateEvent(event);
            } else {
                if (patch != null) {
                    event.getProperties().put(FHIRPersistenceEvent.PROPNAME_PATCH, patch);
                    getInterceptorMgr().fireBeforePatchEvent(event);
                } else {
                    getInterceptorMgr().fireBeforeUpdateEvent(event);
                }
            }

            // capture the resource in case the interceptors modified it in some way
            newResource = event.getFhirResource();

            // In the case of a patch, we should not be updating meaninglessly.
            if ((skippableUpdate || patch != null) && !isDeleted && ior.getPrevResource() != null) {
                ResourceFingerprintVisitor fingerprinter = new ResourceFingerprintVisitor();
                ior.getPrevResource().accept(fingerprinter);
                SaltHash baseline = fingerprinter.getSaltAndHash();

                fingerprinter = new ResourceFingerprintVisitor(baseline);
                newResource.accept(fingerprinter);
                if (fingerprinter.getSaltAndHash().equals(baseline)) {
                    ior.setResource(ior.getPrevResource());
                    ior.setStatus(Status.OK);
                    ior.setLocationURI(FHIRUtil.buildLocationURI(type, ior.getPrevResource()));
                    ior.setOperationOutcome(OperationOutcome.builder()
                            .issue(Issue.builder()
                                .severity(IssueSeverity.INFORMATION)
                                .code(IssueType.INFORMATIONAL)
                                .details(CodeableConcept.builder()
                                    .text(string("Update resource matches the existing resource; skipping the update"))
                                    .build())
                                .build())
                            .build());

                    ior.setCompleted(true);
                    return ior; // early exit
                }
            }

            // update the meta in the new resource. Use the version from the previous resource - this gets checked
            // again under a database lock during the persistence phase and the request will be rejected if there's
            // a mismatch (can happen when there are concurrent updates).
            final net.sovrinhealth.fhir.model.type.Instant lastUpdated = FHIRPersistenceSupport.getNewLastUpdatedInstant(currentLastUpdated);
            final int newVersionNumber = currentVersion + 1; // currentVersion will be 0 if this is a create
            newResource = FHIRPersistenceUtil.copyAndSetResourceMetaFields(newResource, newResource.getId(), newVersionNumber, lastUpdated);

            ior.setResource(newResource);
            ior.setDeleted(isDeleted);

            // That's it for now - persistence is done later
            return ior;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            log.exiting(this.getClass().getName(), "doUpdateMeta");
        }
    }

    @Override
    public FHIRRestOperationResponse doPatchOrUpdatePersist(FHIRPersistenceEvent event, String type, String id,
            boolean isPatch, Resource newResource, Resource prevResource,
            List<Issue> warnings, boolean isDeleted, Integer ifNoneMatch, PayloadPersistenceResponse offloadResponse) throws Exception {
        log.entering(this.getClass().getName(), "doPatchOrUpdatePersist");

        // We'll only start a new transaction here if we don't have one. We'll only
        // commit at the end if we started one here
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        txn.begin();

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        FHIRRestOperationResponse ior = new FHIRRestOperationResponse();

        try {
            // Ensure the persistence event references both original and new resources
            event.setFhirResource(newResource);
            event.setPrevFhirResource(prevResource);

            // Remember, update now doesn't mutate the resource in any way, and nor should the event
            checkIdAndMeta(newResource);

            FHIRPersistenceContext persistenceContext =
                    FHIRPersistenceContextImpl.builder(event)
                    .withIfNoneMatch(ifNoneMatch)
                    .withOffloadResponse(offloadResponse)
                    .withRequestShard(requestContext.getRequestShardKey())
                    .build();

            boolean createOnUpdate = (prevResource == null);
            final SingleResourceResult<Resource> result;
            if (createOnUpdate) {
                // resource shouldn't exist, so we assume it's a create
                result = persistence.create(persistenceContext, newResource);
            } else {
                // resource already exists, so we know it's an update
                result = persistence.update(persistenceContext, newResource);
            }

            if (result.isSuccess() && result.getOutcome() != null) {
                warnings.addAll(result.getOutcome().getIssue());
            }

            // Since 1869 the persistence layer no longer modifies the resource, so we use the original value here
            ior.setResource(newResource);
            ior.setOperationOutcome(FHIRUtil.buildOperationOutcome(warnings));

            // Invoke the 'afterUpdate' interceptor methods.
            if (createOnUpdate) {
                // No previous resource found in initial read, so we attempted to
                // create a new one
                if (result.getStatus() == InteractionStatus.IF_NONE_MATCH_EXISTED) {

                    // Another thread snuck in and created the resource. Because the client requested
                    // If-None-Match, we skip any update and return 304 Not Modified.
                    handleIfNoneMatchExisted(type, id, ior, result.getIfNoneMatchVersion());
                } else {
                    ior.setStatus(Response.Status.CREATED);
                    ior.setLocationURI(FHIRUtil.buildLocationURI(type, newResource));
                    event.getProperties().put(FHIRPersistenceEvent.PROPNAME_RESOURCE_LOCATION_URI, ior.getLocationURI().toString());
                    getInterceptorMgr().fireAfterCreateEvent(event);
                }
            } else {
                // prevResource exists
                if (result.getStatus() == InteractionStatus.IF_NONE_MATCH_EXISTED) {
                    // this should have been handled before the db interaction; this is just a double check
                    log.warning("If-None-Match precondition check succeeded in REST layer but shouldn't have.");
                    handleIfNoneMatchExisted(type, id, ior, result.getIfNoneMatchVersion());
                } else {
                    // update, so make sure the location is configured correctly for the event
                    ior.setLocationURI(FHIRUtil.buildLocationURI(type, newResource));
                    event.getProperties().put(FHIRPersistenceEvent.PROPNAME_RESOURCE_LOCATION_URI, ior.getLocationURI().toString());
                    ior.setStatus(Response.Status.OK);
                    if (isPatch) {
                        getInterceptorMgr().fireAfterPatchEvent(event);
                    } else {
                        getInterceptorMgr().fireAfterUpdateEvent(event);
                    }
                }
            }

            // If the deleted resource is updated, then simply return 201 instead of 200 to pass Touchstone test.
            // We don't set the previous resource to null in above codes if the resource was deleted, otherwise
            // it will break the code logic of the resource versioning.
            if (isDeleted && ior.getStatus() == Response.Status.OK) {
                ior.setStatus(Response.Status.CREATED);
            }

            // Commit our transaction if we started one before.
            txn.commit();
            txn = null;

            return ior;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            // If we still have a transaction at this point, we need to rollback due to an error.
            if (txn != null) {
                txn.rollback();
            }

            log.exiting(this.getClass().getName(), "doPatchOrUpdatePersist");
        }
    }

    private void handleIfNoneMatchExisted(String type, String id, FHIRRestOperationResponse ior,
            Integer ifNoneMatchVersion) throws FHIRPersistenceIfNoneMatchException {
        ior.setResource(null); // the resource shouldn't be needed for either case (304 or 412)
        ior.setLocationURI(FHIRUtil.buildLocationURI(type, id, ifNoneMatchVersion));
        Boolean ifNoneMatchNotModified = FHIRConfigHelper.getBooleanProperty(
            FHIRConfiguration.PROPERTY_IF_NONE_MATCH_RETURNS_NOT_MODIFIED, Boolean.FALSE);
        if (ifNoneMatchNotModified != null && ifNoneMatchNotModified) {
            // Don't treat as an error
            ior.setStatus(Response.Status.NOT_MODIFIED);
        } else {
            throw new FHIRPersistenceIfNoneMatchException("IfNoneMatch precondition failed.");
        }
    }

    /**
     * Check that the id and meta fields in the resource have been set up
     * @param resource
     * @throws FHIRPersistenceException
     */
    private void checkIdAndMeta(Resource resource) throws FHIRPersistenceException {
        if (resource.getId() == null || resource.getId().isEmpty()) {
            throw new FHIRPersistenceException("resource id field not set");
        }

        if (resource.getMeta() == null) {
            throw new FHIRPersistenceException("resource meta is missing");
        }

        if (resource.getMeta().getVersionId() == null || resource.getMeta().getVersionId().getValue() == null
                || resource.getMeta().getVersionId().getValue().isEmpty()) {
            throw new FHIRPersistenceException("resource meta.versionId not set");
        }

        if (resource.getMeta().getLastUpdated() == null) {
            throw new FHIRPersistenceException("resource meta.lastUpdated not set");
        }
    }

    @Override
    public FHIRRestOperationResponse doDelete(String type, String id, String searchQueryString) throws Exception {
        log.entering(this.getClass().getName(), "doDelete");

        // Validate that interaction is allowed for given resource type
        validateInteraction(Interaction.DELETE, type);

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        FHIRRestOperationResponse ior = new FHIRRestOperationResponse();

        // Make sure we get a transaction started before there's any chance
        // it could be marked for rollback
        txn.begin();

        // A list of supplemental warnings to include in the response
        List<Issue> warnings = new ArrayList<>();

        try {
            if (!ModelSupport.isResourceType(type)) {
                throw buildUnsupportedResourceTypeException(type);
            }

            Class<? extends Resource> resourceType = getResourceType(type);

            // Next, if a conditional delete was invoked then use the search criteria to find the
            // resource to be deleted. Otherwise, we'll use the id value to identify the resource
            // to be deleted.
            Bundle responseBundle = null;

            if (searchQueryString != null) {
                int searchPageSize = FHIRConfigHelper.getIntProperty(FHIRConfiguration.PROPERTY_CONDITIONAL_DELETE_MAX_NUMBER,
                        FHIRConstants.FHIR_CONDITIONAL_DELETE_MAX_NUMBER_DEFAULT);

                if (log.isLoggable(Level.FINE)) {
                    log.fine("Performing conditional delete with search criteria: "
                            + Encode.forHtml(searchQueryString));
                }
                try {
                    MultivaluedMap<String, String> searchParameters = getQueryParameterMap(searchQueryString);
                    searchParameters.putSingle(SearchConstants.COUNT, Integer.toString(searchPageSize));
                    responseBundle = doSearch(type, null, null, searchParameters, null, false, true);
                } catch (FHIROperationException e) {
                    throw e;
                } catch (Throwable t) {
                    String msg = "An error occurred while performing the search for a conditional delete operation.";
                    throw new FHIROperationException(msg, t);
                }

                // Check the search results to determine whether or not to perform the update operation.

                int resultCount = responseBundle.getEntry().size();
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Conditional delete search yielded " + resultCount + " results.");
                }

                if (resultCount == 0) {
                    // Search yielded no matches
                    String msg = "Search criteria for a conditional delete operation yielded no matches.";
                    if (log.isLoggable(Level.FINE)) {
                        log.fine(msg);
                    }
                    ior.setOperationOutcome(FHIRUtil.buildOperationOutcome(msg, IssueType.NOT_FOUND, IssueSeverity.WARNING));
                    ior.setStatus(Status.OK);
                    return ior;
                } else if (responseBundle.getTotal().getValue() > searchPageSize) {
                    String msg = "The search criteria specified for a conditional delete operation returned too many matches ( > " + searchPageSize + " ).";
                    throw buildRestException(msg, IssueType.MULTIPLE_MATCHES);
                }
            } else {
                // Make sure an id value was passed in.
                if (id == null) {
                    String msg = "The 'id' parameter is required for a delete operation.";
                    throw buildRestException(msg, IssueType.REQUIRED);
                }

                // Read the resource so it will be available to the beforeDelete interceptor methods.
                SingleResourceResult<? extends Resource> srr = doRead(type, id, !THROW_EXC_ON_NULL, null, !CHECK_INTERACTION_ALLOWED);
                if (srr.isDeleted()) {
                    // Because the resource is already deleted, we can't create a
                    warnings.add(buildOperationOutcomeIssue(IssueSeverity.WARNING, IssueType.DELETED, "Resource of type '"
                        + type + "' with id '" + id + "' is already deleted."));
                    ior.setVersionForETag(srr.getVersion());
                } else if (srr.getResource() != null) {
                    responseBundle = Bundle.builder().type(BundleType.SEARCHSET)
                            .id(UUID.randomUUID().toString())
                            .entry(Entry.builder().id(id).resource(srr.getResource()).build())
                            .total(UnsignedInt.of(1))
                            .build();
                } else {
                    warnings.add(buildOperationOutcomeIssue(IssueSeverity.WARNING, IssueType.NOT_FOUND, "Cannot find "
                            + type + " with id '" + id + "'."));
                }
            }

            if (responseBundle != null) {

                for (Entry entry: responseBundle.getEntry()) {
                    id = entry.getResource().getId();
                    Resource resourceToDelete = entry.getResource();

                    // For soft-delete we store a new version of the resource with the deleted
                    // flag set. Because we've read the resource already, we need to check that
                    // the version we are deleting matches the version we just read, so the
                    // persistence layer takes the current version as an argument so that it
                    // can perform this check after the logical resource is locked for update.
                    final int currentVersionNumber = FHIRPersistenceSupport.getMetaVersionId(resourceToDelete);
                    final Instant currentLastUpdated = FHIRPersistenceSupport.getLastUpdatedFromResource(resourceToDelete);

                    // Because we no longer store a resource payload along with the deletion marker, there's
                    // no fhirResource value set in the event.
                    FHIRPersistenceEvent event =
                        new FHIRPersistenceEvent(null, buildPersistenceEventProperties(type, id, null, null, null));
                    event.setPrevFhirResource(resourceToDelete);

                    // First, invoke the 'beforeDelete' interceptor methods.
                    getInterceptorMgr().fireBeforeDeleteEvent(event);

                    FHIRPersistenceContext persistenceContext =
                            FHIRPersistenceContextImpl.builder(event)
                            .build();

                    final net.sovrinhealth.fhir.model.type.Instant lastUpdated = FHIRPersistenceSupport.getNewLastUpdatedInstant(currentLastUpdated);
                    persistence.delete(persistenceContext, resourceType, resourceToDelete.getId(), currentVersionNumber, lastUpdated);

                    if (responseBundle.getEntry().size() == 1) {
                        // The response needs to return the version number of the deletion marker
                        // as the ETag. This was previously obtained by returning the modified resource,
                        // which we no longer have.
                        int newVersionNumber = currentVersionNumber + 1;
                        ior.setVersionForETag(newVersionNumber);
                    }

                    // Invoke the 'afterDelete' interceptor methods. To support the notification service, we
                    // need to provide a resource with the lastUpdated element set. This is just to simplify
                    // passing values, even though the resource itself doesn't really exist
                    final int newVersionNumber = currentVersionNumber + 1;
                    Resource deletionMarker = FHIRPersistenceUtil.copyAndSetResourceMetaFields(resourceToDelete, resourceToDelete.getId(), newVersionNumber, lastUpdated);
                    event.setFhirResource(deletionMarker);
                    getInterceptorMgr().fireAfterDeleteEvent(event);
                }

                warnings.add(Issue.builder()
                        .severity(IssueSeverity.INFORMATION)
                        .code(IssueType.INFORMATIONAL)
                        .details(CodeableConcept.builder()
                            .text(string("Deleted " + responseBundle.getEntry().size() + " " + type + " resource(s) " +
                                "with the following id(s): " +
                                responseBundle.getEntry().stream().map(e -> e.getResource().getId()).collect(Collectors.joining(","))))
                            .build())
                        .build());

                // Commit our transaction if we started one before.
                txn.commit();
                txn = null;
            }

            // The server should return either a 200 OK if the response contains a payload, or a 204 No Content with no response payload
            if (!warnings.isEmpty()) {
                ior.setOperationOutcome(FHIRUtil.buildOperationOutcome(warnings));
                ior.setStatus(Status.OK);
            } else {
                ior.setStatus(Status.NO_CONTENT);
            }

            return ior;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            // If we previously started a transaction and it's still active, we need to rollback due to an error.
            if (txn != null) {
                txn.rollback();
            }

            log.exiting(this.getClass().getName(), "doDelete");
        }
    }

    @Override
    public SingleResourceResult<? extends Resource> doRead(String type, String id, boolean throwExcOnNull,
            MultivaluedMap<String, String> queryParameters) throws Exception {
        return doRead(type, id, throwExcOnNull, queryParameters, true);
    }

    /**
     * Performs a 'read' operation to retrieve a Resource.
     *
     * @param type
     *            the resource type associated with the Resource to be retrieved
     * @param id
     *            the id of the Resource to be retrieved
     * @param throwExcOnNull
     *            if true, throw an exception if returned resource is null
     * @param queryParameters
     *            for supporting _elements and _summary for resource read
     * @param checkInteractionAllowed
     *            if true, check if this interaction is allowed per the tenant configuration; if false, assume interaction is allowed
     * @return a {@link SingleResourceResult} containing the ResourceResult for the interaction
     * @throws Exception
     */
    private SingleResourceResult<? extends Resource> doRead(String type, String id, boolean throwExcOnNull,
            MultivaluedMap<String, String> queryParameters, boolean checkInteractionAllowed) throws Exception {
        log.entering(this.getClass().getName(), "doRead");

        SingleResourceResult<? extends Resource> result;

        // Validate that interaction is allowed for given resource type
        if (checkInteractionAllowed) {
            validateInteraction(Interaction.READ, type);
        }

        // Start a new txn in the persistence layer if one is not already active.
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        txn.begin();

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        try {
            String resourceTypeName = type;
            if (!ModelSupport.isResourceType(type)) {
                throw buildUnsupportedResourceTypeException(type);
            }

            Class<? extends Resource> resourceType = getResourceType(resourceTypeName);

            FHIRSearchContext searchContext = null;
            if (queryParameters != null) {
                searchContext = searchHelper.parseReadQueryParameters(resourceType, queryParameters, Interaction.READ.value(),
                    HTTPHandlingPreference.LENIENT.equals(requestContext.getHandlingPreference()), fhirVersion);
            }

            // First, invoke the 'beforeRead' interceptor methods.
            FHIRPersistenceEvent event =
                    new FHIRPersistenceEvent(buildPersistenceEventProperties(type, id, null, searchContext, null));
            getInterceptorMgr().fireBeforeReadEvent(event);

            FHIRPersistenceContext persistenceContext =
                    FHIRPersistenceContextFactory.createPersistenceContext(event, searchContext, requestContext.getRequestShardKey());
            result = persistence.read(persistenceContext, resourceType, id);
            event.setFhirResource(result.getResource());

            // Invoke the 'afterRead' interceptor methods.
            getInterceptorMgr().fireAfterReadEvent(event);

            // Update the result if the interceptor changed it
            result = result.replace(event.getFhirResource());

            if (result.getResource() == null && throwExcOnNull) {
                if (result.isDeleted()) {
                    throw new FHIRResourceDeletedException("Resource '" + type + "/" + id + "' is deleted.");
                } else {
                    throw new FHIRResourceNotFoundException("Resource '" + type + "/" + id + "' not found.");
                }
            }

            // Commit our transaction if we started one before.
            txn.commit();
            txn = null;

            return result;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            // If we previously started a transaction and it's still active, we need to rollback due to an error.
            if (txn != null) {
                txn.rollback();
            }

            log.exiting(this.getClass().getName(), "doRead");
        }
    }

    @Override
    public SingleResourceResult<? extends Resource> doVRead(String type, String id, String versionId, MultivaluedMap<String, String> queryParameters)
            throws Exception {
        log.entering(this.getClass().getName(), "doVRead");

        // Validate that interaction is allowed for given resource type
        validateInteraction(Interaction.VREAD, type);

        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        // Start a new txn in the persistence layer if one is not already active.
        txn.begin();

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        try {
            String resourceTypeName = type;
            if (!ModelSupport.isResourceType(type)) {
                throw buildUnsupportedResourceTypeException(type);
            }

            Class<? extends Resource> resourceType = getResourceType(resourceTypeName);

            FHIRSearchContext searchContext = null;
            if (queryParameters != null) {
                searchContext = searchHelper.parseReadQueryParameters(resourceType, queryParameters, Interaction.VREAD.value(),
                    HTTPHandlingPreference.LENIENT.equals(requestContext.getHandlingPreference()), fhirVersion);
            }

            // First, invoke the 'beforeVread' interceptor methods.
            FHIRPersistenceEvent event =
                    new FHIRPersistenceEvent(null, buildPersistenceEventProperties(type, id, versionId, searchContext, null));
            getInterceptorMgr().fireBeforeVreadEvent(event);

            FHIRPersistenceContext persistenceContext =
                    FHIRPersistenceContextFactory.createPersistenceContext(event, searchContext, requestContext.getRequestShardKey());
            SingleResourceResult<? extends Resource> srr = persistence.vread(persistenceContext, resourceType, id, versionId);

            // The resource may be null if it doesn't exist or has been deleted
            event.setFhirResource(srr.getResource());

            // Invoke the 'afterVread' interceptor methods.
            getInterceptorMgr().fireAfterVreadEvent(event);

            // Update the result if the interceptor changed it
            srr = srr.replace(event.getFhirResource());

            if (srr.getResource() == null) {
                if (srr.isDeleted()) {
                    throw new FHIRResourceDeletedException("Resource '" + type + "/" + id + "'"
                            + " version " + versionId + " is deleted.");
                } else {
                    throw new FHIRResourceNotFoundException("Resource '" + type + "/" + id + "'"
                            + " version " + versionId + " not found.");
                }
            }

            // Commit our transaction if we started one before.
            txn.commit();
            txn = null;

            return srr;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            // If we previously started a transaction and it's still active, we need to rollback due to an error.
            if (txn != null) {
                txn.rollback();
            }

            log.exiting(this.getClass().getName(), "doVRead");
        }
    }

    @Override
    public Bundle doHistory(String type, String id, MultivaluedMap<String, String> queryParameters, String requestUri)
            throws Exception {
        log.entering(this.getClass().getName(), "doHistory");

        // Validate that interaction is allowed for given resource type
        validateInteraction(Interaction.HISTORY, type);

        // Start a new txn in the persistence layer if one is not already active.
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        txn.begin();

        Bundle bundle = null;

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        try {
            String resourceTypeName = type;
            if (!ModelSupport.isResourceType(type)) {
                throw buildUnsupportedResourceTypeException(type);
            }

            Class<? extends Resource> resourceType = getResourceType(resourceTypeName);
            FHIRHistoryContext historyContext = FHIRPersistenceUtil.parseHistoryParameters(queryParameters,
                    HTTPHandlingPreference.LENIENT.equals(requestContext.getHandlingPreference()));

            // First, invoke the 'beforeHistory' interceptor methods.
            FHIRPersistenceEvent event =
                    new FHIRPersistenceEvent(null, buildPersistenceEventProperties(type, id, null, null, null));
            getInterceptorMgr().fireBeforeHistoryEvent(event);

            FHIRPersistenceContext persistenceContext =
                    FHIRPersistenceContextFactory.createPersistenceContext(event, historyContext);
            MultiResourceResult historyResult =
                    persistence.history(persistenceContext, resourceType, id);
            bundle = createHistoryBundle(historyResult.getResourceResults(), historyContext, type);
            bundle = addLinks(historyContext, bundle, requestUri, null, null, null, null);

            event.setFhirResource(bundle);

            // Invoke the 'afterHistory' interceptor methods.
            getInterceptorMgr().fireAfterHistoryEvent(event);

            // Update the result if the interceptor changed it
            Resource x = event.getFhirResource();
            if (x != null && x.is(Bundle.class)) {
                bundle = x.as(Bundle.class);
            }

            // Commit our transaction if we started one before.
            txn.commit();
            txn = null;

            return bundle;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            // If we previously started a transaction and it's still active, we need to rollback due to an error.
            if (txn != null) {
                txn.rollback();
            }

            log.exiting(this.getClass().getName(), "doHistory");
        }
    }

    @Override
    public Bundle doSearch(String type, String compartment, String compartmentId,
            MultivaluedMap<String, String> queryParameters, String requestUri) throws Exception {
        return doSearch(type, compartment, compartmentId, queryParameters, requestUri, true, false);
    }

    @Override
    public Bundle doSearch(String type, String compartment, String compartmentId,
            MultivaluedMap<String, String> queryParameters, String requestUri,
            boolean checkInteractionAllowed, boolean alwaysIncludeResources) throws Exception {
        log.entering(this.getClass().getName(), "doSearch");

        // Validate that interaction is allowed for given resource type
        if (checkInteractionAllowed) {
            validateInteraction(Interaction.SEARCH, type);
        }

        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        // Start a new txn in the persistence layer if one is not already active.
        txn.begin();

        Bundle bundle = null;

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        try {
            String resourceTypeName = type;

            // Check to see if it's supported, else, throw a bad request.
            // If this is removed, it'll result in nullpointer when processing the request
            if (!ModelSupport.isResourceType(type)) {
                throw buildUnsupportedResourceTypeException(type);
            }

            Class<? extends Resource> resourceType = getResourceType(resourceTypeName);

            final boolean isLenientHandling = HTTPHandlingPreference.LENIENT == requestContext.getHandlingPreference();
            final boolean includeResources = alwaysIncludeResources || HTTPReturnPreference.MINIMAL != requestContext.getReturnPreference() || requestContext.isReturnPreferenceDefault();
            if (!includeResources) {
                log.info("Not including resources");
            }
            FHIRSearchContext searchContext = searchHelper.parseCompartmentQueryParameters(compartment, compartmentId, resourceType, queryParameters,
                isLenientHandling, includeResources, fhirVersion);

            // First, invoke the 'beforeSearch' interceptor methods.
            FHIRPersistenceEvent event =
                    new FHIRPersistenceEvent(buildPersistenceEventProperties(type, null, null, searchContext, null));
            getInterceptorMgr().fireBeforeSearchEvent(event);

            FHIRPersistenceContext persistenceContext =
                    FHIRPersistenceContextFactory.createPersistenceContext(event, searchContext, requestContext.getRequestShardKey());
            MultiResourceResult searchResult =
                    persistence.search(persistenceContext, resourceType);
            bundle = createSearchResponseBundle(searchResult.getResourceResults(), searchContext, type);

            if (requestUri != null) {
                bundle = addLinks(searchContext, bundle, requestUri, searchResult.geFirstId(), searchResult.getLastId(), searchResult.getExpectedNextId(), searchResult.getExpectedPreviousId());
            }
            event.setFhirResource(bundle);

            // Invoke the 'afterSearch' interceptor methods.
            getInterceptorMgr().fireAfterSearchEvent(event);

            // Interceptors might want to change the response bundle, so make sure pick up the new value.
            // Protect against an interceptor returning something other than a Bundle
            Resource afterSearchEventValue = event.getFhirResource();
            if (afterSearchEventValue != null && afterSearchEventValue.is(Bundle.class)) {
                bundle = afterSearchEventValue.as(Bundle.class);
            }

            // Commit our transaction if we started one before.
            txn.commit();
            txn = null;

            return bundle;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            // If we previously started a transaction and it's still active, we need to rollback due to an error.
            if (txn != null) {
                txn.rollback();
            }

            log.exiting(this.getClass().getName(), "doSearch");
        }
    }

    /**
     * Get the resource Id of the input resourceResult.
     * @param resourceResult
     * @return The resource Id of the input resourceResult.
     */
    private String getResourceId(ResourceResult<? extends Resource> resourceResult) {
        if (resourceResult == null || resourceResult.getResource() == null) {
            return null;
        }
        return resourceResult.getResource().getId();

    }

    @Override
    public Resource doInvoke(FHIROperationContext operationContext, String resourceTypeName, String logicalId,
            String versionId, Resource resource, MultivaluedMap<String, String> queryParameters) throws Exception {
        log.entering(this.getClass().getName(), "doInvoke");

        // Save the current request context.
        FHIRRequestContext requestContext = FHIRRequestContext.get();
        String operationName = operationContext.getOperationCode();

        try {
            Class<? extends Resource> resourceType = null;
            if (resourceTypeName != null) {
                resourceType = getResourceType(resourceTypeName);
            }
            String operationKey = (resourceTypeName == null ? operationName : operationName + ":" + resourceTypeName);

            FHIROperation operation = FHIROperationRegistry.getInstance().getOperation(operationKey);
            Parameters parameters = null;
            if (resource instanceof Parameters) {
                parameters = (Parameters) resource;
            } else {
                if (resource == null) {
                    // build parameters object from query parameters
                    parameters =
                            FHIROperationUtil.getInputParameters(operation.getDefinition(), queryParameters);
                } else {
                    // wrap resource in a parameters object
                    parameters =
                            FHIROperationUtil.getInputParameters(operation.getDefinition(), resource);
                }
            }

            // Add properties to the FHIR operation context
            setOperationContextProperties(operationContext, resourceTypeName, parameters);

            getInterceptorMgr().fireBeforeInvokeEvent(operationContext);

            if (log.isLoggable(Level.FINE)) {
                log.fine("Invoking operation '" + operationName + "', context=\n"
                        + operationContext.toString());
            }
            Parameters result =
                    operation.invoke(operationContext, resourceType, logicalId, versionId, parameters, this, searchHelper);
            operationContext.setProperty(FHIROperationContext.PROPNAME_RESPONSE_PARAMETERS, result);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Returned from invocation of operation '" + operationName + "'...");
            }

            getInterceptorMgr().fireAfterInvokeEvent(operationContext);

            // Grab the result from the operationContext in case an interceptor modified it
            result = (Parameters) operationContext.getProperty(FHIROperationContext.PROPNAME_RESPONSE_PARAMETERS);

            // if single resource output parameter, return the resource
            if (FHIROperationUtil.hasSingleResourceOutputParameter(result)) {
                return FHIROperationUtil.getSingleResourceOutputParameter(result);
            }

            return result;
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            log.exiting(this.getClass().getName(), "doInvoke");
        }
    }

    @Override
    public Bundle doBundle(Bundle inputBundle, boolean skippableUpdates) throws Exception {
        log.entering(this.getClass().getName(), "doBundle");

        FHIRRequestContext requestContext = FHIRRequestContext.get();

        try {
            // First, validate the bundle and save the error / warning responses by index entry.
            Map<Integer, Entry> validationResponseEntries = validateBundle(inputBundle);

            // Next, process each of the entries in the bundle.
            return processBundleEntries(inputBundle, validationResponseEntries, skippableUpdates);
        } finally {
            // Restore the original request context.
            FHIRRequestContext.set(requestContext);

            log.exiting(this.getClass().getName(), "doBundle");
        }
    }

    @Override
    public FHIRPersistenceTransaction getTransaction() throws Exception {
        return persistence.getTransaction();
    }

    /**
     * Validate the input resource and throw if there are validation errors
     *
     * @param resource
     * @throws FHIRValidationException if an error occurs during validation
     * @throws FHIROperationException if there are validation errors
     * @return A list of validation warnings
     */
    private List<OperationOutcome.Issue> validateInput(Resource resource)
            throws FHIROperationException {
        List<OperationOutcome.Issue> issues = validateResource(resource);
        if (!issues.isEmpty()) {
            for (OperationOutcome.Issue issue : issues) {
                if (FHIRUtil.isFailure(issue.getSeverity())) {
                    throw new FHIROperationException("Input resource failed validation.").withIssue(issues);
                }
            }

            if (log.isLoggable(Level.FINE)) {
                String info = issues.stream()
                        .flatMap(issue -> Stream.of(issue.getDetails()))
                        .flatMap(details -> Stream.of(details.getText()))
                        .flatMap(text -> Stream.of(text.getValue()))
                        .collect(Collectors.joining(", "));
                log.fine("Validation warnings for input resource: " + info);
            }
        }
        return issues;
    }

    /**
     * Performs validation of a request Bundle and returns a Map of entry indices to error / warning
     * response entries that correspond to the entries in the request Bundle.
     *
     * @param bundle
     *            the bundle to be validated
     * @return a map of entry indices to error responses / warnings; empty if there are no validation warnings or errors
     * @throws Exception
     */
    private Map<Integer, Entry> validateBundle(Bundle bundle) throws Exception {
        log.entering(this.getClass().getName(), "validateBundle");
        Map<Integer, Entry> validationResponseEntries = new HashMap<>();

        try {
            // Make sure the bundle isn't empty
            if (bundle == null) {
                String msg = "Bundle parameter is missing or empty.";
                throw buildRestException(msg, IssueType.REQUIRED);
            }

            BundleType.Value requestType = bundle.getType().getValueAsEnum();
            if (requestType != BundleType.Value.BATCH && requestType != BundleType.Value.TRANSACTION) {
                // TODO add support for posting history bundles. Note that when that support
                // is added, some of the following bundle constraint checks will need to be updated
                // since they are assuming a bundle type of 'batch' or 'transaction'.
                String msg = "Bundle.type must be either 'batch' or 'transaction'.";
                throw buildRestException(msg, IssueType.VALUE);
            }
            if (requestType == BundleType.Value.TRANSACTION && !persistence.isTransactional()) {
                // For a 'transaction' interaction, if the underlying persistence layer doesn't support
                // transactions, then throw an error.
                String msg = "Bundled 'transaction' request cannot be processed because "
                        + "the configured persistence layer does not support transactions.";
                IssueType extendedIssueType = IssueType.NOT_SUPPORTED.toBuilder()
                        .extension(Extension.builder()
                            .url(EXT_BASE +  "not-supported-detail")
                            .value(Code.of("interaction"))
                            .build())
                        .build();
                throw buildRestException(msg, extendedIssueType);
            }
            if (bundle.getTotal() != null) {
                // Verify that the total field is not present for 'batch' or
                // 'transaction' type bundles (Bundle constraint bdl-1).
                String msg = "Bundle.total must be empty.";
                throw buildRestException(msg, IssueType.INVALID);
            }

            // For 'transaction' bundle requests, keep a list of issues in case of failure
            List<OperationOutcome.Issue> issueList = new ArrayList<OperationOutcome.Issue>();

            Set<String> localIdentifiers = new HashSet<>();
            Set<String> fullUrls = new HashSet<>();

            for (int i = 0; i < bundle.getEntry().size(); i++) {
                // Create a corresponding response entry and add it to the response bundle.
                Bundle.Entry requestEntry = bundle.getEntry().get(i);
                Bundle.Entry responseEntry = null;

                // Validate 'requestEntry' and update 'responseEntry' with any errors.
                try {
                    Bundle.Entry.Request request = requestEntry.getRequest();

                    // Verify that the request field is present for 'batch' or 'transaction'
                    // type bundles (Bundle constraint bdl-3, also covers Bundle constraint bdl-5).
                    if (request == null) {
                        String msg = "Bundle.Entry is missing the 'request' field.";
                        throw buildRestException(msg, IssueType.REQUIRED);
                    }

                    // Verify that a method was specified.
                    if (request.getMethod() == null || request.getMethod().getValue() == null) {
                        String msg = "Bundle.Entry.request is missing the 'method' field";
                        throw buildRestException(msg, IssueType.REQUIRED);
                    }

                    // Verify that a URL was specified.
                    if (request.getUrl() == null || request.getUrl().getValue() == null) {
                        String msg = "Bundle.Entry.request is missing the 'url' field";
                        throw buildRestException(msg, IssueType.REQUIRED);
                    }

                    // Verify that the fullUrl field is not a duplicate if it specifies a local reference
                    // and if the request method is POST or PUT.
                    String fullUrl = requestEntry.getFullUrl() != null ? requestEntry.getFullUrl().getValue() : null;
                    if ((request.getMethod().equals(HTTPVerb.POST) || request.getMethod().equals(HTTPVerb.PUT))
                            && fullUrl != null) {
                        String localIdentifier = retrieveLocalIdentifier(fullUrl);
                        if (localIdentifier != null) {
                            if (localIdentifiers.contains(localIdentifier)) {
                                String msg = "Duplicate local identifier encountered in bundled request entry: " + localIdentifier;
                                throw buildRestException(msg, IssueType.DUPLICATE);
                            }
                            localIdentifiers.add(localIdentifier);
                        }
                    }

                    // Verify that the search field is not present for 'batch' or 'transaction'
                    // type bundles (Bundle constraint bdl-2).
                    if (requestEntry.getSearch() != null) {
                        String msg = "Bundle.Entry.search must be empty.";
                        throw buildRestException(msg, IssueType.INVALID);
                    }

                    // Verify that the response field is not present for 'batch' or 'transaction'
                    // type bundles (Bundle constraint bdl-4).
                    if (requestEntry.getResponse() != null) {
                        String msg = "Bundle.Entry.response must be empty.";
                        throw buildRestException(msg, IssueType.INVALID);
                    }

                    // Verify that the fullUrl field is not a version specific reference
                    // (Bundle constraint bdl-8) and that the fullUrl field + resource.meta.versionId
                    // is unique for 'batch' or 'transaction' type bundles (Bundle constraint bdl-7)
                    Resource resource = requestEntry.getResource();
                    if (fullUrl != null) {
                        if (fullUrl.contains("/_history/")) {
                            String msg = "Bundle.Entry.fullUrl cannot be a version specific reference.";
                            throw buildRestException(msg, IssueType.VALUE);
                        }
                        String fullUrlPlusVersion = fullUrl;
                        if (resource != null && resource.getMeta() != null
                                && resource.getMeta().getVersionId() != null && resource.getMeta().getVersionId().hasValue()) {
                            fullUrlPlusVersion = fullUrl + resource.getMeta().getVersionId().getValue();
                        } else {
                            fullUrlPlusVersion = fullUrl;
                        }
                        if (fullUrls.contains(fullUrlPlusVersion)) {
                            String msg = "Duplicate Bundle.Entry.fullUrl encountered in bundled request entry: " + fullUrl;
                            throw buildRestException(msg, IssueType.DUPLICATE);
                        }
                        fullUrls.add(fullUrlPlusVersion);
                    }

                    // Validate the resource for the requested HTTP method.
                    methodValidation(request.getMethod(), resource);

                    // If the request entry contains a resource, then validate it now.
                    if (resource != null) {
                        List<Issue> issues = validateResource(resource);
                        if (!issues.isEmpty()) {
                            OperationOutcome oo = FHIRUtil.buildOperationOutcome(issues);
                            if (FHIRUtil.anyFailureInIssues(issues)) {
                                if (requestType == BundleType.Value.TRANSACTION) {
                                    issueList.addAll(issues);
                                } else {
                                    responseEntry = Entry.builder()
                                                .response(Entry.Response.builder()
                                                    .status(SC_BAD_REQUEST_STRING)
                                                    .build())
                                                .resource(oo)
                                                .build();
                                }
                            } else {
                                responseEntry = Entry.builder()
                                        .response(Entry.Response.builder()
                                            .status(SC_ACCEPTED_STRING)
                                            .outcome(oo)
                                            .build())
                                        .build();
                            }
                        }
                    }
                } catch (FHIROperationException e) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "Failed to process BundleEntry ["
                                + bundle.getEntry().indexOf(requestEntry) + "]", e);
                    }
                    if (requestType == BundleType.Value.TRANSACTION) {
                        issueList.addAll(e.getIssues());
                    } else {
                        Entry.Response response = Entry.Response.builder()
                                .status(SC_BAD_REQUEST_STRING)
                                .build();
                        responseEntry = Entry.builder()
                                .response(response)
                                .resource(FHIRUtil.buildOperationOutcome(e, false))
                                .build();
                    }
                } finally {
                    if (responseEntry != null) {
                        validationResponseEntries.put(i, responseEntry);
                    }
                }
            } // End foreach requestEntry

            // If this is a "transaction" interaction and we encountered any errors, then we'll
            // abort processing this request right now since a transaction interaction is supposed to be
            // all or nothing.
            if (requestType == BundleType.Value.TRANSACTION && issueList.size() > 0) {
                String msg =
                        "One or more errors were encountered while validating a 'transaction' request bundle.";
                throw buildRestException(msg, IssueType.INVALID).withIssue(issueList);
            }

            return validationResponseEntries;
        } finally {
            log.exiting(this.getClass().getName(), "validateBundle");
        }
    }

    /**
     * Perform method-specific validation of the resource
     */
    private void methodValidation(HTTPVerb method, Resource resource) throws FHIRPersistenceException, FHIROperationException {
        switch(method.getValueAsEnum()) {
        case PATCH:
        case POST:
            break;
        case DELETE:
            // If the "delete" operation isn't supported by the configured persistence layer,
            // then we need to fail validation of this bundle entry.
            if (!persistence.isDeleteSupported()) {
                String msg = "Bundle.Entry.request contains unsupported HTTP method: "
                        + method.getValue();
                IssueType extendedIssueType = IssueType.NOT_SUPPORTED.toBuilder()
                        .extension(Extension.builder()
                            .url(EXT_BASE +  "not-supported-detail")
                            .value(Code.of("interaction"))
                            .build())
                        .build();
                throw buildRestException(msg, extendedIssueType);
            }
            // Purposefully fall through to next clause
        case HEAD:
        case GET:
            if (resource != null) {
                String msg =
                        "Bundle.Entry.resource not allowed for BundleEntry with " + method.getValue() + " method.";
                throw buildRestException(msg, IssueType.INVALID);
            }
            break;
        case PUT:
            if (resource == null) {
                String msg =
                        "Bundle.Entry.resource is required for BundleEntry with PUT method.";
                throw buildRestException(msg, IssueType.INVALID);
            }
            break;
        default:
            String msg = "Bundle.Entry.request contains unsupported HTTP method: " + method.getValue();
            throw buildRestException(msg, IssueType.INVALID);
        }
    }

    /**
     * This function will perform the version-aware update check by making sure that the If-Match request header value
     * (if present) specifies a version # equal to the current latest version of the resource. If the check fails, then
     * a FHIRRestException will be thrown. If the check succeeds then nothing occurs and processing continues.
     *
     * @param currentResource
     *            the current latest version of the resource
     * @param ifMatchValue
     *            the string value of the If-Match header
     */
    private void performVersionAwareUpdateCheck(Resource currentResource, String ifMatchValue) throws FHIROperationException {
        if (ifMatchValue != null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Performing a version aware update. ETag value =  " + ifMatchValue);
            }

            String ifMatchVersion = getVersionIdFromETagValue(ifMatchValue);

            // Make sure that we got a version # from the request header.
            // If not, then return a 400 Bad Request status code.
            if (ifMatchVersion == null || ifMatchVersion.isEmpty()) {
                throw buildRestException("Invalid ETag value specified in request: "
                        + ifMatchValue, IssueType.PROCESSING);
            }

            if (log.isLoggable(Level.FINE)) {
                log.fine("Version id from ETag value specified in request: " + ifMatchVersion);
            }

            // Retrieve the version #'s from the current and updated resources.
            String currentVersion = null;
            if (currentResource.getMeta() != null
                    && currentResource.getMeta().getVersionId() != null) {
                currentVersion = currentResource.getMeta().getVersionId().getValue();
            }

            // Next, make sure that the If-Match version matches the version # found
            // in the current latest version of the resource.
            // If they don't match we'll return an HTTP 412 (Precondition Failed) status code.
            if (!ifMatchVersion.equals(currentVersion)) {
                String msg = "If-Match version '" + ifMatchVersion
                        + "' does not match current latest version of resource: " + currentVersion;
                IssueType extendedIssueType = IssueType.CONFLICT.toBuilder()
                        .extension(Extension.builder()
                            .url(EXT_BASE + "http-failed-precondition")
                            .value(string("If-Match"))
                            .build())
                        .build();
                throw buildRestException(msg, extendedIssueType);
            }
        }
    }

    private FHIROperationException buildUnsupportedResourceTypeException(String resourceTypeName) {
        String msg = "'" + Encode.forHtml(resourceTypeName) + "' is not a valid resource type.";
        Issue issue = OperationOutcome.Issue.builder()
                .severity(IssueSeverity.FATAL)
                .code(IssueType.NOT_SUPPORTED.toBuilder()
                        .extension(Extension.builder()
                            .url(EXT_BASE +  "not-supported-detail")
                            .value(Code.of("resource"))
                            .build())
                        .build())
                .details(CodeableConcept.builder().text(string(msg)).build())
                .build();
        return new FHIROperationException(msg).withIssue(issue);
    }

    private FHIROperationException buildRestException(String msg, IssueType issueType) {
        return buildRestException(msg, issueType, IssueSeverity.FATAL);
    }

    private FHIROperationException buildRestException(String msg, IssueType issueType, IssueSeverity severity) {
        return new FHIROperationException(msg).withIssue(buildOperationOutcomeIssue(severity, issueType, msg));
    }

    /**
     * Builds an OperationOutcomeIssue with the respective values for some of the fields.
     */
    private OperationOutcome.Issue buildOperationOutcomeIssue(IssueSeverity severity, IssueType type, String msg) {
        return OperationOutcome.Issue.builder()
                .severity(severity)
                .code(type)
                .details(CodeableConcept.builder().text(string(msg)).build())
                .build();
    }

    /**
     * Retrieves the version id value from an ETag header value. The ETag header value will be of the form:
     * W/"<version-id>".
     *
     * @param ifMatchValue
     *            the value of the If-Match request header.
     */
    private String getVersionIdFromETagValue(String ifMatchValue) {
        String result = null;
        if (ifMatchValue != null) {
            if (ifMatchValue.startsWith("W/")) {
                String s = ifMatchValue.substring(2);
                // If the part after "W/" starts and ends with a ",
                // then extract the part between the " characters and we're done.
                if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
                    result = s.substring(1, s.length() - 1);
                }
            }
        }
        return result;
    }

    /**
     * This function will process each request contained in the specified request bundle, and update the response bundle
     * with the appropriate response information.
     *
     * @param requestBundle
     *            the bundle containing the requests
     * @param validationResponseEntries
     *            a map from entry indices to the corresponding response entries created during validation
     * @param skippableUpdates
     *            if true, and the bundle contains an update for which the resource content in the update matches the existing
     *            resource on the server, then skip the update; if false, then always attempt the updates specified in the bundle
     * @return a response bundle
     */
    private Bundle processBundleEntries(Bundle requestBundle, Map<Integer, Entry> validationResponseEntries,
            boolean skippableUpdates) throws Exception {
        log.entering(this.getClass().getName(), "processBundleEntries");

        // Generate a request correlation id for this request bundle.
        bundleRequestCorrelationId = UUID.randomUUID().toString();
        if (log.isLoggable(Level.FINE)) {
            log.fine("Processing request bundle, request-correlation-id=" + bundleRequestCorrelationId);
        }

        try {
            // Build a mapping of local identifiers to external identifiers for local reference resolution.
            // Map<String, String> localRefMap = buildLocalRefMap(requestBundle, validationResponseEntries);
            // Since 1869, the local ref map is populated in the phase 1 loop through the bundle
            // entries where all the ids are assigned and lookups performed.

            // Process entries.
            BundleType.Value bundleType = requestBundle.getType().getValueAsEnum();

            // Translate the entries in the bundle to a list of FHIRRestOperation commands which we
            // then process in order
            final boolean isTransactionBundle = bundleType == BundleType.Value.TRANSACTION;
            FHIRRestBundleHelper bundleHelper = new FHIRRestBundleHelper(this);
            List<FHIRRestInteraction> bundleInteractions = bundleHelper.translateBundleEntries(requestBundle,
                    validationResponseEntries, isTransactionBundle, bundleRequestCorrelationId, skippableUpdates);
            List<Entry> responseEntries = processBundleInteractions(bundleInteractions, validationResponseEntries, isTransactionBundle);

            // Build the response bundle.
            // TODO add support for posting history bundles
            Bundle.Builder bundleResponseBuilder = Bundle.builder().entry(responseEntries);
            if (bundleType == BundleType.Value.BATCH) {
                bundleResponseBuilder.type(BundleType.BATCH_RESPONSE);
            } else if (bundleType == BundleType.Value.TRANSACTION) {
                bundleResponseBuilder.type(BundleType.TRANSACTION_RESPONSE);
            }

            return bundleResponseBuilder.build();

        } finally {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Finished processing request bundle, request-correlation-id="
                    + bundleRequestCorrelationId);
            }

            // Clear the request correlation id field since we're done processing the bundle.
            bundleRequestCorrelationId = null;

            log.exiting(this.getClass().getName(), "processBundleEntries");
        }
    }

    /**
     * Process the given list of FHIRRestInteraction in order
     * @param bundleInteractions
     * @param validationResponseEntries
     * @param transaction
     * @return
     * @throws Exception
     */
    private List<Entry> processBundleInteractions(List<FHIRRestInteraction> bundleInteractions,
            Map<Integer, Entry> validationResponseEntries, boolean transaction) throws Exception {
        assert(bundleInteractions.size() > 0);
        Entry[] responseEntries = new Entry[bundleInteractions.size()];
        Map<String, String> localRefMap = new HashMap<>();

        // Run the prepare for all the bundle operations first. This allows us to perform
        // some async operations which we can fetch the results for later, which can
        // significantly reduce the overall request response time - especially important
        // for large bundles.
        FHIRTransactionHelper txn = null;
        try {
            // Always start the transaction here because we may need it to handle searches and reads
            // during the phase 1 loop. This is read-only
            txn = new FHIRTransactionHelper(getTransaction());
            txn.begin();

            // Phase 1: Do any ifNoneExist search and assign new id and meta info. If there is an
            // ifNoneExist hit, it is added to the corresponding responseEntries slot
            FHIRRestInteractionVisitorMeta meta = new FHIRRestInteractionVisitorMeta(transaction, this, localRefMap, responseEntries);
            for (FHIRRestInteraction interaction: bundleInteractions) {
                interaction.accept(meta);
            }

            // When the bundle type is batch, we close out the current transaction so that each interaction
            // will start its own. For transaction bundles, we use a single transaction until the end
            if (!transaction) {
                txn.commit();
                txn = null;
            }

            // Phase 2: Now we have id values for each resource we can update any local references. At this point,
            // the localRefMap should be fixed, so let's enforce that here. Once reference mapping is done, the
            // resource is finalized - if the persistence layer supports payload offloading, we can do that now
            localRefMap = Collections.unmodifiableMap(localRefMap);
            FHIRRestInteractionVisitorReferenceMapping refMapper = new FHIRRestInteractionVisitorReferenceMapping(transaction, this, localRefMap, responseEntries);
            FHIRRestInteractionVisitorOffload offloadVisitor = new FHIRRestInteractionVisitorOffload(transaction, this, localRefMap, responseEntries);
            for (FHIRRestInteraction interaction: bundleInteractions) {
                // Only process stuff we don't yet have a response for
                if (responseEntries[interaction.getEntryIndex()] == null) {
                    interaction.accept(refMapper);
                }

                // Now that the resource will no longer be changed, we can
                // initiate payload offload (when supported)
                if (responseEntries[interaction.getEntryIndex()] == null) {
                    interaction.accept(offloadVisitor);
                }
            }

            // Phase 3: Now run all the persistence operations in the correct order, injecting each result into the
            // appropriate position in the responseEntries array. At the end of the loop, each slot will be filled.
            FHIRRestInteractionVisitorPersist persist = new FHIRRestInteractionVisitorPersist(this, localRefMap, responseEntries, transaction);
            for (FHIRRestInteraction interaction: bundleInteractions) {
                // Only process stuff we don't yet have a response for
                if (responseEntries[interaction.getEntryIndex()] == null) {
                    interaction.accept(persist);
                }
            }
        } catch (Exception x) {
            log.log(Level.SEVERE, "", x);
            if (txn != null) {
                txn.setRollbackOnly();
            }
            throw x;
        } finally {
            // close out the transaction if we need to
            if (txn != null) {
                txn.end();
                txn = null;
            }
        }

        return Arrays.asList(responseEntries);
    }

    /**
     * common update to the operationContext
     * @param operationContext
     * @param method
     */
    public void updateOperationContext(FHIROperationContext operationContext, String method) {
        FHIRRequestContext requestContext = FHIRRequestContext.get();
        operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, requestContext.getExtendedOperationProperties(FHIROperationContext.PROPNAME_URI_INFO));
        operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, requestContext.getExtendedOperationProperties(FHIROperationContext.PROPNAME_HTTP_HEADERS));
        operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, requestContext.getExtendedOperationProperties(FHIROperationContext.PROPNAME_SECURITY_CONTEXT));
        operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, requestContext.getExtendedOperationProperties(FHIROperationContext.PROPNAME_HTTP_REQUEST));
        operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, method);
    }

    /**
     * This function converts the specified query string (a String) into an equivalent MultivaluedMap<String,String>
     * containing the query parameters defined in the query string.
     *
     * @param queryString
     *            the query string to be processed
     * @return
     */
    private MultivaluedMap<String, String> getQueryParameterMap(String queryString) {
        MultivaluedMap<String, String> result = null;
        FHIRUrlParser parser = new FHIRUrlParser("foo?" + queryString);
        result = parser.getQueryParameters();
        return result;
    }

    /**
     * This method will return the fullUrl if it is a local identifier, or return null
     * otherwise.
     *
     * @param fullUrl
     *            the non-null bundle request entry fullUrl value
     * @return the local identifier
     */
    private String retrieveLocalIdentifier(String fullUrl) {
        if (fullUrl.startsWith(LOCAL_REF_PREFIX)) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("Request entry contains local identifier: " + fullUrl);
            }
            return fullUrl;
        }
        return null;
    }

    /**
     * Creates a bundle that will hold results for a search operation.
     *
     * @param resourceResults
     *            the list of resource results to include in the bundle
     * @param searchContext
     *            the FHIRSearchContext object associated with the search
     * @param type
     *            the name of the resource type being searched
     * @return the bundle
     * @throws Exception
     */
    Bundle createSearchResponseBundle(List<ResourceResult<? extends Resource>> resourceResults, FHIRSearchContext searchContext, String type) throws Exception {

        // throws if we have a count of more than 2,147,483,647 resources
        UnsignedInt totalCount = searchContext.getTotalCount() != null ? UnsignedInt.of(searchContext.getTotalCount()) : null;
        // generate ID for this bundle and set total
        Bundle.Builder bundleBuilder = Bundle.builder()
                                            .type(BundleType.SEARCHSET)
                                            .id(UUID.randomUUID().toString())
                                            .total(totalCount);

        if (resourceResults.size() > 0) {
            // Calculate how many resources are 'match' mode
            int matchResourceCount = searchContext.getMatchCount();
            List<ResourceResult<? extends Resource>> matchResources = resourceResults.subList(0,  matchResourceCount);

            // Check if too many included resources
            if (resourceResults.size() > matchResourceCount + searchContext.getMaxPageIncludeCount()) {
                throw buildRestException("Number of returned 'include' resources exceeds allowable limit of " + searchContext.getMaxPageIncludeCount(),
                    IssueType.BUSINESS_RULE, IssueSeverity.ERROR);
            }

            // Find chained search parameters and find reference search parameters containing only a logical ID
            List<QueryParameter> chainedSearchParameters = new ArrayList<>();
            List<QueryParameter> logicalIdReferenceSearchParameters = new ArrayList<>();
            for (QueryParameter queryParameter : searchContext.getSearchParameters()) {
                // We do not need to look at canonical references here. They will not contain versions of the
                // form '.../_history/xx' nor logical ID-only references, which is what we want to check
                // these search parameters for.
                if (!queryParameter.isReverseChained() && !queryParameter.isCanonical()) {
                    if (queryParameter.isChained()) {
                        chainedSearchParameters.add(queryParameter);
                    } else if (SearchConstants.Type.REFERENCE == queryParameter.getType()) {
                        // Look for logical ID-only value
                        for (QueryParameterValue value : queryParameter.getValues()) {
                            ReferenceValue refVal = ReferenceUtil.createReferenceValueFrom(value.getValueString(), null, ReferenceUtil.getBaseUrl(null));
                            if (refVal.getType() == ReferenceType.LITERAL_RELATIVE && refVal.getTargetResourceType() == null) {
                                logicalIdReferenceSearchParameters.add(queryParameter);
                                break;
                            }
                        }
                    }
                }
            }
            List<Issue> issues = new ArrayList<>();
            if (searchContext.getOutcomeIssues() != null) {
                issues.addAll(searchContext.getOutcomeIssues());
            }
            if (!chainedSearchParameters.isEmpty() || !logicalIdReferenceSearchParameters.isEmpty()) {
                // Check 'match' resources for versioned references in chain search parameter fields and
                // multiple resource types with matching logical ID in reference search parameter fields.
                issues = performSearchReferenceChecks(type, chainedSearchParameters, logicalIdReferenceSearchParameters, matchResources);
            }

            for (ResourceResult<? extends Resource> resourceResult : resourceResults) {
                Resource resource = resourceResult.getResource();
                Entry.Builder entryBuilder = Entry.builder();
                if (resource != null) {
                    if (resource.getId() != null) {
                        // do not set the entryBuilder.id value for response bundle
                        entryBuilder.fullUrl(Uri.of(getRequestBaseUri(type) + "/" + resource.getClass().getSimpleName() + "/" + resource.getId()));
                    } else {
                        String msg = "A resource with no id was found.";
                        log.warning(msg);
                        issues.add(FHIRUtil.buildOperationOutcomeIssue(IssueSeverity.WARNING, IssueType.NOT_SUPPORTED, msg));
                    }
                    entryBuilder.resource(resource);
                } else if (searchContext.isIncludeResourceData()) {
                    String msg = "A resource with no data was found.";
                    log.warning(msg);
                    issues.add(FHIRUtil.buildOperationOutcomeIssue(IssueSeverity.WARNING, IssueType.NOT_SUPPORTED, msg));
                } else {
                    // Off-spec - simply provide the url without the resource body. But in order
                    // to satisfy "Rule: must be a resource unless there's a request or response"
                    // we also add a response element with the version and lastModified info
                    final String fullUrl = getRequestBaseUri(type) + "/" + resourceResult.getResourceTypeName() + "/" + resourceResult.getLogicalId();
                    Bundle.Entry.Response response = Bundle.Entry.Response.builder()
                            .status("200")
                            .etag(getEtagValue(resourceResult.getVersion()))
                            .location(Uri.of(fullUrl + "/_history/" + resourceResult.getVersion()))
                            .lastModified(net.sovrinhealth.fhir.model.type.Instant.of(resourceResult.getLastUpdated().atZone(UTC)))
                            .build();
                    entryBuilder.fullUrl(Uri.of(fullUrl)).response(response);
                }
                // Search mode is determined by the matchResourceCount, which will be decremented each time through the loop.
                // If the count is greater than 0, the mode is MATCH. If less than or equal to 0, the mode is INCLUDE.
                Entry entry = entryBuilder
                    .search(Search.builder()
                        .mode(matchResourceCount-- > 0 ? SearchEntryMode.MATCH : SearchEntryMode.INCLUDE)
                        .score(Decimal.of("1"))
                        .build())
                    .build();
                bundleBuilder.entry(entry);
            }

            if (!issues.isEmpty()) {
                // Add OperationOutcome resource containing issues
                bundleBuilder.entry(
                    Entry.builder()
                    .search(Search.builder().mode(SearchEntryMode.OUTCOME).build())
                    .resource(FHIRUtil.buildOperationOutcome(issues))
                    .build());
            }
        }

        Bundle bundle = bundleBuilder.build();

        // Add the SUBSETTED tag, if the _elements search result parameter was applied to limit elements included in
        // returned resources or _summary is required.
        if (searchContext.hasElementsParameters()
                || (searchContext.hasSummaryParameter() && !searchContext.getSummaryParameter().equals(SummaryValueSet.FALSE))) {
            bundle = FHIRUtil.addTag(bundle, SearchConstants.SUBSETTED_TAG);
        }

        return bundle;
    }

    /**
     * For chained search, check 'match' resources for existence of a versioned reference in the field
     * associated with the chain search parameter.
     *
     * For reference search specifying logical ID only, check 'match' resources for existence of multiple
     * resource types containing the same logical ID in the field associated with the reference search parameter.
     *
     * @param resourceType
     *            The search resource type.
     * @param chainQueryParameters
     *            The chained query parameters. These will be mutually exclusive of the logicalIdReferenceQueryParameters.
     * @param logicalIdReferenceQueryParameters
     *            The list of reference query parameters that only specified a logical ID.
     * @param matchResources
     *            The list of 'match' resources to check.
     * @return
     *            A list of Issues, one per resource in which a versioned reference is found.
     * @throws Exception if multiple resource types containing the same logical ID are found
     */
    private List<Issue> performSearchReferenceChecks(String resourceType, List<QueryParameter> chainQueryParameters,
            List<QueryParameter> logicalIdReferenceQueryParameters, List<ResourceResult<? extends Resource>> matchResources) throws Exception {
        List<Issue> issues = new ArrayList<>();

        if (!chainQueryParameters.isEmpty() || !logicalIdReferenceQueryParameters.isEmpty()) {
            // Build a map of parameter name to SearchParameter for all queryParameters.
            // Since the search was successful, we can assume search parameters exist, are valid, and of type Reference.
            // However, if this is a whole-system search, we will need to get the SearchParameters based on
            // the resource type returned.
            Map<QueryParameter, SearchParameter> searchParameterMap = new HashMap<>();
            if (!Resource.class.getSimpleName().equals(resourceType)) {
                Class<? extends Resource> resourceTypeClass = ModelSupport.getResourceType(resourceType);
                for (QueryParameter queryParameter : chainQueryParameters) {
                    searchParameterMap.put(queryParameter, searchHelper.getSearchParameter(resourceTypeClass, queryParameter.getCode()));
                }
                for (QueryParameter queryParameter : logicalIdReferenceQueryParameters) {
                    searchParameterMap.put(queryParameter, searchHelper.getSearchParameter(resourceTypeClass, queryParameter.getCode()));
                }
            }

            List<QueryParameter> queryParameters = new ArrayList<>(chainQueryParameters);
            queryParameters.addAll(logicalIdReferenceQueryParameters);
            Map<String, String> logicalIdToTypeMap = new HashMap<>();

            FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

            // Loop through the resources, looking for versioned references and references to multiple resource types for the same logical ID
            for (ResourceResult<? extends Resource> resourceResult : matchResources) {
                Resource resource = resourceResult.getResource();
                if (resource == null) {
                    log.warning("Unexpected null resource: " + resourceResult.toString());
                    throw new FHIRPersistenceException("Search reference check contained a null resource");
                }

                // A flag that indicates whether we need to take a closer look at the reference values or not
                boolean needsEval = false;

                // If any of the reference values are "versioned"
                // then we'll need to check that they aren't used for chaining
                // TODO Should we pass the previously-gathered set of references into the method instead?
                CollectingVisitor<Reference> refCollector = new CollectingVisitor<>(Reference.class);
                resource.accept(refCollector);
                List<Reference> references = refCollector.getResult();
                for (Reference ref : references) {
                    if (ref.getReference() != null && ref.getReference().getValue() != null
                            && ref.getReference().getValue().contains("/_history/")) {
                        needsEval = true;
                        break;
                    }
                }

                // If any of the ids are "logical id only" (i.e. have no target resource type info),
                // then we'll need to check that they aren't ambiguous
                if (!logicalIdReferenceQueryParameters.isEmpty()) {
                    needsEval = true;
                }

                if (needsEval) {
                    validateReferenceParams(chainQueryParameters, logicalIdReferenceQueryParameters, issues,
                            searchParameterMap, queryParameters, logicalIdToTypeMap, evaluator, resource);
                }
            }
        }

        return issues;
    }

    private void validateReferenceParams(List<QueryParameter> chainQueryParameters, List<QueryParameter> logicalIdReferenceQueryParameters,
            List<Issue> issues, Map<QueryParameter, SearchParameter> searchParameterMap, List<QueryParameter> queryParameters,
            Map<String, String> logicalIdToTypeMap, FHIRPathEvaluator evaluator, Resource resource)
            throws Exception, FHIRPathException, FHIRSearchException, FHIROperationException {
        EvaluationContext evaluationContext = new EvaluationContext(resource);
        for (QueryParameter queryParameter : queryParameters) {
            SearchParameter searchParameter = searchParameterMap.get(queryParameter);
            if (searchParameter == null) {
                searchParameter = searchHelper.getSearchParameter(resource.getClass(), queryParameter.getCode());
            }

            // For logical ID check, only need to look at search parameters with more than one target resource type
            if (logicalIdReferenceQueryParameters.contains(queryParameter) && searchParameter.getTarget().size() == 1) {
                continue;
            }

            Collection<FHIRPathNode> nodes = evaluator.evaluate(evaluationContext, searchParameter.getExpression().getValue());
            for (FHIRPathNode node : nodes) {
                Reference reference = node.asElementNode().element().as(Reference.class);
                ReferenceValue rv = ReferenceUtil.createReferenceValueFrom(reference, ReferenceUtil.getBaseUrl(null));
                if (chainQueryParameters.contains(queryParameter) && rv.getVersion() != null &&
                        (rv.getTargetResourceType() == null || rv.getTargetResourceType().equals(queryParameter.getModifierResourceTypeName()))) {
                    // Found versioned reference value
                    String msg = "Resource with id '" + resource.getId() +
                            "' contains a versioned reference in an element used for chained search, but chained search does not act on versioned references.";
                    issues.add(FHIRUtil.buildOperationOutcomeIssue(IssueSeverity.WARNING, IssueType.NOT_SUPPORTED, msg, node.path()));
                } else if (logicalIdReferenceQueryParameters.contains(queryParameter) && rv.getTargetResourceType() != null &&
                        !rv.getTargetResourceType().equals(logicalIdToTypeMap.computeIfAbsent(queryParameter.getCode() + "|" + rv.getValue(), v -> rv.getTargetResourceType()))) {
                    // Found multiple resource types this logical ID
                    String msg = "Multiple resource type matches found for logical ID '" + rv.getValue() +
                            "' for search parameter '" + queryParameter.getCode() + "'.";
                    throw buildRestException(msg, IssueType.INVALID, IssueSeverity.ERROR);
                }
            }
        }
    }

    /**
     * Creates a bundle that will hold the results of a history operation.
     *
     * @param resourcesResults
     *            the list of resource results to include in the history bundle
     * @param historyContext
     *            the FHIRHistoryContext associated with the history operation
     * @param type
     *            the name of the resource type on which the history operation was requested
     * @return the bundle
     * @throws Exception
     */
    private Bundle createHistoryBundle(List<ResourceResult<? extends Resource>> resourceResults, FHIRHistoryContext historyContext, String type)
            throws Exception {

        // throws if we have a count of more than 2,147,483,647 resources
        UnsignedInt totalCount = historyContext.getTotalCount() != null ? UnsignedInt.of(historyContext.getTotalCount()) : null;
        // generate ID for this bundle and set the "total" field for the bundle
        Bundle.Builder bundleBuilder = Bundle.builder()
                                             .type(BundleType.HISTORY)
                                             .id(UUID.randomUUID().toString())
                                             .total(totalCount);

        for (int i = 0; i < resourceResults.size(); i++) {
            ResourceResult<? extends Resource> resourceResult = resourceResults.get(i);
            Resource resource = resourceResult.getResource();

            // Note that the resource object may be null if it has been erased
            if (resource != null && resource.getId() == null) {
                String msg = "A resource with no id was found.";
                log.warning(msg);
                throw new IllegalStateException(msg);
            }

            // Determine the correct method to include in this history entry (POST, PUT, DELETE).
            HTTPVerb method;
            String status;
            if (resourceResult.isDeleted() || resource == null) {
                method = HTTPVerb.DELETE;
                status = "200";
            } else if (resourceResult.getVersion() == 1) {
                // it may have been a PUT in the create-on-update case, but we use POST here anyway
                method = HTTPVerb.POST;
                status = "201";
            } else {
                method = HTTPVerb.PUT;
                // it may have been a 201 (Created) in the "undelete" case, but we use 200 here anyway
                status = "200";
            }

            // Create the 'request' entry, and set the request.url field.
            // 'create' --> url = "<resourceType>"
            // 'update'/'delete' --> url = "<resourceType>/<logicalId>"
            final String resourceType = resourceResult.getResourceTypeName();
            final String resourcePath = resourceType + "/" + resourceResult.getLogicalId();
            Entry.Request request = Entry.Request.builder()
                    .method(method)
                    .url(Url.of(method == HTTPVerb.POST ? resourceType : resourcePath))
                    .build();

            String fullUrl = getRequestBaseUri(type) + "/" + resourcePath;

            Entry.Response response = Entry.Response.builder()
                    .status(status)
                    .etag(getEtagValue(resourceResult.getVersion()))
                    .location(Uri.of(fullUrl + "/_history/" + resourceResult.getVersion()))
                    .lastModified(net.sovrinhealth.fhir.model.type.Instant.of(resourceResult.getLastUpdated().atZone(UTC)))
                    .build();

            Entry entry = Entry.builder()
                    .fullUrl(Uri.of(fullUrl))
                    .request(request)
                    .response(response)
                    .resource(resource)
                    .build();

            bundleBuilder.entry(entry);
        }

        return bundleBuilder.build();
    }

    /**
     * Retrieves the shared interceptor mgr instance from the servlet context.
     */
    private FHIRPersistenceInterceptorMgr getInterceptorMgr() {
        return FHIRPersistenceInterceptorMgr.getInstance();
    }

    /**
     *
     * @param context the FHIRPagingContext associated with this request
     * @param responseBundle the search results bundle
     * @param requestUri the request URI
     * @param firstId the resource Id of the first resource from the search result
     * @param lastId The resource Id of the last resource from the search result
     * @param expectedNextId the expected resource Id of the first resource in the next page of search results
     * @param expectedPreviousId the expected resource Id of the last resource in the previous page of search results
     * @return the search results bundle that is returned to the REST service caller
     * @throws Exception Any non-recoverable exception thrown while adding links to the response bundle
     */
    private Bundle addLinks(FHIRPagingContext context, Bundle responseBundle, String requestUri, Long firstId, Long lastId, Long expectedNextId, Long expectedPreviousId) throws Exception {
        String selfUri = null;
        SummaryValueSet summaryParameter = null;
        Bundle.Builder bundleBuilder = responseBundle.toBuilder();

        if (context instanceof FHIRSearchContext) {
            FHIRSearchContext searchContext = (FHIRSearchContext) context;
            summaryParameter = searchContext.getSummaryParameter();
            try {
                selfUri = SearchHelper.buildSearchSelfUri(requestUri, searchContext);
            } catch (Exception e) {
                log.log(Level.WARNING, "Unable to construct self link for search result bundle; using the request URI instead.", e);
            }
        }
        requestUri = requestUri.replace("&_firstId=" + context.getFirstId(), "").replace("_firstId="
                + context.getFirstId() + "&", "").replace("_firstId="
                        + context.getFirstId(), "");
        requestUri = requestUri.replace("&_lastId=" + context.getLastId(), "").replace("_lastId="
                + context.getLastId() + "&", "").replace("_lastId="
                        + context.getLastId(), "");
        if (selfUri == null) {
            selfUri = requestUri;
        }
        // add the resource Id of the first resource of the search result as a query parameter to self uri
        selfUri = addParameterToUrl(selfUri, SearchConstants.FIRST_ID, firstId);
        // add the resource Id of the last resource of the search result as a query parameter to self uri
        selfUri = addParameterToUrl(selfUri, SearchConstants.LAST_ID, lastId);
        // create 'self' link
        Bundle.Link selfLink = Bundle.Link.builder()
                .relation(string("self"))
                .url(Url.of(selfUri))
                .build();
        bundleBuilder.link(selfLink);

        // If for search with _summary=count or pageSize == 0, then don't add previous and next links.
        if (!SummaryValueSet.COUNT.equals(summaryParameter) && context.getPageSize() > 0) {
            // In case the currently requested page is < 1, ensure the next link points to page 1,
            // to avoid unnecessarily paging through additional page numbers < 1
            int nextPageNumber = Math.max(context.getPageNumber() + 1, 1);
            if (nextPageNumber <= context.getLastPageNumber()
                    && (nextPageNumber == 1 || context.getTotalCount() != null || context.getMatchCount() == context.getPageSize())) {

                // starting with the self URI
                String nextLinkUrl = requestUri;

                // remove existing _page parameters from the query string
                nextLinkUrl = nextLinkUrl.replace("&_page=" + context.getPageNumber(), "").replace("_page="
                        + context.getPageNumber() + "&", "").replace("_page=" + context.getPageNumber(), "");

                if (nextLinkUrl.contains("?")) {
                    if (!nextLinkUrl.endsWith("?")) {
                        // there are other parameters in the query string
                        nextLinkUrl += "&";
                    }
                } else {
                    nextLinkUrl += "?";
                }

                // add new _page parameter to the query string
                nextLinkUrl += "_page=" + nextPageNumber;

                // add the expected resource Id of the first resource in the next page of search results as a query parameter to next url
                nextLinkUrl = addParameterToUrl(nextLinkUrl, SearchConstants.FIRST_ID, expectedNextId);

                // create 'next' link
                Bundle.Link nextLink =
                        Bundle.Link.builder().relation(string("next")).url(Url.of(nextLinkUrl)).build();
                bundleBuilder.link(nextLink);
            }

            int prevPageNumber = Math.min(context.getPageNumber() - 1, context.getLastPageNumber());
            if (prevPageNumber > 0) {

                // starting with the original request URI
                String prevLinkUrl = requestUri;

                // remove existing _page parameters from the query string
                prevLinkUrl =
                        prevLinkUrl.replace("&_page=" + context.getPageNumber(), "").replace("_page="
                                + context.getPageNumber() + "&", "").replace("_page="
                                        + context.getPageNumber(), "");

                if (prevLinkUrl.contains("?")) {
                    if (!prevLinkUrl.endsWith("?")) {
                        // there are other parameters in the query string
                        prevLinkUrl += "&";
                    }
                } else {
                    prevLinkUrl += "?";
                }

                // add new _page parameter to the query string
                prevLinkUrl += "_page=" + prevPageNumber;

                // add the expected resource Id of the last resource in the previous page of search results as a query parameter to previous url
                prevLinkUrl = addParameterToUrl(prevLinkUrl, SearchConstants.LAST_ID, expectedPreviousId);


                // create 'previous' link
                Bundle.Link prevLink =
                        Bundle.Link.builder().relation(string("previous")).url(Url.of(prevLinkUrl)).build();
                bundleBuilder.link(prevLink);
            }
        }

        return bundleBuilder.build();
    }

    /**
     * Add a new parameter with the name and value to the url.
     * @param url the url to which the parameter has to be added
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @return
     */
    private String addParameterToUrl(String url, String name, Long value) {
        if (value == null) {
            return url;
        }
        if (url.contains("?")) {
            if (!url.endsWith("?")) {
                // there are other parameters in the query string
                url += "&";
            }
        } else {
            url += "?";
        }
        // add new parameter to the query string
        url += name  + "=" + value;
        return url;
    }

    /**
     * This method returns the "base URI" associated with the current request. For example, if a client invoked POST
     * https://myhost:9443/fhir-server/api/v4/Patient to create a Patient resource, this method would return
     * "https://myhost:9443/fhir-server/api/v4".
     *
     * @param type
     *      The resource type associated with the request URI (e.g. "Patient" in the case of
     *      https://myhost:9443/fhir-server/api/v4/Patient), or null if there is no such resource type
     * @return The base endpoint URI associated with the current request.
     * @throws Exception if an error occurs while reading the config
     * @implNote This method uses {@link FHIRRequestContext#getOriginalRequestUri()} to get the original request URI
     *      and then strips it to the <a href="https://www.hl7.org/fhir/http.html#general">Service Base URL</a>
     */
    public static String getRequestBaseUri(String type) throws Exception {
        String baseUri = null;

        String requestUri = FHIRRequestContext.get().getOriginalRequestUri();

        // Strip off everything after the path
        int queryPathSeparatorLoc = requestUri.indexOf("?");
        if (queryPathSeparatorLoc != -1) {
            baseUri = requestUri.substring(0, queryPathSeparatorLoc);
        } else {
            baseUri = requestUri;
        }

        // Strip off any path elements after the base
        if (type != null && !type.isEmpty()) {
            int resourceNamePathLocation = baseUri.indexOf("/" + type + "/");
            if (resourceNamePathLocation != -1) {
                baseUri = baseUri.substring(0, resourceNamePathLocation);
            } else {
                resourceNamePathLocation = baseUri.lastIndexOf("/" + type);
                if (resourceNamePathLocation != -1) {
                    baseUri = baseUri.substring(0, resourceNamePathLocation);
                } else {
                    // Assume the request was a batch/transaction; nothing to strip
                }
            }
        }

        // Strip any path segments for whole-system interactions
        // (in case of whole-system search, "Resource" is passed as the type, or $everything-based search)
        if (type == null || type.isEmpty() || "Resource".equals(type) || baseUri.contains("$everything")) {
            if (baseUri.endsWith("/_search")) {
                baseUri = baseUri.substring(0, baseUri.length() - "/_search".length());
            } else if (baseUri.endsWith("/_history")) {
                baseUri = baseUri.substring(0, baseUri.length() - "/_history".length());
            } else if (baseUri.contains("/$")) {
                baseUri = baseUri.substring(0, baseUri.lastIndexOf("/$"));
            }
        }

        return baseUri;
    }

    @Override
    public Map<String, Object> buildPersistenceEventProperties(String type, String id,
            String version, FHIRSearchContext searchContext, FHIRSystemHistoryContext systemHistoryContext) throws FHIRPersistenceException {
        Map<String, Object> props = new HashMap<>();
        props.put(FHIRPersistenceEvent.PROPNAME_PERSISTENCE_IMPL, persistence);
        if (type != null) {
            props.put(FHIRPersistenceEvent.PROPNAME_RESOURCE_TYPE, type);
        }
        if (id != null) {
            props.put(FHIRPersistenceEvent.PROPNAME_RESOURCE_ID, id);
        }
        if (version != null) {
            props.put(FHIRPersistenceEvent.PROPNAME_VERSION_ID, version);
        }
        if (searchContext != null) {
            props.put(FHIRPersistenceEvent.PROPNAME_SEARCH_CONTEXT_IMPL, searchContext);
        }
        if (systemHistoryContext != null) {
            props.put(FHIRPersistenceEvent.PROPNAME_SYSTEM_HISTORY_CONTEXT_IMPL, systemHistoryContext);
        }
        return props;
    }

    /**
     * Sets various properties on the FHIROperationContext instance.
     *
     * @param operationContext
     *            the FHIROperationContext on which to set the properties
     * @param resourceTypeName
     * @param requestParameters
     * @throws Exception
     */
    private void setOperationContextProperties(FHIROperationContext operationContext, String resourceTypeName, Parameters requestParameters)
            throws Exception {
        operationContext.setProperty(FHIROperationContext.PROPNAME_REQUEST_BASE_URI, getRequestBaseUri(resourceTypeName));
        operationContext.setProperty(FHIROperationContext.PROPNAME_REQUEST_PARAMETERS, requestParameters);
        operationContext.setProperty(FHIROperationContext.PROPNAME_PERSISTENCE_IMPL, persistence);
        operationContext.setProperty(FHIROperationContext.PROPNAME_FHIR_VERSION, fhirVersion);
    }

    @Override
    public int doReindex(FHIROperationContext operationContext, OperationOutcome.Builder operationOutcomeResult, Instant tstamp,
            List<Long> indexIds, String resourceLogicalId, boolean force) throws Exception {
        int result = 0;
        // Since the try logic is slightly different in the code paths, we want to dispatch to separate methods to simplify the logic.
        if (indexIds == null) {
            result = doReindexSingle(operationOutcomeResult, tstamp, resourceLogicalId, force);
        } else {
            result = doReindexList(operationOutcomeResult, tstamp, indexIds, force);
        }
        return result;
    }

    /**
     * encapsulates the logic to process a list with graduated backoff through the full list of indexIds
     *
     * @param operationOutcomeResult
     * @param tstamp
     * @param indexIds
     * @param force
     * @return
     * @throws Exception
     */
    public int doReindexList(OperationOutcome.Builder operationOutcomeResult, Instant tstamp, List<Long> indexIds, boolean force) throws Exception {
        // If the indexIds are empty or null, then it's not properly formed.
        if (indexIds == null || indexIds.isEmpty()) {
            throw new IllegalArgumentException("No indexIds sent to the $reindex list method");
        }

        /*
         * How the backoff works...
         * indexIds[1,2,3,4,5,6,7,8,9,10]
         *
         * Pass 1 left=0, right=10, max=10
         * Result: Deadlock
         *
         * Pass2 left=0, right=1, max=10
         * Move over by 1
         * Result: Pass
         *
         * Pass3 left=1, right=2, max=10
         * Move over by 1
         * Result: Pass
         *
         * ... all the way up to 10
         *
         * Return total count back to caller.
         *
         * @implNote tried divide and conquer and it caused it to try large pass fail, small pass succeed, and therefore chose a small linear pass.
         */

        // Maximum attempts to retry across all windows.
        final int TX_ATTEMPTS = 10;

        int result = 0;
        int max = indexIds.size();

        int left = 0;
        int right = max;

        int window = 0;
        int attempt = 1;
        while (left < max && attempt <= TX_ATTEMPTS) {
            window++;
            if (log.isLoggable(Level.FINE)) {
                log.fine("$reindex window [" + window + "/" + attempt + "] -> left=[" + left + "] right=[" + right + "] max=[" + max + "]");
            }

            boolean backoff = false;

            List<Long> subListIndexIds = indexIds.subList(left, right);

            FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
            txn.begin();
            try {
                FHIRPersistenceContext persistenceContext = null;
                result += persistence.reindex(persistenceContext, operationOutcomeResult, tstamp, subListIndexIds, null, force);
            } catch (FHIRPersistenceDataAccessException x) {
                // At this point, the transaction is marked for rollback
                if (x.isTransactionRetryable() && ++attempt <= TX_ATTEMPTS) {
                    if (x.getCause() instanceof LockException && ((LockException) x.getCause()).isDeadlock()) {
                        backoff = true;
                        long wait = RANDOM.nextInt(5000);
                        log.info("attempt #" + window + "/" + attempt + " failed, retrying transaction, backing off [wait=" + wait + "ms]");
                        Thread.sleep(wait);
                    } else {
                        log.info("attempt #" + window + "/" + attempt + " failed, retrying transaction, not backing off");
                    }
                } else {
                    throw x;
                }
            } finally {
                txn.end();
            }

            // backoff controls how we increment the window.
            if (backoff) {
                // we're now going to move over one at a time.
                right = left + 1;
            } else {
                // we're sliding over by one more
                left = right;
                right += 1;
            }
        }
        return result;
    }

    /**
     * do a single reindex on a specific resourceLogicalId
     *
     * @param operationOutcomeResult
     * @param tstamp
     * @param resourceLogicalId
     * @param force
     * @return
     * @throws Exception
     */
    public int doReindexSingle(OperationOutcome.Builder operationOutcomeResult, Instant tstamp, String resourceLogicalId, boolean force) throws Exception {
        int result = 0;
        // handle some retries in case of deadlock exceptions
        final int TX_ATTEMPTS = 5;
        int attempt = 1;
        do {
            FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
            txn.begin();
            try {
                FHIRPersistenceContext persistenceContext = null;
                result = persistence.reindex(persistenceContext, operationOutcomeResult, tstamp, null, resourceLogicalId, force);
                attempt = TX_ATTEMPTS; // end the retry loop
            } catch (FHIRPersistenceDataAccessException x) {
                if (x.isTransactionRetryable() && attempt < TX_ATTEMPTS) {
                    log.info("attempt #" + attempt + " failed, retrying transaction");
                } else {
                    throw x;
                }
            } finally {
                txn.end();
            }
        } while (attempt++ < TX_ATTEMPTS);

        return result;
    }

    @Override
    public List<Issue> validateResource(Resource resource) throws FHIROperationException {
        Set<String> atLeastOneProfiles = new HashSet<>();
        Set<String> atLeastOneProfilesWithoutVersion = new HashSet<>();
        Set<String> notAllowedProfiles = new HashSet<>();
        Set<String> notAllowedProfilesWithoutVersion = new HashSet<>();
        boolean allowUnknown;
        Map<String, String> defaultVersions = new HashMap<>();
        boolean defaultVersionsSpecified = false;
        Resource resourceToValidate = resource;

        // Retrieve the profile configuration
        try {
            StringBuilder defaultProfileConfigPath = new StringBuilder(FHIRConfiguration.PROPERTY_RESOURCES).append("/Resource/")
                    .append(FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES).append("/");
            StringBuilder resourceSpecificProfileConfigPath = new StringBuilder(FHIRConfiguration.PROPERTY_RESOURCES).append("/")
                    .append(resource.getClass().getSimpleName()).append("/").append(FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES)
                    .append("/");

            // Get the 'atLeastOne' profile list
            List<String> resourceSpecificAtLeastOneProfiles =
                    FHIRConfigHelper.getStringListProperty(resourceSpecificProfileConfigPath.toString() +
                        FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_AT_LEAST_ONE);
            if (resourceSpecificAtLeastOneProfiles != null) {
                atLeastOneProfiles.addAll(resourceSpecificAtLeastOneProfiles);
            } else {
                List<String> defaultAtLeastOneProfiles =
                        FHIRConfigHelper.getStringListProperty(defaultProfileConfigPath.toString() +
                            FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_AT_LEAST_ONE);
                if (defaultAtLeastOneProfiles != null) {
                    atLeastOneProfiles.addAll(defaultAtLeastOneProfiles);
                }
            }

            // Build the list of 'atLeastOne' profiles that didn't specify a version
            for (String profile : atLeastOneProfiles) {
                if (!profile.contains("|")) {
                    atLeastOneProfilesWithoutVersion.add(profile);
                }
            }

            if (log.isLoggable(Level.FINER)) {
                log.finer("Required profile list: " + atLeastOneProfiles);
            }

            // Get the 'notAllowed' profile list
            List<String> resourceSpecificNotAllowedProfiles =
                    FHIRConfigHelper.getStringListProperty(resourceSpecificProfileConfigPath.toString() +
                        FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_NOT_ALLOWED);
            if (resourceSpecificNotAllowedProfiles != null) {
                notAllowedProfiles.addAll(resourceSpecificNotAllowedProfiles);
            } else {
                List<String> defaultNotAllowedProfiles =
                        FHIRConfigHelper.getStringListProperty(defaultProfileConfigPath.toString() +
                            FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_NOT_ALLOWED);
                if (defaultNotAllowedProfiles != null) {
                    notAllowedProfiles.addAll(defaultNotAllowedProfiles);
                }
            }

            // Build the list of 'notAllowed' profiles that didn't specify a version
            for (String profile : notAllowedProfiles) {
                if (!profile.contains("|")) {
                    notAllowedProfilesWithoutVersion.add(profile);
                }
            }

            if (log.isLoggable(Level.FINER)) {
                log.finer("Not allowed profile list: " + notAllowedProfiles);
            }

            // Get the 'allowUnknown' property
            Boolean resourceSpecificAllowUnknown =
                    FHIRConfigHelper.getBooleanProperty(resourceSpecificProfileConfigPath.toString() +
                        FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_ALLOW_UNKNOWN, null);
            if (resourceSpecificAllowUnknown != null) {
                allowUnknown = resourceSpecificAllowUnknown;
            } else {
                allowUnknown = FHIRConfigHelper.getBooleanProperty(defaultProfileConfigPath.toString() +
                    FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_ALLOW_UNKNOWN, Boolean.TRUE);
            }

            if (log.isLoggable(Level.FINER)) {
                log.finer("Allow unknown: " + allowUnknown);
            }

            // Get the 'defaultVersions' entries
            PropertyGroup resourceSpecificDefaultVersionsGroup =
                    FHIRConfigHelper.getPropertyGroup(resourceSpecificProfileConfigPath.toString() +
                        FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_DEFAULT_VERSIONS);
            if (resourceSpecificDefaultVersionsGroup != null) {
                defaultVersionsSpecified = true;
                for (PropertyEntry entry : resourceSpecificDefaultVersionsGroup.getProperties()) {
                    defaultVersions.put(entry.getName(), (String) entry.getValue());
                }
            } else {
                PropertyGroup allResourceDefaultVersionsGroup =
                        FHIRConfigHelper.getPropertyGroup(defaultProfileConfigPath.toString() +
                            FHIRConfiguration.PROPERTY_FIELD_RESOURCES_PROFILES_DEFAULT_VERSIONS);
                if (allResourceDefaultVersionsGroup != null) {
                    defaultVersionsSpecified = true;
                    for (PropertyEntry entry : allResourceDefaultVersionsGroup.getProperties()) {
                        defaultVersions.put(entry.getName(), (String) entry.getValue());
                    }
                }
            }

            if (log.isLoggable(Level.FINER)) {
                log.finer("Default profile versions: [");
                for (String profile : defaultVersions.keySet()) {
                    log.finer("  " + profile + " : " + defaultVersions.get(profile));
                }
                log.finer("]");
            }
        } catch (Exception e) {
            throw new FHIROperationException("Error retrieving profile configuration.", e);
        }

        // Validate asserted profiles if necessary:
        // - if 'atLeastOne' is a non-empty list OR
        // - if 'notAllowed' is a non-empty list OR
        // - if 'allowUnknown' is set to false OR
        // - if 'defaultVersions' exists (empty or not)
        List<Issue> issues = new ArrayList<>();
        if (!notAllowedProfiles.isEmpty() || !atLeastOneProfiles.isEmpty() || !allowUnknown || defaultVersionsSpecified) {
            boolean validProfileFound = false;
            boolean defaultVersionUsed = false;
            List<Canonical> defaultVersionAssertedProfiles = new ArrayList<>();;

            // Get the profiles asserted for this resource
            List<String> resourceAssertedProfiles = ProfileSupport.getResourceAssertedProfiles(resource);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Asserted profiles: " + resourceAssertedProfiles);
            }

            // Validate the asserted profiles
            for (String resourceAssertedProfile : resourceAssertedProfiles) {
                // Check if asserted profile contains a version
                String strippedAssertedProfile = null;
                int index = resourceAssertedProfile.indexOf("|");
                if (index != -1) {
                    strippedAssertedProfile = resourceAssertedProfile.substring(0, index);
                } else {
                    // Check if assertedProfile has a default version
                    String defaultVersion = defaultVersions.get(resourceAssertedProfile);
                    if (defaultVersion != null) {
                        defaultVersionUsed = true;
                        strippedAssertedProfile = resourceAssertedProfile;
                        resourceAssertedProfile = resourceAssertedProfile + "|" + defaultVersion;
                    }
                }
                defaultVersionAssertedProfiles.add(Canonical.of(resourceAssertedProfile));

                if (!notAllowedProfiles.isEmpty() || !atLeastOneProfiles.isEmpty()) {
                    // For 'atLeastOne' profiles, check that at least one asserted profile is in the list of 'atLeastOne' profiles.
                    // For 'notAllowed' profiles, check that no asserted profile is in the list of 'notAllowed' profiles.
                    // If an 'atLeastOne' or 'notAllowed' profile specifies a version, an asserted profile must be an exact match.
                    // If an 'atLeastOne' or 'notAllowed' profile does not specify a version, any asserted profile of the same name
                    // will be a match regardless if it specifies a version or not.
                    if (notAllowedProfiles.contains(resourceAssertedProfile) ||
                            notAllowedProfilesWithoutVersion.contains(strippedAssertedProfile)) {
                        // For 'notAllowed' profiles, a match means an invalid profile was found
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Not allowed asserted profile found: '" + resourceAssertedProfile + "'");
                        }
                        issues.add(buildOperationOutcomeIssue(IssueSeverity.ERROR, IssueType.BUSINESS_RULE,
                            "A profile was specified which is not allowed. Resources of type '" + resource.getClass().getSimpleName() +
                            "' are not allowed to declare conformance to any of the following profiles: " + notAllowedProfiles));
                    }
                    if (atLeastOneProfiles.contains(resourceAssertedProfile) ||
                            atLeastOneProfilesWithoutVersion.contains(strippedAssertedProfile)) {
                        // For 'atLeastOne' profiles, a match means a valid profile was found
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Valid asserted profile found: '" + resourceAssertedProfile + "'");
                        }
                        validProfileFound = true;
                    }
                }
                if (!allowUnknown) {
                    // Check if asserted profile is supported
                    StructureDefinition profile = ProfileSupport.getProfile(resourceAssertedProfile);
                    if (profile == null) {
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Not supported asserted profile found: '" + resourceAssertedProfile + "'");
                        }
                        issues.add(buildOperationOutcomeIssue(IssueSeverity.ERROR, IssueType.NOT_SUPPORTED,
                            "Profile '" + resourceAssertedProfile + "' is not supported"));
                    }
                }
            }

            // Check if a profile is required but no valid profile asserted
            if (!atLeastOneProfiles.isEmpty() && !validProfileFound) {
                issues.add(buildOperationOutcomeIssue(IssueSeverity.ERROR, IssueType.BUSINESS_RULE,
                    "A required profile was not specified. Resources of type '" + resource.getClass().getSimpleName() +
                    "' must declare conformance to at least one of the following profiles: " + atLeastOneProfiles));
            }

            if (!issues.isEmpty()) {
                return issues;
            }

            // If any asserted profiles have a default version specified, make a copy of the
            // resource with the new asserted profile values and validate against the copy.
            if (defaultVersionUsed) {
                Meta metaCopy = resource.getMeta().toBuilder().profile(defaultVersionAssertedProfiles).build();
                resourceToValidate = resource.toBuilder().meta(metaCopy).build();
            }
        }

        try {
            issues = validator.validate(resourceToValidate);
        } catch (FHIRValidationException e) {
            throw new FHIROperationException("Error validating resource.", e);
        }

        return issues;
    }

    @Override
    public void validateInteraction(Interaction interaction, String resourceType) throws FHIROperationException {
        if ("Resource".equals(resourceType)) {
            switch (interaction) {
            case SEARCH:
                if (!resourcesConfig.isWholeSystemSearchSupported()) {
                    throw buildRestException("Whole system search is disabled on the server.", IssueType.BUSINESS_RULE, IssueSeverity.ERROR);
                }
                return;
            case HISTORY:
                if (!resourcesConfig.isWholeSystemHistorySupported()) {
                    throw buildRestException("Whole system history is disabled on the server.", IssueType.BUSINESS_RULE, IssueSeverity.ERROR);
                }
                return;
            default:
                throw new IllegalStateException("Search and history are the only supported system-level interactions");
            }
        }

        if (!resourcesConfig.getSupportedResourceTypes().contains(resourceType)) {
            throw buildRestException("The requested resource type '" + resourceType + "' is not found",
                    IssueType.NOT_FOUND, IssueSeverity.ERROR);
        }

        // fhir-config and fhir-server have two different Interaction enums with the same values
        // we should merge those into a single enum and put it somewhere common (fhir-core?)
        net.sovrinhealth.fhir.config.Interaction configInteraction = net.sovrinhealth.fhir.config.Interaction.from(interaction.value());

        if (!resourcesConfig.getSupportedResourceTypes(configInteraction).contains(resourceType)) {
            throw buildRestException("The requested interaction of type '" + interaction.value() + "' is not allowed for resource type '" + resourceType + "'",
                    IssueType.BUSINESS_RULE, IssueSeverity.ERROR);
        }

        // ensure that the resource type and fhirVersion for the interaction is compatible with the fhirVersion of the server
        if (!ResourceType.RESOURCE.value().equals(resourceType) && !ResourceTypeUtil.isCompatible(resourceType, fhirVersion, FHIRVersionParam.VERSION_43)) {
            throw buildRestException("The requested resource type '" + resourceType + "' is not supported for fhirVersion " + fhirVersion.value(),
                    IssueType.NOT_SUPPORTED, IssueSeverity.ERROR);
        }
    }

    @Override
    public Bundle doHistory(MultivaluedMap<String, String> queryParameters, String requestUri, String resourceType) throws Exception {
        log.entering(this.getClass().getName(), "doHistory");

        // Validate that the interaction is allowed
        if (resourceType == null) {
            validateInteraction(Interaction.HISTORY, "Resource");
        } else {
            validateInteraction(Interaction.HISTORY, resourceType);
        }

        // extract the query parameters
        FHIRRequestContext requestContext = FHIRRequestContext.get();

        if (requestContext.isReturnPreferenceDefault()) {
            // For _history, override the default preference to be REPRESENTATION so resources
            // are returned in the response bundle per the R4 spec.
            requestContext.setReturnPreference(HTTPReturnPreference.REPRESENTATION);
        }
        FHIRSystemHistoryContext historyContext = FHIRPersistenceUtil.parseSystemHistoryParameters(queryParameters,
                HTTPHandlingPreference.LENIENT.equals(requestContext.getHandlingPreference()), resourcesConfig);

        // If HTTPReturnPreference is REPRESENTATION, we fetch the resources and include them
        // in the response bundle. To make it simple, we make the records and resources the same
        // size and in the same order
        List<ResourceChangeLogRecord> records;
        List<Resource> resources = null;

        // First, invoke the 'beforeHistory' interceptor methods.
        FHIRPersistenceEvent event =
                new FHIRPersistenceEvent(null, buildPersistenceEventProperties(resourceType == null ? "Resource" : resourceType, null, null, null, historyContext));
        getInterceptorMgr().fireBeforeHistoryEvent(event);
        // Build a context
        FHIRPersistenceContext context = FHIRPersistenceContextImpl.builder(event).withRequestShard(requestContext.getRequestShardKey()).build();

        // Start a new txn in the persistence layer if one is not already active.
        Integer count = historyContext.getCount();
        Instant since = historyContext.getSince() != null && historyContext.getSince().getValue() != null ? historyContext.getSince().getValue().toInstant() : null;
        Instant before = historyContext.getBefore() != null && historyContext.getBefore().getValue() != null ? historyContext.getBefore().getValue().toInstant() : null;
        FHIRTransactionHelper txn = new FHIRTransactionHelper(getTransaction());
        txn.begin();
        try {
            if (count == null) {
                count = DEFAULT_HISTORY_ENTRIES;
            } else if (count > MAX_HISTORY_ENTRIES) {
                count = MAX_HISTORY_ENTRIES;
            }

            if (resourceType != null) {
                // Use the resource type on the path, ignoring any _type parameter
                records = persistence.changes(context, count, since, before, historyContext.getChangeIdMarker(), Collections.singletonList(resourceType),
                        historyContext.isExcludeTransactionTimeoutWindow(), historyContext.getHistorySortOrder());
            } else if (historyContext.getResourceTypes().size() > 0) {
                // New API allows us to filter using multiple resource type names, but first we
                // have to check the interaction is allowed for each one
                for (String rt: historyContext.getResourceTypes()) {
                    validateInteraction(Interaction.HISTORY, rt);
                }
                records = persistence.changes(context, count, since, before, historyContext.getChangeIdMarker(), historyContext.getResourceTypes(),
                        historyContext.isExcludeTransactionTimeoutWindow(), historyContext.getHistorySortOrder());
            } else {
                // no resource type filter
                final List<String> NULL_RESOURCE_TYPE_NAMES = null;
                records = persistence.changes(context, count, since, before, historyContext.getChangeIdMarker(), NULL_RESOURCE_TYPE_NAMES,
                        historyContext.isExcludeTransactionTimeoutWindow(), historyContext.getHistorySortOrder());
            }

            if (historyContext.getReturnPreference() == HTTPReturnPreference.REPRESENTATION
                    || historyContext.getReturnPreference() == HTTPReturnPreference.OPERATION_OUTCOME) {
                // vread the resources so that we can include them in the response bundle
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Fetching resources for history response bundle, count=" + records.size());
                }
                resources = persistence.readResourcesForRecords(records);

                if (resources.size() != records.size()) {
                    // very unlikely...unless we overlap with a patient erase operation
                    throw new FHIRPersistenceException("Could not read all required resource records");
                }
            }
        } catch (FHIRPersistenceDataAccessException x) {
            log.log(Level.SEVERE, "Error reading history; params = {" + historyContext + "}", x);
            throw x;
        } finally {
            txn.end();
        }


        // Create a history bundle and add an entry for each record
        Bundle.Builder bundleBuilder = Bundle.builder();

        Long changeIdMarker = null;
        Instant lastChangeTime = null;

        // The records are ordered correctly by the query, so we just need to
        // output them in the same order.
        for (int i=0; i<records.size(); i++) {
            ResourceChangeLogRecord changeRecord = records.get(i);
            Resource resource = resources != null ? resources.get(i) : null; // REPRESENTATION or OPERATION_OUTCOME
            if (historyContext.getHistorySortOrder() == HistorySortOrder.ASC_LAST_UPDATED) {
                if (lastChangeTime == null || changeRecord.getChangeTstamp().isAfter(lastChangeTime)) {
                    // Keep track of the latest timestamp and the corresponding resource (change) id
                    lastChangeTime = changeRecord.getChangeTstamp();
                    changeIdMarker = changeRecord.getChangeId();
                }
            } else if (historyContext.getHistorySortOrder() == HistorySortOrder.DESC_LAST_UPDATED) {
                if (lastChangeTime == null || changeRecord.getChangeTstamp().isBefore(lastChangeTime)) {
                    // Keep track of the earliest timestamp and the corresponding resource (change) id
                    lastChangeTime = changeRecord.getChangeTstamp();
                    changeIdMarker = changeRecord.getChangeId();
                }
            } else if (changeIdMarker == null || changeRecord.getChangeId() > changeIdMarker) {
                // keep track of the greatest change id value when scanning forwards
                // using the primary key
                changeIdMarker = changeRecord.getChangeId();
            }

            Request.Builder requestBuilder = Request.builder();
            Entry.Response.Builder responseBuilder = Entry.Response.builder();
            switch (changeRecord.getChangeType()) {
            case CREATE:
                requestBuilder.method(changeRecord.getVersionId() > 1 ? HTTPVerb.PUT : HTTPVerb.POST);
                requestBuilder.url(Url.of(changeRecord.getResourceTypeName()));
                responseBuilder.status("201");
                break;
            case UPDATE:
                requestBuilder.method(HTTPVerb.PUT);
                requestBuilder.url(Url.of(changeRecord.getResourceTypeName() + "/" + changeRecord.getLogicalId()));
                responseBuilder.status("200");
                break;
            case DELETE:
                requestBuilder.method(HTTPVerb.DELETE);
                requestBuilder.url(Url.of(changeRecord.getResourceTypeName() + "/" + changeRecord.getLogicalId()));
                responseBuilder.status("200");
                break;
            }

            String fullUrl = getRequestBaseUri(resourceType) + "/" + changeRecord.getResourceTypeName() + "/" + changeRecord.getLogicalId();

            responseBuilder.lastModified(net.sovrinhealth.fhir.model.type.Instant.of(changeRecord.getChangeTstamp().atZone(UTC)));
            responseBuilder.etag("W/\"" + changeRecord.getVersionId() + "\"");
            responseBuilder.location(Url.of(fullUrl + "/_history/" + changeRecord.getVersionId()));

            // Per the R4 spec, the fullUrl should not contain _history/:vid
            Entry.Builder entryBuilder = Entry.builder();
            entryBuilder.id(Long.toString(changeRecord.getChangeId()));
            entryBuilder.fullUrl(Url.of(fullUrl));
            entryBuilder.request(requestBuilder.build());
            entryBuilder.response(responseBuilder.build());

            // history bundle entry should not include any resource for deleted entries,
            // also resource may be null if returnPreference=minimal
            if (changeRecord.getChangeType() != ChangeType.DELETE && resource != null) {
                entryBuilder.resource(resource);
            }
            bundleBuilder.entry(entryBuilder.build());
        }

        // Get the service base address to use for next and self links
        String serviceBase = ReferenceUtil.getBaseUrl(null);
        if (serviceBase.endsWith("/")) {
            serviceBase = serviceBase.substring(0, serviceBase.length()-1);
        }

        if (changeIdMarker != null || lastChangeTime != null) {
            // post the next link which a client can use to get the next set of changes.
            // If this link is not included, the client can assume we've reached the end.

            StringBuilder nextRequest = new StringBuilder();
            nextRequest.append(serviceBase);
            if (resourceType != null) {
                // note that the serviceBase includes /_history for whole system history,
                // but not for the resourceType/ whole history interaction
                nextRequest.append("/").append(resourceType).append("/_history");
            }
            nextRequest.append("?");
            nextRequest.append("_count=").append(count);

            if (resourceType == null && historyContext.getResourceTypes().size() > 0) {
                nextRequest.append("&_type=");
                nextRequest.append(String.join(",", historyContext.getResourceTypes()));
            }

            if (historyContext.isExcludeTransactionTimeoutWindow()) {
                nextRequest.append("&_excludeTransactionTimeoutWindow=true");
            }

            switch (historyContext.getHistorySortOrder()) {
            case ASC_LAST_UPDATED:
                nextRequest.append("&_sort=" + HistorySortOrder.ASC_LAST_UPDATED.toString());
                nextRequest.append("&_since=").append(lastChangeTime.atZone(UTC).format(DateTime.PARSER_FORMATTER));
                if (historyContext.getBefore() != null && historyContext.getBefore().getValue() != null) {
                    // make sure we keep including the before filter specified in the original request
                    nextRequest.append("&_before=").append(historyContext.getBefore().getValue().format(DateTime.PARSER_FORMATTER));
                }

                if (changeIdMarker != null) {
                    // good way to exclude this resource on the next call
                    nextRequest.append("&_changeIdMarker=").append(changeIdMarker);
                }
                break;
            case DESC_LAST_UPDATED:
                nextRequest.append("&_sort=" + HistorySortOrder.DESC_LAST_UPDATED.toString());
                nextRequest.append("&_before=").append(lastChangeTime.atZone(UTC).format(DateTime.PARSER_FORMATTER));
                if (historyContext.getSince() != null && historyContext.getSince().getValue() != null) {
                    // make sure we keep including the since filter specified in the original request
                    nextRequest.append("&_since=").append(historyContext.getSince().getValue().format(DateTime.PARSER_FORMATTER));
                }
                if (changeIdMarker != null) {
                    // good way to exclude this resource on the next call
                    nextRequest.append("&_changeIdMarker=").append(changeIdMarker);
                }
                break;
            case NONE:
                nextRequest.append("&_sort=" + HistorySortOrder.NONE.toString());
                nextRequest.append("&_changeIdMarker=").append(changeIdMarker);
                // since/before range filters should not be common when using system-defined
                // sort order (HistorySortOrder.NONE) but are supported. Note the before
                // value will interact with _excludeTransactionTimeoutWindow
                if (historyContext.getSince() != null && historyContext.getSince().getValue() != null) {
                    nextRequest.append("&_since=").append(historyContext.getSince().getValue().format(DateTime.PARSER_FORMATTER));
                }

                if (historyContext.getBefore() != null && historyContext.getBefore().getValue() != null) {
                    nextRequest.append("&_before=").append(historyContext.getBefore().getValue().format(DateTime.PARSER_FORMATTER));
                }
                break;
            }

            Bundle.Link.Builder linkBuilder = Bundle.Link.builder();
            linkBuilder.url(Uri.of(nextRequest.toString()));
            linkBuilder.relation(net.sovrinhealth.fhir.model.type.String.of("next"));

            bundleBuilder.link(linkBuilder.build());
        }

        // Add a self link
        StringBuilder selfRequest = new StringBuilder();
        selfRequest.append(serviceBase);
        if (resourceType != null) {
            selfRequest.append("/").append(resourceType);
        }
        selfRequest.append("?");
        selfRequest.append("_count=").append(count);

        if (historyContext.getChangeIdMarker() != null) {
            selfRequest.append("&_changeIdMarker=").append(historyContext.getChangeIdMarker());
        }

        if (historyContext.getSince() != null && historyContext.getSince().getValue() != null) {
            selfRequest.append("&_since=").append(historyContext.getSince().getValue().format(DateTime.PARSER_FORMATTER));
        }

        if (historyContext.getBefore() != null && historyContext.getBefore().getValue() != null) {
            selfRequest.append("&_before=").append(historyContext.getBefore().getValue().format(DateTime.PARSER_FORMATTER));
        }

        if (resourceType == null && historyContext.getResourceTypes().size() > 0) {
            selfRequest.append("&_type=");
            selfRequest.append(String.join(",", historyContext.getResourceTypes()));
        }

        if (historyContext.isExcludeTransactionTimeoutWindow()) {
            selfRequest.append("&_excludeTransactionTimeoutWindow=true");
        }

        switch (historyContext.getHistorySortOrder()) {
        case ASC_LAST_UPDATED:
            selfRequest.append("&_sort=" + HistorySortOrder.ASC_LAST_UPDATED.toString());
            break;
        case DESC_LAST_UPDATED:
            selfRequest.append("&_sort=" + HistorySortOrder.DESC_LAST_UPDATED.toString());
            break;
        case NONE:
            selfRequest.append("&_sort=" + HistorySortOrder.NONE.toString());
            break;
        }

        Bundle.Link.Builder linkBuilder = Bundle.Link.builder();
        linkBuilder.url(Uri.of(selfRequest.toString()));
        linkBuilder.relation(net.sovrinhealth.fhir.model.type.String.of("self"));
        bundleBuilder.link(linkBuilder.build());
        bundleBuilder.type(BundleType.HISTORY);
        Bundle bundle = bundleBuilder.build();

        // Invoke the 'afterHistory' interceptor methods.
        event.setFhirResource(bundle);
        getInterceptorMgr().fireAfterHistoryEvent(event);

        // See if the interceptor modified the result bundle
        Resource x = event.getFhirResource();
        if (x != null && x.is(Bundle.class)) {
            bundle = x.as(Bundle.class);
        }

        return bundle;
    }

    @Override
    public ResourceEraseRecord doErase(FHIROperationContext operationContext, EraseDTO eraseDto) throws FHIROperationException {
        // @implNote doReindex has a nice pattern to handle some retries in case of deadlock exceptions
        FHIRPersistenceContext context = null;
        final int TX_ATTEMPTS = 5;
        int attempt = 1;
        ResourceEraseRecord eraseRecord = new ResourceEraseRecord();
        do {
            FHIRTransactionHelper txn = null;
            try {
                txn = new FHIRTransactionHelper(getTransaction());
                txn.begin();
                eraseRecord = persistence.erase(context, eraseDto);
                attempt = TX_ATTEMPTS; // end the retry loop
            } catch (FHIRPersistenceDataAccessException x) {
                if (x.isTransactionRetryable() && attempt < TX_ATTEMPTS) {
                    log.info("attempt #" + attempt + " failed, retrying transaction");
                } else {
                    throw new FHIROperationException("Error during $erase", x);
                }
            } catch (Exception x) {
                attempt = TX_ATTEMPTS; // end the retry loop
                throw new FHIROperationException("Error during $erase", x);
            } finally {
                if (txn != null) {
                    txn.end();
                }
            }
        } while (attempt++ < TX_ATTEMPTS);
        return eraseRecord;
    }

    @Override
    public List<Long> doRetrieveIndex(FHIROperationContext operationContext, String resourceTypeName, int count, Instant notModifiedAfter, Long afterIndexId) throws Exception {
        List<Long> indexIds = null;

        FHIRPersistenceContext context = null;

        FHIRTransactionHelper txn = null;
        try {
            txn = new FHIRTransactionHelper(getTransaction());
            txn.begin();
            indexIds = persistence.retrieveIndex(context, count, notModifiedAfter, afterIndexId, resourceTypeName);
        } finally {
            if (txn != null) {
                txn.end();
            }
        }

        return indexIds;
    }

    @Override
    public String generateResourceId() {
        return persistence.generateResourceId();
    }

    @Override
    public PayloadPersistenceResponse storePayload(Resource resource, String logicalId, int newVersionNumber, String resourcePayloadKey) throws Exception {

        // Delegate to the persistence layer. Result will be null if offloading is not supported
        return persistence.storePayload(resource, logicalId, newVersionNumber, resourcePayloadKey);
    }
}
