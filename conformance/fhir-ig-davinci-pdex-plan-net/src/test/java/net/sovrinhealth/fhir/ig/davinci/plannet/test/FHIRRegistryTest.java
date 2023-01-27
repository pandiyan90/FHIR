/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.plannet.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.generator.exception.FHIRGeneratorException;
import net.sovrinhealth.fhir.model.resource.CapabilityStatement;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class FHIRRegistryTest {
    @Test
    public void testRegistry() throws FHIRGeneratorException {
        CapabilityStatement definition = FHIRRegistry.getInstance()
                .getResource("http://hl7.org/fhir/us/davinci-pdex-plan-net/CapabilityStatement/plan-net", CapabilityStatement.class);
        FHIRGenerator.generator(Format.XML).generate(definition, System.out);
        Assert.assertNotNull(definition);
    }
}
