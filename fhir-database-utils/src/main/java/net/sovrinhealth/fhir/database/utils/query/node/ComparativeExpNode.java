/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.query.node;

/**
 * Base for all comparative type expressions (<, <=, >, >=)
 */
public abstract class ComparativeExpNode extends BinaryExpNode {

    @Override
    public int precedence() {
        return 4;
    }
}