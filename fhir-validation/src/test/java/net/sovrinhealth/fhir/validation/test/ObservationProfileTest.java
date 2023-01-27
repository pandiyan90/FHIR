/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;


import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.compile;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.profile.ConstraintGenerator;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class ObservationProfileTest {
    @Test
    public void testObservationProfile() throws Exception {
        StructureDefinition profile = FHIRRegistry.getInstance().getResource("http://example.com/fhir/StructureDefinition/my-observation",  StructureDefinition.class);
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        constraints.forEach(System.out::println);
        constraints.forEach(constraint -> compile(constraint.expression()));
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "component.exists().not()");
    }
}
