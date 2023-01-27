/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.test;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import net.sovrinhealth.fhir.config.FHIRConfiguration;
import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.examples.Index;
import net.sovrinhealth.fhir.model.spec.test.R4ExamplesDriver;
import net.sovrinhealth.fhir.validation.test.ValidationProcessor;

public class ExamplesDriverTest {

    @BeforeClass
    public void setup() {
        FHIRConfiguration.setConfigHome("target/test-classes");
    }

    /**
     * Process all the examples in the fhir-r4-spec example library
     */
    @Test(groups = { "server-examples" })
    public void processExamples() throws Exception {
        FHIRRequestContext.get().setTenantId("default");
        // Process each of the examples using the provided ExampleRequestProcessor. We want to
        // validate first before we try and send to FHIR
        final R4ExamplesDriver driver = new R4ExamplesDriver();
        driver.setValidator(new ValidationProcessor());
        driver.setProcessor(new ExtractorRequestProcessor());
        String index = System.getProperty(this.getClass().getName()
            + ".index", Index.MINIMAL_JSON.name());
        driver.processIndex(Index.valueOf(index));
        FHIRRequestContext.remove();
     }
    
}
