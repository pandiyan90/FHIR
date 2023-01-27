/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.constraint.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countWarnings;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.hasErrors;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.hasWarnings;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.type.Boolean;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Date;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.HumanName;
import net.sovrinhealth.fhir.model.util.ModelSupport;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class PatientConstraintValidatorTest {
    @Test
    public void testPatientConstraintProvider() {
        List<Constraint> constraints = ModelSupport.getConstraints(Patient.class);
        Set<String> ids = constraints.stream()
            .map(constraint -> constraint.id())
            .collect(Collectors.toSet());
        assertTrue(ids.contains("patient-name-1"));
    }

    @Test
    public void testPatientConstraintValidator1() throws Exception {
        Patient patient = Patient.builder()
            .active(Boolean.TRUE)
            .birthDate(Date.of("1970-01-01"))
            .name(HumanName.builder()
                .given(string("John"))
                .family(string("Doe"))
                .build())
            .build();
        List<Issue> issues = FHIRValidator.validator().validate(patient);
        assertFalse(hasErrors(issues));
        assertTrue(hasWarnings(issues));
        assertEquals(countWarnings(issues), 1);
    }

    @Test
    public void testPatientConstraintValidator2() throws Exception {
        Patient patient = Patient.builder()
            .active(Boolean.TRUE)
            .birthDate(Date.of("1970-01-01"))
            .name(HumanName.builder()
                .given(string("John"))
                .build())
            .build();
        List<Issue> issues = FHIRValidator.validator().validate(patient);
        // dom-6: A resource should have narrative for robust management
        // patient-name-1: If Patient.name exists, then Patient.name.family SHOULD exist
        assertFalse(hasErrors(issues));
        assertTrue(hasWarnings(issues));
        assertEquals(countWarnings(issues), 2);
    }

    @Test
    public void testPatientConstraintValidatorForExtension() throws Exception {
        Patient patient = Patient.builder()
            .extension(Extension.builder()
                .url("http://hl7.org/fhir/StructureDefinition/patient-congregation")
                .value(Code.of("test"))
                .build())
            .build();
        List<Issue> issues = FHIRValidator.validator().validate(patient);
        assertTrue(hasErrors(issues));
        assertEquals(countErrors(issues), 1);
        assertTrue(hasWarnings(issues));
        assertEquals(countWarnings(issues), 1);
    }
}
