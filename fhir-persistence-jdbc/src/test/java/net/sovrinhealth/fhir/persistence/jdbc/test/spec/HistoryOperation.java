/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.test.spec;

import java.util.List;

import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.persistence.ResourceResult;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;

/**
 * Test the history interaction for a resource
 */
public class HistoryOperation extends BaseOperation {
    
    // the number of resource versions we expect to read from the database
    final int expectedCount;

    /**
     * Public constructor
     * @param expectedCount
     */
    public HistoryOperation(int expectedCount) {
        this.expectedCount = expectedCount;
    }

	@Override
	public void process(TestContext tc) throws FHIRPersistenceException {

	    // history operations need a persistence context configured with a FHIRHistoryContext
	    final FHIRPersistenceContext context = tc.createHistoryPersistenceContext();
        final Resource resource = tc.getResource();
        
        final String logicalId = resource.getId();
        
        List<ResourceResult<? extends Resource>> resources = tc.getPersistence().history(context, resource.getClass(), logicalId).getResourceResults();
        if (resources.size() != this.expectedCount) {
            throw new AssertionError(resource.getClass().getSimpleName() + "/" + logicalId + " history returned "
                + resources.size() + ", expected " + this.expectedCount);
        }
		
	}

}
