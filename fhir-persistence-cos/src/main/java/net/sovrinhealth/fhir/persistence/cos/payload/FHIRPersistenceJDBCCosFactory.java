/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.cos.payload;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.jdbc.FHIRPersistenceJDBCFactory;
import net.sovrinhealth.fhir.persistence.payload.FHIRPayloadPersistence;

/**
 *
 */
public class FHIRPersistenceJDBCCosFactory extends FHIRPersistenceJDBCFactory {

    @Override
    public FHIRPayloadPersistence getPayloadPersistence() throws FHIRPersistenceException {
        // Store the payload in Cloud Object Storage (Cos)
        return new FHIRPayloadPersistenceCosImpl();
    };
}