/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.examples.test;

import net.sovrinhealth.fhir.examples.CompleteMockDataCreator;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.resource.Resource;

public class GenerateSingleResource {
    public static void main(String[] args) throws Exception {
        CompleteMockDataCreator creator = new CompleteMockDataCreator();
        
        Resource resource = creator.createResource("Goal", 1);
        FHIRGenerator generator = FHIRGenerator.generator(Format.JSON, true);
        generator.generate(resource, System.out);
    }
}
