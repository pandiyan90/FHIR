/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.domain;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.search.parameters.QueryParameter;

/**
 * A reference search parameter
 */
public class ReferenceSearchParam extends SearchParam {

    /**
     * Public constructor
     * @param rootResourceType
     * @param name
     * @param queryParameter
     */
    public ReferenceSearchParam(String rootResourceType, String name, QueryParameter queryParameter) {
        super(rootResourceType, name, queryParameter);
    }

    @Override
    public <T> T visit(T queryData, SearchQueryVisitor<T> visitor) throws FHIRPersistenceException {
        QueryParameter queryParm = getQueryParameter();
        return visitor.addReferenceParam(queryData, getRootResourceType(), queryParm);
    }
}