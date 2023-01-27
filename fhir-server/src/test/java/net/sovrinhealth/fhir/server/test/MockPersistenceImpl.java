/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.server.test;

import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Builder;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.FHIRPersistenceTransaction;
import net.sovrinhealth.fhir.persistence.HistorySortOrder;
import net.sovrinhealth.fhir.persistence.InteractionStatus;
import net.sovrinhealth.fhir.persistence.MultiResourceResult;
import net.sovrinhealth.fhir.persistence.ResourceChangeLogRecord;
import net.sovrinhealth.fhir.persistence.ResourcePayload;
import net.sovrinhealth.fhir.persistence.ResourceResult;
import net.sovrinhealth.fhir.persistence.SingleResourceResult;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.payload.PayloadPersistenceResponse;
import net.sovrinhealth.fhir.server.util.FHIRRestHelperTest;

/**
 * Mock implementation of FHIRPersistence for use during testing.
 */
public class MockPersistenceImpl implements FHIRPersistence {
    int id = 0;

    @Override
    public <T extends Resource> SingleResourceResult<T> create(FHIRPersistenceContext context, T resource)
            throws FHIRPersistenceException {

        SingleResourceResult.Builder<T> resultBuilder = new SingleResourceResult.Builder<T>()
                .success(true)
                .interactionStatus(InteractionStatus.MODIFIED)
                .resource(resource);
        return resultBuilder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Resource> SingleResourceResult<T> read(FHIRPersistenceContext context, Class<T> resourceType, String logicalId)
            throws FHIRPersistenceException {
        // TODO. Why this logic? Definitely worthy of a comment
        if (logicalId.startsWith("generated")) {
            return new SingleResourceResult.Builder<T>()
                    .interactionStatus(InteractionStatus.READ)
                    .success(true)
                    .resource(null).build();
        } else {
            T updatedResource = (T) Patient.builder().id("test").meta(Meta.builder().versionId(Id.of("1")).lastUpdated(Instant.now()).build()).build();
            return new SingleResourceResult.Builder<T>()
                    .interactionStatus(InteractionStatus.READ)
                    .success(true)
                    .resource(updatedResource).build();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Resource> SingleResourceResult<T> vread(FHIRPersistenceContext context, Class<T> resourceType, String logicalId, String versionId)
            throws FHIRPersistenceException {
        if (logicalId.startsWith("generated")) {
            return new SingleResourceResult.Builder<T>()
                    .success(true)
                    .interactionStatus(InteractionStatus.READ)
                    .resource(null).build();
        } else {
            T updatedResource = (T) Patient.builder().id("test").meta(Meta.builder().versionId(Id.of("1")).lastUpdated(Instant.now()).build()).build();
            return new SingleResourceResult.Builder<T>()
                    .success(true)
                    .interactionStatus(InteractionStatus.READ)
                    .resource(updatedResource).build();
        }
    }

    @Override
    public <T extends Resource> SingleResourceResult<T> update(FHIRPersistenceContext context, T resource)
            throws FHIRPersistenceException {
        OperationOutcome operationOutcome = null;
        if (resource.getLanguage() != null && resource.getLanguage().getValue().equals("en-US")) {
            operationOutcome = FHIRRestHelperTest.ID_SPECIFIED;
        }
        SingleResourceResult.Builder<T> resultBuilder = new SingleResourceResult.Builder<T>()
                .success(true)
                .resource(resource) // persistence layer should no longer change the resource!
                .interactionStatus(InteractionStatus.MODIFIED)
                .outcome(operationOutcome);
        return resultBuilder.build();
    }

    @Override
    public MultiResourceResult history(FHIRPersistenceContext context, Class<? extends Resource> resourceType, String logicalId) throws FHIRPersistenceException {
        Instant lastUpdated = Instant.now(ZoneOffset.UTC);
        Patient updatedResource = Patient.builder().id("test").meta(Meta.builder().versionId(Id.of("1")).lastUpdated(lastUpdated).build()).build();
        ResourceResult<Resource> resourceResult = ResourceResult.builder()
                .resource(updatedResource)
                .logicalId(logicalId)
                .version(1)
                .resourceTypeName(updatedResource.getClass().getSimpleName())
                .lastUpdated(lastUpdated.getValue().toInstant())
                .build();
        return MultiResourceResult.builder()
                .success(true)
                .resourceResult(resourceResult).build();
    }

    @Override
    public MultiResourceResult search(FHIRPersistenceContext context, Class<? extends Resource> resourceType) throws FHIRPersistenceException {
        return MultiResourceResult.builder()
                .success(true)
                .build();
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public FHIRPersistenceTransaction getTransaction() {
        return new MockTransactionAdapter();
    }

    @Override
    public OperationOutcome getHealth() throws FHIRPersistenceException {
        return null;
    }

    @Override
    public String generateResourceId() {
        return "generated-" + id++;
    }

    @Override
    public int reindex(FHIRPersistenceContext context, Builder operationOutcomeResult, java.time.Instant tstamp, List<Long> indexIds,
        String resourceLogicalId, boolean force) throws FHIRPersistenceException {
        return 0;
    }

    @Override
    public <T extends Resource> void delete(FHIRPersistenceContext context, Class<T> resourceType, String logicalId, int versionId,
            net.sovrinhealth.fhir.model.type.Instant lastUpdated) throws FHIRPersistenceException {
        // NOP. No need to do anything in this very simple mock
    }

    @Override
    public ResourcePayload fetchResourcePayloads(Class<? extends Resource> resourceType, java.time.Instant fromLastModified,
        java.time.Instant toLastModified, Function<ResourcePayload, Boolean> process) throws FHIRPersistenceException {
        // NOP
        return null;
    }

    @Override
    public List<ResourceChangeLogRecord> changes(FHIRPersistenceContext context, int resourceCount, java.time.Instant sinceLastModified, java.time.Instant beforeLastModified,
            Long changeIdMarker, List<String> resourceTypeNames, boolean excludeTransactionTimeoutWindow, HistorySortOrder historySortOrder)
            throws FHIRPersistenceException {
        // NOP
        return null;
    }

    @Override
    public List<Long> retrieveIndex(FHIRPersistenceContext context, int count, java.time.Instant notModifiedAfter, Long afterIndexId, String resourceTypeName) throws FHIRPersistenceException {
        // NOP
        return null;
    }

    @Override
    public PayloadPersistenceResponse storePayload(Resource resource, String logicalId, int newVersionNumber, String resourcePayloadKey) throws FHIRPersistenceException {
        // NOP
        return null;
    }

    @Override
    public List<Resource> readResourcesForRecords(List<ResourceChangeLogRecord> records) throws FHIRPersistenceException {
        // NOP
        return null;
    }
}