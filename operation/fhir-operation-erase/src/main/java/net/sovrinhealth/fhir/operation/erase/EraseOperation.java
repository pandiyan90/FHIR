/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.erase;

import java.io.InputStream;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.operation.erase.adapter.ResourceEraseRecordAdapter;
import net.sovrinhealth.fhir.operation.erase.audit.EraseOperationAuditLogger;
import net.sovrinhealth.fhir.operation.erase.impl.EraseRest;
import net.sovrinhealth.fhir.operation.erase.impl.EraseRestFactory;
import net.sovrinhealth.fhir.persistence.ResourceEraseRecord;
import net.sovrinhealth.fhir.persistence.erase.EraseDTO;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.AbstractOperation;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationUtil;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * Custom Operation to Erase a specific instance or instance-version of the FHIR
 * Resource and it's history and values table supported by the IBM FHIR Server.
 */
public class EraseOperation extends AbstractOperation {
    private ResourceEraseRecordAdapter adapter = new ResourceEraseRecordAdapter();

    public EraseOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        FHIRParser parser = FHIRParser.parser(Format.JSON);
        return generateOperationDefinition(parser);
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper) throws FHIROperationException {
        // Create the audit, so we get the start time.
        EraseOperationAuditLogger audit = new EraseOperationAuditLogger(operationContext);

        EraseRest erase = EraseRestFactory.getInstance(operationContext, parameters, resourceType, logicalId);

        // Authorize should be executed first so it is the first error before enabled check.
        erase.authorize();
        erase.enabled();
        EraseDTO eraseDto = erase.verify();

        try {
            ResourceEraseRecord eraseRecord = resourceHelper.doErase(operationContext, eraseDto);
            // Checks to see if it's not found
            if (ResourceEraseRecord.Status.NOT_FOUND == eraseRecord.getStatus()) {
                FHIROperationException notFoundEx = FHIROperationUtil.buildExceptionWithIssue("Resource not found", IssueType.NOT_FOUND);
                audit.error(parameters, notFoundEx, eraseDto);
                throw notFoundEx;
            } else if (ResourceEraseRecord.Status.NOT_SUPPORTED_GREATER == eraseRecord.getStatus()) {
                FHIROperationException badVersion = FHIROperationUtil.buildExceptionWithIssue("Resource Version specified is greater than the version found", IssueType.INVALID);
                audit.error(parameters, badVersion, eraseDto);
                throw badVersion;
            } else if (ResourceEraseRecord.Status.NOT_SUPPORTED_LATEST == eraseRecord.getStatus()) {
                FHIROperationException badVersion = FHIROperationUtil.buildExceptionWithIssue("Resource Version specified is the latest version found", IssueType.INVALID);
                audit.error(parameters, badVersion, eraseDto);
                throw badVersion;
            }

            // Adapt to the output type and log it.
            Parameters response = adapter.adapt(eraseRecord, eraseDto);
            audit.audit(response, eraseDto);

            return response;
        } catch (FHIROperationException e) {
            audit.error(parameters, e, eraseDto);
            throw e;
        }
    }

    /**
     * Parser generates the OperationDefinition
     * @implNote this is broken down into a separate method to facilitate testing.
     * @param parser
     * @return
     */
    public OperationDefinition generateOperationDefinition(FHIRParser parser) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("erase.json")) {
            return parser.parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}