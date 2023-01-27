/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.schema.build;

import net.sovrinhealth.fhir.database.utils.api.IDatabaseAdapter;
import net.sovrinhealth.fhir.database.utils.common.PlainSchemaAdapter;

/**
 * Represents an adapter used to build the standard FHIR schema
 */
public class FhirSchemaAdapter extends PlainSchemaAdapter {

    /**
     * Public constructor
     * 
     * @param databaseAdapter
     */
    public FhirSchemaAdapter(IDatabaseAdapter databaseAdapter) {
        super(databaseAdapter);
    }
}
