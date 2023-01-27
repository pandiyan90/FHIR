/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.schema.control;

import java.sql.Connection;
import java.sql.SQLException;

import net.sovrinhealth.fhir.database.utils.api.IConnectionProvider;
import net.sovrinhealth.fhir.database.utils.api.IDatabaseTranslator;
import net.sovrinhealth.fhir.database.utils.jdbc.ConnectionStub;

/**
 * Used to Test the Schema Build There are no TESTs in this code.
 */
public class ConnectionProviderTestImpl implements IConnectionProvider {

    // Just use a single connection stub
    private final ConnectionStub connection;

    private final IDatabaseTranslator translator;

    public ConnectionProviderTestImpl(IDatabaseTranslator t) {
        this.connection = new ConnectionStub();
        this.translator = t;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public IDatabaseTranslator getTranslator() {
        return this.translator;
    }

    @Override
    public void commitTransaction() throws SQLException {
        // NO Implementation

    }

    @Override
    public void rollbackTransaction() throws SQLException {
        // NO Implementation
    }

    @Override
    public void describe(String prefix, StringBuilder cfg, String key) {
        // NO Implementation
    }

}
