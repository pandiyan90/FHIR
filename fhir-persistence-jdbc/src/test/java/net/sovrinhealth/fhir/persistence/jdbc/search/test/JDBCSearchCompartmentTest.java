/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.search.test;

import net.sovrinhealth.fhir.config.FHIRConfigProvider;
import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.jdbc.test.util.PersistenceTestSupport;
import net.sovrinhealth.fhir.persistence.search.test.AbstractSearchCompartmentTest;
import net.sovrinhealth.fhir.search.util.SearchHelper;

/**
 * JDBC unit-tests for compartment-based searches
 */
public class JDBCSearchCompartmentTest extends AbstractSearchCompartmentTest {

    // Container to hide the instantiation of the persistence impl used for tests
    private PersistenceTestSupport testSupport;

    @Override
    public void bootstrapDatabase() throws Exception {
        testSupport = new PersistenceTestSupport();
    }

    @Override
    public FHIRPersistence getPersistenceImpl(FHIRConfigProvider configProvider, SearchHelper searchHelper) throws Exception {
        return testSupport.getPersistenceImpl(configProvider, searchHelper);
    }

    @Override
    protected void shutdownPools() throws Exception {
        if (testSupport != null) {
            testSupport.shutdown();
        }
    }
}