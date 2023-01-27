/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.spec.test;

import net.sovrinhealth.fhir.model.resource.Resource;

/**
 * Processes the examples
 */
public interface IExampleProcessor {

    /**
     * Process the given resource example
     * @param jsonFile
     * @param resource
     * @throws Exception
     */
    void process(String jsonFile, Resource resource) throws Exception;
}
