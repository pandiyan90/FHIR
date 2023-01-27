/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test;

import net.sovrinhealth.fhir.persistence.FHIRPersistenceTransaction;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;

public class MockTransactionAdapter implements FHIRPersistenceTransaction {
    public MockTransactionAdapter() {
    }

    @Override
    public void begin() throws FHIRPersistenceException {
    }

    @Override
    public void end() throws FHIRPersistenceException {
    }

    @Override
    public void setRollbackOnly() throws FHIRPersistenceException {
    }

    @Override
    public boolean hasBegun() throws FHIRPersistenceException {
        return false;
    }
}