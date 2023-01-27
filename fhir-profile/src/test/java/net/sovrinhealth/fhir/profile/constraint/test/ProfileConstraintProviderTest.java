/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.profile.constraint.test;

import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class ProfileConstraintProviderTest {
    @BeforeClass
    public void before() {
        FHIRRegistry.getInstance();
        FHIRRegistry.init();
    }

    @Test
    public void testProfileConstraintProvider() {
        List<Constraint> constraints = ProfileSupport.getConstraints("http://hl7.org/fhir/StructureDefinition/bp", Observation.class);
        Set<String> ids = constraints.stream()
                .map(constraint -> constraint.id())
                .collect(Collectors.toSet());
        assertTrue(!ids.contains("vs-1"));
        assertTrue(ids.contains("added-vs-4"));
    }
}
