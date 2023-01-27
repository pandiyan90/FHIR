/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.model;

import net.sovrinhealth.fhir.database.utils.api.IDatabaseTypeAdapter;

/**
 * Int Column
 */
public class IntColumn extends ColumnBase {

    /**
     * @param name
     */
    public IntColumn(String name, boolean nullable) {
        super(name, nullable);
    }

    @Override
    public String getTypeInfo(IDatabaseTypeAdapter adapter) {
        return "INT";
    }

}
