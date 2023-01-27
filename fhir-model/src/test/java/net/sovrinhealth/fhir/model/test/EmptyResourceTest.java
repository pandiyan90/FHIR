/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.test;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Patient.Link;
import net.sovrinhealth.fhir.model.type.Narrative;

public class EmptyResourceTest {
    @Test
    public void testEmptyPatient() {
        Patient.builder().build();
    }

    @Test
    public void testPatientWithEmptyNarrative() {
        Patient.builder().text(Narrative.EMPTY).build();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testInvalidPatient() {
        Patient.builder().link((Link)null).build();
    }
}
