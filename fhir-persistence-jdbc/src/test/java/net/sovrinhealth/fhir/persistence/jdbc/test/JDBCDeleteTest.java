/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.test;

import net.sovrinhealth.fhir.config.FHIRConfigProvider;
import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.jdbc.test.util.PersistenceTestSupport;
import net.sovrinhealth.fhir.persistence.test.common.AbstractDeleteTest;
import net.sovrinhealth.fhir.search.util.SearchHelper;

/**
 * Concrete subclass for delete tests run against the JDBC schema.
 */
public class JDBCDeleteTest extends AbstractDeleteTest {

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
