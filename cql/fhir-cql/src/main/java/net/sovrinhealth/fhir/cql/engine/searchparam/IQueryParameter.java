/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.cql.engine.searchparam;

import net.sovrinhealth.fhir.search.SearchConstants.Modifier;

public interface IQueryParameter {

    Boolean getMissing();

    Modifier getModifier();

    String getParameterValue();
}
