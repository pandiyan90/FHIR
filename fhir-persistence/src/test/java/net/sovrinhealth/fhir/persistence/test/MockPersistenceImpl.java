/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.FHIRPersistenceTransaction;
import net.sovrinhealth.fhir.persistence.HistorySortOrder;
import net.sovrinhealth.fhir.persistence.MultiResourceResult;
import net.sovrinhealth.fhir.persistence.ResourceChangeLogRecord;
import net.sovrinhealth.fhir.persistence.ResourcePayload;
import net.sovrinhealth.fhir.persistence.SingleResourceResult;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.payload.PayloadPersistenceResponse;

/**
 * Mock implementation of FHIRPersistence for use during testing.
 *
 */
public class MockPersistenceImpl implements FHIRPersistence {

    @Override
    public <T extends Resource> SingleResourceResult<T> create(FHIRPersistenceContext context, T resource)
            throws FHIRPersistenceException {
        return null;
    }

    @Override
    public <T extends Resource> SingleResourceResult<T> read(FHIRPersistenceContext context, Class<T> resourceType, String logicalId)
            throws FHIRPersistenceException {
        return null;
    }

    @Override
    public <T extends Resource> SingleResourceResult<T> vread(FHIRPersistenceContext context, Class<T> resourceType, String logicalId, String versionId)
            throws FHIRPersistenceException {
        return null;
    }

    @Override
    public MultiResourceResult history(FHIRPersistenceContext context, Class<? extends Resource> resourceType, String logicalId) throws FHIRPersistenceException {
        return null;
    }

    @Override
    public MultiResourceResult search(FHIRPersistenceContext context, Class<? extends Resource> resourceType) throws FHIRPersistenceException {
        return null;
    }

    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public FHIRPersistenceTransaction getTransaction() {
        return null;
    }

    @Override
    public OperationOutcome getHealth() throws FHIRPersistenceException {
        return null;
    }

    @Override
    public int reindex(FHIRPersistenceContext context, OperationOutcome.Builder oob, Instant tstamp, List<Long> indexIds,
            String resourceLogicalId, boolean force) throws FHIRPersistenceException {
        return 0;
    }

    @Override
    public String generateResourceId() {
        return null;
    }

    @Override
    public ResourcePayload fetchResourcePayloads(Class<? extends Resource> resourceType, Instant fromLastModified, Instant toLastModified,
            Function<ResourcePayload, Boolean> process) throws FHIRPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ResourceChangeLogRecord> changes(FHIRPersistenceContext context, int resourceCount, Instant fromLastModified, Instant beforeLastModified, Long afterResourceId, List<String> resourceTypeNames,
            boolean excludeTransactionTimeoutWindow, HistorySortOrder historySortOrder) throws FHIRPersistenceException {
        return Collections.emptyList();
    }

    @Override
    public List<Long> retrieveIndex(FHIRPersistenceContext context, int count, java.time.Instant notModifiedAfter, Long afterIndexId, String resourceTypeName) throws FHIRPersistenceException {
        return Collections.emptyList();
    }

    @Override
    public <T extends Resource> SingleResourceResult<T> update(FHIRPersistenceContext context, T resource)
            throws FHIRPersistenceException {
        return null;
    }

    @Override
    public PayloadPersistenceResponse storePayload(Resource resource, String logicalId, int newVersionNumber, String resourcePayloadKey) {
        return null;
    }

    @Override
    public List<Resource> readResourcesForRecords(List<ResourceChangeLogRecord> records) throws FHIRPersistenceException {
        // NOP
        return null;
    }
}