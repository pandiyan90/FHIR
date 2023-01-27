/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.test.spec;

import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.util.FHIRPersistenceTestSupport;

public class DeleteOperation extends BaseOperation {

    @Override
    public void process(TestContext tc) throws FHIRPersistenceException {
        final Resource resource = tc.getResource();
        final FHIRPersistenceContext context = tc.createPersistenceContext();

        // Resource is no longer stored with the deletion marker, so delete
        // doesn't return anything
        FHIRPersistenceTestSupport.delete(tc.getPersistence(), context, resource);
    }
}