/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.model;

/**
 * DatabaseObjectType Enumeration
 */
public enum DatabaseObjectType {
    TABLE,
    INDEX,
    PROCEDURE,
    SEQUENCE,
    TYPE,
    PERMISSION,
    VARIABLE,
    NOP,
    GROUP,
    TABLESPACE,
    VIEW
}
