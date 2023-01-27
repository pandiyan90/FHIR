/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.hrex.test.v100;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.compile;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.ig.davinci.hrex.HREX100ResourceProvider;
import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.util.ModelSupport;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.registry.resource.FHIRRegistryResource;
import net.sovrinhealth.fhir.registry.spi.FHIRRegistryResourceProvider;

public class ProviderTest {

    @Test
    public void testHREXResourceProvider() {
        FHIRRegistryResourceProvider provider = new HREX100ResourceProvider();
        Collection<FHIRRegistryResource> registryResources = provider.getRegistryResources();
        assertNotNull(registryResources);
        assertTrue(!registryResources.isEmpty());
        for (FHIRRegistryResource fhirRegistryResource : registryResources) {
            assertNotNull(fhirRegistryResource.getResource());
        }
    }

    @Test
    public static void testConstraintGenerator() throws Exception {
        FHIRRegistryResourceProvider provider = new HREX100ResourceProvider();
        for (FHIRRegistryResource registryResource : provider.getRegistryResources()) {
            if (StructureDefinition.class.equals(registryResource.getResourceType())) {
                String url = registryResource.getUrl();
                System.out.println(url);
                Class<?> type =
                        ModelSupport.isResourceType(registryResource.getType()) ? ModelSupport.getResourceType(registryResource.getType()) : Extension.class;
                for (Constraint constraint : ProfileSupport.getConstraints(url, type)) {
                    System.out.println("    " + constraint);
                    if (!Constraint.LOCATION_BASE.equals(constraint.location())) {
                        compile(constraint.location());
                    }
                    compile(constraint.expression());
                }
                System.out.println("--- done");
            }
        }
    }

    @Test
    public void testRegistry() {
        StructureDefinition definition =
                FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/us/davinci-hrex/StructureDefinition/hrex-parameters-member-match-out", StructureDefinition.class);
        Assert.assertNotNull(definition);
    }
}