/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.model;

import net.sovrinhealth.fhir.database.utils.api.IDatabaseTypeAdapter;
import net.sovrinhealth.fhir.database.utils.postgres.PostgresAdapter;

/**
 * Column acting as either a boolean or smallint depending on the underlying
 * database type
 */
public class SmallIntBooleanColumn extends ColumnBase {
    /**
     * @param name
     * @param nullable
     * @param defaultValue
     */
    public SmallIntBooleanColumn(String name, boolean nullable, String defaultValue) {
        super(name, nullable, defaultValue);
    }

    @Override
    public String getTypeInfo(IDatabaseTypeAdapter adapter) {
        if (adapter instanceof PostgresAdapter) {
            this.resetDefaultValue();
            return "BOOLEAN";
        } else {
            return "SMALLINT";
        }
    }
}