/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.test.spec;

import net.sovrinhealth.fhir.model.resource.Resource;

@FunctionalInterface
public interface IResourceOperation {

	/**
	 * Process the resource
	 * @param resource
	 * @return
	 */
	Resource process(Resource resource);
}
