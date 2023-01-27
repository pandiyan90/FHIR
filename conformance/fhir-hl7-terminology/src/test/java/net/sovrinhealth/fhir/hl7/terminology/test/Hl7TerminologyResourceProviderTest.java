/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.hl7.terminology.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.hl7.terminology.Hl7Terminology310ResourceProvider;
import net.sovrinhealth.fhir.registry.spi.FHIRRegistryResourceProvider;

public class Hl7TerminologyResourceProviderTest {
    @Test
    public void testR4BSpecResourceProvider() {
        FHIRRegistryResourceProvider provider = new Hl7Terminology310ResourceProvider();
        Assert.assertEquals(provider.getRegistryResources().size(), 3535);
    }
}
