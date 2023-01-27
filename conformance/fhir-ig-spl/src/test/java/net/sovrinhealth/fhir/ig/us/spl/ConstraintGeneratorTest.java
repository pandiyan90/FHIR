/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.us.spl;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.compile;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.util.ModelSupport;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.registry.resource.FHIRRegistryResource;
import net.sovrinhealth.fhir.registry.spi.FHIRRegistryResourceProvider;

public class ConstraintGeneratorTest {
    @Test
    public static void testConstraintGenerator() throws Exception {
        FHIRRegistryResourceProvider provider = new ResourceProvider();
        for (FHIRRegistryResource registryResource : provider.getRegistryResources()) {
            if (StructureDefinition.class.equals(registryResource.getResourceType())) {
                String url = registryResource.getUrl();
                System.out.println(url);
                String kind = registryResource.getKind();
                if ("resource".equals(kind) || "complex-type".equals(kind)) {
                    Class<?> type = ModelSupport.isResourceType(registryResource.getType()) ? ModelSupport.getResourceType(registryResource.getType()) : Class.forName("net.sovrinhealth.fhir.model.type." + registryResource.getType());
                    for (Constraint constraint : ProfileSupport.getConstraints(url, type)) {
                        System.out.println("    " + constraint);
                        if (!Constraint.LOCATION_BASE.equals(constraint.location())) {
                            compile(constraint.location());
                        }
                        compile(constraint.expression());
                    }
                }
            }
        }
    }
}
