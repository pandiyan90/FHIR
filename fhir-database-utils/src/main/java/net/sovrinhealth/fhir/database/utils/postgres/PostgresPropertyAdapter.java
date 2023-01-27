/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.postgres;

import java.util.Properties;

import net.sovrinhealth.fhir.database.utils.common.JdbcPropertyAdapter;

/**
 * An adapter for Postgres properties
 */
public class PostgresPropertyAdapter extends JdbcPropertyAdapter {
    public PostgresPropertyAdapter(Properties properties) {
        super(properties);
    }
}