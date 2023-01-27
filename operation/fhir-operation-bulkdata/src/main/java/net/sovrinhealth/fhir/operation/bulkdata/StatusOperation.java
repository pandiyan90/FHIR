/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata;

import java.io.InputStream;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.operation.bulkdata.processor.BulkDataFactory;
import net.sovrinhealth.fhir.operation.bulkdata.util.BulkDataExportUtil;
import net.sovrinhealth.fhir.operation.bulkdata.util.CommonUtil;
import net.sovrinhealth.fhir.operation.bulkdata.util.CommonUtil.Type;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.AbstractOperation;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * <a href="https://build.fhir.org/ig/HL7/bulk-data/index.html">BulkDataAccess IG: STU1 - Polling Response</a><br>
 * There are two specific operations
 * <li>status of a bulkdata export/import job</li>
 * <li>delete a bulkdata export/import job</li>
 */
public class StatusOperation extends AbstractOperation {
    private static final String FILE = "status.json";

    private static final CommonUtil COMMON = new CommonUtil(Type.STATUS);
    private static final BulkDataExportUtil export = new BulkDataExportUtil();

    public StatusOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(FILE);) {
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper)
            throws FHIROperationException {
        COMMON.checkEnabled();
        if (logicalId == null && versionId == null && resourceType == null) {
            String method = (String) operationContext.getProperty(FHIROperationContext.PROPNAME_METHOD_TYPE);
            if ("DELETE".equalsIgnoreCase(method)) {
                String job = export.checkAndValidateJob(parameters);
                // For now, we're going to execute the status update, and check.
                // If Base, Export Status (Else Invalid)
                return BulkDataFactory.getInstance(operationContext).delete(job, operationContext);
            } else {
                // Assume GET or POST
                String job = export.checkAndValidateJob(parameters);

                // @implNote We don't need a preflight... we wouldn't have go here otherwise.

                // For now, we're going to execute the status update, and check.
                // If Base, Export Status (Else Invalid)
                return BulkDataFactory.getInstance(operationContext).status(job, operationContext);
            }
        } else {
            // Unsupported on Resource Type
            // Root operation is only supported, and we signal it back here.
            // Don't get fancy, just send it back.
            throw buildExceptionWithIssue("Invalid call $bulkdata-status operation call", IssueType.INVALID);
        }
    }

    @Override
    protected boolean isAdditionalMethodAllowed(String method) {
        return "DELETE".equalsIgnoreCase(method);
    }

}