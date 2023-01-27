/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.pdex.formulary.test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.ig.davinci.pdex.formulary.Formulary101ResourceProvider;
import net.sovrinhealth.fhir.ig.davinci.pdex.formulary.Formulary200ResourceProvider;
import net.sovrinhealth.fhir.registry.resource.FHIRRegistryResource;
import net.sovrinhealth.fhir.registry.spi.FHIRRegistryResourceProvider;

public class FormularyResourceProviderTest {
    @Test
    public void testGetFormulary101Resources() {
        FHIRRegistryResourceProvider provider = new Formulary101ResourceProvider();
        Collection<FHIRRegistryResource> registryResources = provider.getRegistryResources();
        assertNotNull(registryResources);
        assertTrue(!registryResources.isEmpty());
        for (FHIRRegistryResource fhirRegistryResource : registryResources) {
            assertNotNull(fhirRegistryResource.getResource());
        }
    }

    @Test
    public void testGetFormulary110Resources() {
        FHIRRegistryResourceProvider provider = new Formulary101ResourceProvider();
        Collection<FHIRRegistryResource> registryResources = provider.getRegistryResources();
        assertNotNull(registryResources);
        assertTrue(!registryResources.isEmpty());
        for (FHIRRegistryResource fhirRegistryResource : registryResources) {
            assertNotNull(fhirRegistryResource.getResource());
        }
    }
    
    /**
     * Test DaVinci Payer Data Exchange (PDex) US Drug Formulary, Release 2.0.0 - US Realm STU resource provider
     */
    @Test
    public void testGetFormulary200Resources() {
        FHIRRegistryResourceProvider provider = new Formulary200ResourceProvider();
        Collection<FHIRRegistryResource> registryResources = provider.getRegistryResources();
        assertNotNull(registryResources);
        assertTrue(!registryResources.isEmpty());
        for (FHIRRegistryResource fhirRegistryResource : registryResources) {
            assertNotNull(fhirRegistryResource.getResource());
        }
    }
}