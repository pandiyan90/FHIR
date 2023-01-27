/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata;

import java.io.InputStream;
import java.util.List;

import net.sovrinhealth.fhir.core.FHIRVersionParam;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.operation.bulkdata.config.preflight.Preflight;
import net.sovrinhealth.fhir.operation.bulkdata.config.preflight.PreflightFactory;
import net.sovrinhealth.fhir.operation.bulkdata.model.type.Input;
import net.sovrinhealth.fhir.operation.bulkdata.model.type.StorageDetail;
import net.sovrinhealth.fhir.operation.bulkdata.processor.BulkDataFactory;
import net.sovrinhealth.fhir.operation.bulkdata.util.BulkDataImportUtil;
import net.sovrinhealth.fhir.operation.bulkdata.util.CommonUtil;
import net.sovrinhealth.fhir.operation.bulkdata.util.CommonUtil.Type;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.AbstractOperation;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * BulkData Specification Proposal:
 * <a href= "https://github.com/smart-on-fhir/bulk-import/blob/master/import.md">$import</a>
 */
public class ImportOperation extends AbstractOperation {
    private static final String FILE = "import.json";

    private static final CommonUtil COMMON = new CommonUtil(Type.IMPORT);

    public ImportOperation() {
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
        COMMON.checkAllowed(operationContext, true);

        // Checks the Import Type
        checkImportType(operationContext.getType());

        BulkDataImportUtil util = new BulkDataImportUtil(operationContext, parameters);

        // Parameter: inputFormat
        String inputFormat = util.retrieveInputFormat();

        // Parameter: inputSource
        String inputSource = util.retrieveInputSource();

        // Parameter: input
        FHIRVersionParam fhirVersion = (FHIRVersionParam) operationContext.getProperty(FHIROperationContext.PROPNAME_FHIR_VERSION);
        List<Input> inputs = util.retrieveInputs(fhirVersion);

        // Parameter: storageDetail
        StorageDetail storageDetail = util.retrieveStorageDetails();

        Preflight preflight =  PreflightFactory.getInstance(operationContext, inputs, null, inputFormat);
        preflight.checkStorageAllowed(storageDetail);
        preflight.preflight(true);
        return BulkDataFactory.getInstance(operationContext, true)
                .importBulkData(inputFormat, inputSource, inputs, storageDetail, operationContext);
    }

    private void checkImportType(FHIROperationContext.Type type) throws FHIROperationException {
        // Check Import Type is System.  We only support system right now.

        if (!FHIROperationContext.Type.SYSTEM.equals(type)) {
            throw buildExceptionWithIssue("Invalid call; $import can only be invoked at the system level",
                    IssueType.INVALID);
        }
    }
}