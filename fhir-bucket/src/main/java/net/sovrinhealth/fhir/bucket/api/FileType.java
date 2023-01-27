/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.bucket.api;


/**
 * The type of file/object we are processing
 */
public enum FileType {
    JSON,
    NDJSON,
    UNKNOWN
}
