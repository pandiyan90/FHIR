/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.test;

import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.FHIRPersistenceFactory;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.search.util.SearchHelper;


/**
 * Mock persistence factory for use during testing.
 */
public class MockExceptionPersistenceFactory implements FHIRPersistenceFactory {

    /**
     * Always throws a NullPointerException
     */
    @SuppressWarnings("null")
    public MockExceptionPersistenceFactory() {
        String a = null;
        a.toString();
    }

    @Override
    public FHIRPersistence getInstance(SearchHelper searchHelper) throws FHIRPersistenceException {
        return null;
    }
}
