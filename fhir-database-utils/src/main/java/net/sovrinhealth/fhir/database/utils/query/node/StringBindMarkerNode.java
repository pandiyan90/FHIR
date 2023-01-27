/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.query.node;

import net.sovrinhealth.fhir.database.utils.query.expression.BindMarkerNodeVisitor;

/**
 * A bind marker representing a String value
 */
public class StringBindMarkerNode extends BindMarkerNode {
    // The string value (can be null)
    private final String value;

    public StringBindMarkerNode(String value) {
        this.value = value;
    }

    @Override
    public <T> T visit(ExpNodeVisitor<T> visitor) {
        return visitor.bindMarker(value);
    }

    @Override
    public void visit(BindMarkerNodeVisitor visitor) {
        visitor.bindString(value);
    }

    @Override
    public boolean checkTypeAndValue(Object expectedValue) {
        if (value == null) {
            return expectedValue == null;
        } else if (expectedValue instanceof String) {
            return this.value.equals(expectedValue);
        } else {
            return false;
        }
    }

    @Override
    public String toValueString(String defaultValue) {
        return this.value != null ? value.toString() : defaultValue;
    }
}