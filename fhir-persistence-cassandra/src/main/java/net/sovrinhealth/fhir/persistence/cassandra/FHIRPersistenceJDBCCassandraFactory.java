/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.cassandra;

import net.sovrinhealth.fhir.persistence.cassandra.cql.DatasourceSessions;
import net.sovrinhealth.fhir.persistence.cassandra.payload.FHIRPayloadPersistenceCassandraImpl;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.jdbc.FHIRPersistenceJDBCFactory;
import net.sovrinhealth.fhir.persistence.payload.FHIRPayloadPersistence;

/**
 * Factory for creating a hybrid JDBC/Cassandra persistence implementation
 */
public class FHIRPersistenceJDBCCassandraFactory extends FHIRPersistenceJDBCFactory {

    @Override
    public FHIRPayloadPersistence getPayloadPersistence() throws FHIRPersistenceException {
        
        // If payload persistence is configured for this tenant, provide
        // the impl otherwise null
        FHIRPayloadPersistence result = null;
        if (DatasourceSessions.isPayloadPersistenceConfigured()) {
            result = new FHIRPayloadPersistenceCassandraImpl();
        }
        
        return result;
    }
}
