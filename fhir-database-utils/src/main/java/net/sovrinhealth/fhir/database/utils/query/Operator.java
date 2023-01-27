/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.query;


/**
 * Operators for comparing two values
 */
public enum Operator {
    EQ, LIKE, LT, LTE, GT, GTE, NE
}