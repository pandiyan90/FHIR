/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.query.expression;

/**
 * A reference to a column. Typed to help us distinguish from literal strings
 * when building expressions
 */
public class ColumnRef {

    // The column reference (e.g. "LR.LOGICAL_ID")
    private final String ref;

    public ColumnRef(String ref) {
        this.ref = ref;
    }

    /**
     * Getter for the column reference value
     */
    public String getRef() {
        return this.ref;
    }

    @Override
    public String toString() {
        return this.ref;
    }
}