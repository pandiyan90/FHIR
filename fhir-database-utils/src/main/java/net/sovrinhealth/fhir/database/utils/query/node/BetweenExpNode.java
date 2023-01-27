/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.query.node;

/**
 * Addition node
 */
public class BetweenExpNode extends BinaryExpNode {

    @Override
    public <T> T visit(ExpNodeVisitor<T> visitor) {
        T leftValue = getLeft().visit(visitor);
        T rightValue = getRight().visit(visitor);
        return visitor.between(leftValue, rightValue);
    }

    @Override
    public int precedence() {
        return 6;
    }
}