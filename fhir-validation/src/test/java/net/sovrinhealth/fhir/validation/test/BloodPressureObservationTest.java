/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import static net.sovrinhealth.fhir.model.type.String.string;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.Observation.Component;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Quantity;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.ObservationStatus;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class BloodPressureObservationTest {
    @Test
    public static void testBloodPressureObservation() throws Exception {
        // build a blood pressure observation
        Observation bloodPressureObservation = Observation.builder()
            // resource metadata
            .meta(Meta.builder()
                // asssert conformance to the blood pressure profile
                .profile(Canonical.of("http://hl7.org/fhir/StructureDefinition/bp"))
                .build())

            // observation status
            .status(ObservationStatus.FINAL)

            // observation subject (Patient)
            .subject(Reference.builder()
                .reference(string("Patient/1234"))
                .build())

            // observation effective time
            .effective(DateTime.builder()
                .value("2019-01-01")
                .build())

            // observation category (vital signs)
            .category(CodeableConcept.builder()
                .coding(Coding.builder()
                    .system(Uri.of("http://terminology.hl7.org/CodeSystem/observation-category"))
                    .code(Code.of("vital-signs"))
                    .build())
                .build())

            // observation code (blood pressure)
            .code(CodeableConcept.builder()
                .coding(Coding.builder()
                    .system(Uri.of("http://loinc.org"))
                    .code(Code.of("85354-9"))
                    .build())
                .build())

            // observation component (systolic blood pressure)
            .component(Component.builder()
                .code(CodeableConcept.builder()
                    .coding(Coding.builder()
                        .system(Uri.of("http://loinc.org"))
//                      .code(Code.of("8480-6"))
                        .display(string("Systolic Blood Pressure"))
                        .build())
                    .build())
                .value(Quantity.builder()
                    .value(Decimal.of(120))
                    .system(Uri.of("http://unitsofmeasure.org"))
                    .code(Code.of("mm[Hg]"))
                    .unit(string("mm[Hg]"))
                    .build())
                .build())

            // observation component (diastolic blood pressure)
            .component(Component.builder()
                .code(CodeableConcept.builder()
                    .coding(Coding.builder()
                        .system(Uri.of("http://loinc.org"))
                        .code(Code.of("8462-4"))
                        .display(string("Diastolic Blood Pressure"))
                        .build())
                    .build())
                .value(Quantity.builder()
                    .value(Decimal.of(80))
                    .system(Uri.of("http://unitsofmeasure.org"))
                    .code(Code.of("mm[Hg]"))
                    .unit(string("mm[Hg]"))
                    .build())
                .build())
            .build();

        // print the blood pressure observation to the console
        FHIRGenerator.generator(Format.JSON, true).generate(bloodPressureObservation, System.out);
        System.out.println("");

        // validate the blood pressure observation in debug mode and print issues to console
        List<Issue> issues = FHIRValidator.validator().validate(bloodPressureObservation);
        issues.forEach(System.out::println);
        Assert.assertEquals(issues.size(), 3);
        Assert.assertTrue(issues.get(0).getDetails().getText().getValue().startsWith("generated-bp-8"));
        Assert.assertTrue(issues.get(1).getDetails().getText().getValue().startsWith("dom-6"));
        Assert.assertTrue(issues.get(2).getSeverity().equals(IssueSeverity.INFORMATION));

        System.out.println("");
    }
}