/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.us.core.test.v311;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.compile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.ig.us.core.USCore311ResourceProvider;
import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.util.ModelSupport;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.registry.resource.FHIRRegistryResource;
import net.sovrinhealth.fhir.registry.spi.FHIRRegistryResourceProvider;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class ProviderTest {
    @Test
    public static void testConstraintGenerator() throws Exception {
        FHIRRegistryResourceProvider provider = new USCore311ResourceProvider();
        for (FHIRRegistryResource registryResource : provider.getRegistryResources()) {
            if (StructureDefinition.class.equals(registryResource.getResourceType())) {
                String url = registryResource.getUrl() + "|3.1.1";
                System.out.println(url);
                Class<?> type = ModelSupport.isResourceType(registryResource.getType()) ? ModelSupport.getResourceType(registryResource.getType()) : Extension.class;
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

    @Test
    public void testRegistry() {
        StructureDefinition definition = FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/us/core/StructureDefinition/pediatric-bmi-for-age", StructureDefinition.class);
        Assert.assertNotNull(definition);
    }

    @Test
    public void testUSCoreResourceProvider() {
        FHIRRegistryResourceProvider provider = new USCore311ResourceProvider();
        assertEquals(provider.getRegistryResources().size(), 147);
    }

    @Test
    public void testValidateResources() throws Exception {
        FHIRRegistryResourceProvider provider = new USCore311ResourceProvider();

        List<Exception> exceptions = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();

        FHIRValidator validator = FHIRValidator.validator();

        for (FHIRRegistryResource registryResource : provider.getRegistryResources()) {
            try {
                Resource resource = registryResource.getResource();
                issues.addAll(validator.validate(resource));
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        assertEquals(exceptions.size(), 0);
        assertFalse(issues.stream().anyMatch(issue -> IssueSeverity.ERROR.equals(issue.getSeverity())));
    }
}