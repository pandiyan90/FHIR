/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.test.spec;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;

/**
 * A test operation on a Resource
 */
@FunctionalInterface
public interface ITestResourceOperation {

	/**
	 * Process the resource and update the context as needed
	 * @param context
	 */
	public void process(TestContext context) throws FHIRPersistenceException;
}
