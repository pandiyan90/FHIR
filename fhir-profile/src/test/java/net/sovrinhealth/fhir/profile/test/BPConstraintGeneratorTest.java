/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.profile.test;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.profile.ConstraintGenerator;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class BPConstraintGeneratorTest {
    private static final boolean DEBUG = false;

    @BeforeClass
    public void before() {
        FHIRRegistry.getInstance();
        FHIRRegistry.init();
    }

    @Test
    public void generateConstraints() throws Exception {
        StructureDefinition profile = ProfileSupport.getProfile("http://hl7.org/fhir/StructureDefinition/bp");
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 9);
        for (Constraint constraint : constraints) {
            if (DEBUG) {
                System.out.println(constraint.id() + "\t" + constraint.expression());
            }
        }
    }
}
