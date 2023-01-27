/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.model;

/**
 * Basic set of privileges that can be granted to a database object
 */
public enum Privilege {
    ALL,
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    READ,
    WRITE,
    EXECUTE,
    USAGE
}
