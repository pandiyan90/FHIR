/*
 * (C) Copyright IBM Corp. 2017, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.test;

import static org.testng.AssertJUnit.assertNotNull;

import java.sql.Connection;
import java.util.Properties;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.database.utils.pool.PoolConnectionProvider;
import net.sovrinhealth.fhir.persistence.jdbc.connection.Action;
import net.sovrinhealth.fhir.persistence.jdbc.connection.FHIRDbConnectionStrategy;
import net.sovrinhealth.fhir.persistence.jdbc.connection.FHIRDbConstants;
import net.sovrinhealth.fhir.persistence.jdbc.connection.FHIRDbTestConnectionStrategy;
import net.sovrinhealth.fhir.persistence.jdbc.connection.SchemaNameFromProps;
import net.sovrinhealth.fhir.persistence.jdbc.connection.SetSchemaAction;
import net.sovrinhealth.fhir.persistence.jdbc.dao.api.FHIRDbDAO;
import net.sovrinhealth.fhir.persistence.jdbc.test.util.DerbyInitializer;
import net.sovrinhealth.fhir.schema.derby.DerbyFhirDatabase;

/**
 * This class tests the functions of the FHIR DB Data Access Object class.
 */
public class FHIRDbDAOTest {
    /**
     * Tests acquiring a connection to a Derby FHIR database using the embedded Derby driver. 
     * If the database does not exist prior to the test, it should be created.
     * @throws Exception
     */
    @Test(groups = {"jdbc"})
    public void testDerbyConnectionStrategy() throws Exception {
        
        // DerbyFhirDatabase will initialize the full FHIR schema if necessary.
        // Don't close, because we want to avoid shutting the database down because
        // it will be used by other tests in the suite
        DerbyFhirDatabase database = new DerbyFhirDatabase(DerbyInitializer.DB_NAME);
        PoolConnectionProvider connectionPool = new PoolConnectionProvider(database, 1);
        // ITransactionProvider transactionProvider = new SimpleTransactionProvider(connectionPool);

        // Test creation of a DAO instance with a connection strategy.

        Action action = new SetSchemaAction(new SchemaNameFromProps("FHIRDATA"), null);
        FHIRDbConnectionStrategy strat = new FHIRDbTestConnectionStrategy(connectionPool, action);

        // We only ask the DAO for a connection
        try (Connection c = strat.getConnection()) {
            assertNotNull(c);
        }
    }
}
