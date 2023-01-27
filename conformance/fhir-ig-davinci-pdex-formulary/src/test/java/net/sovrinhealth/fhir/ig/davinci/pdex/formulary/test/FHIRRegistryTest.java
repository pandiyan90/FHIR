/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.pdex.formulary.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.CapabilityStatement;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class FHIRRegistryTest {
    @Test
    public void testRegistry() {
        CapabilityStatement definition = FHIRRegistry.getInstance()
                .getResource("http://hl7.org/fhir/us/davinci-drug-formulary/CapabilityStatement/usdf-server", CapabilityStatement.class);
        Assert.assertNotNull(definition);
    }
}