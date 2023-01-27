/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.constraint.test;

import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.util.ModelSupport;

public class ModelConstraintProviderTest {
    @Test
    public void testModelConstraintProvider() {
        List<Constraint> constraints = ModelSupport.getConstraints(Patient.class);
        Set<String> ids = constraints.stream()
                .map(constraint -> constraint.id())
                .collect(Collectors.toSet());
        //assertTrue(!ids.contains("pat-1"));
        //assertTrue(ids.contains("added-pat-1"));
    }
}
