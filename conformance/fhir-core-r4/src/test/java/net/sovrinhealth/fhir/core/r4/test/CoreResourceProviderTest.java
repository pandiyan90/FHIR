/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.core.r4.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.core.r4.Core401ResourceProvider;
import net.sovrinhealth.fhir.registry.spi.FHIRRegistryResourceProvider;

public class CoreResourceProviderTest {
    @Test
    public void testR4SpecResourceProvider() {
        FHIRRegistryResourceProvider provider = new Core401ResourceProvider();
        Assert.assertEquals(provider.getRegistryResources().size(), 11251);
    }
}
