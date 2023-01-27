/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata;

import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

/**
 * Creates a Patient Export of FHIR Data to NDJSON
 * @see https://hl7.org/Fhir/uv/bulkdata/OperationDefinition-patient-export.json.html
 */
public class PatientExportOperation extends ExportOperation {
    public PatientExportOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/uv/bulkdata/OperationDefinition/patient-export", OperationDefinition.class);
    }
}