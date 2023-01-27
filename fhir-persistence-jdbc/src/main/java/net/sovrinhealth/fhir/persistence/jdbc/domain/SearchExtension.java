/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.domain;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;

/**
 * An extension to the search query which is not related to a single search parameter
 */
public interface SearchExtension {

    <T> T visit(T query, SearchQueryVisitor<T> visitor) throws FHIRPersistenceException;

}