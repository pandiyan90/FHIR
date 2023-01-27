/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;
import static org.testng.Assert.assertEquals;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.examples.ExamplesUtil;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.Observation.Component;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class FailFastValidationTest {
    @Test
    public void testFailFastValidation1() throws Exception {
        Reader reader = ExamplesUtil.resourceReader("json/spec/observation-example-bloodpressure.json");

        Observation observation = FHIRParser.parser(Format.JSON).parse(reader);
        List<Component> component = new ArrayList<>(observation.getComponent());
        component.set(0, component.get(0).toBuilder()
            .value((Element)null)
            .build());
        observation = observation.toBuilder()
            .meta(observation.getMeta().toBuilder()
                .profile(Collections.singletonList(Canonical.of("http://hl7.org/fhir/StructureDefinition/bp")))
                .build())
            .component(component)
            .build();

        List<Issue> issues = FHIRValidator.validator().validate(observation);

        assertEquals(countErrors(issues), 2);
    }

    @Test
    public void testFailFastValidation2() throws Exception {
        Reader reader = ExamplesUtil.resourceReader("json/spec/observation-example-bloodpressure.json");

        Observation observation = FHIRParser.parser(Format.JSON).parse(reader);
        List<Component> component = new ArrayList<>(observation.getComponent());
        component.set(0, component.get(0).toBuilder()
            .value((Element)null)
            .build());
        observation = observation.toBuilder()
            .meta(observation.getMeta().toBuilder()
                .profile(Collections.singletonList(Canonical.of("http://hl7.org/fhir/StructureDefinition/bp")))
                .build())
            .component(component)
            .build();

        List<Issue> issues = FHIRValidator.validator(true).validate(observation);

        assertEquals(countErrors(issues), 1);
    }

    @Test
    public void testFailFastValidation3() throws Exception {
        Reader reader = ExamplesUtil.resourceReader("json/spec/observation-example-f001-glucose.json");

        Observation observation = FHIRParser.parser(Format.JSON).parse(reader);
        observation = observation.toBuilder()
            .dataAbsentReason(CodeableConcept.builder()
                .text(string("unknown"))
                .build())
            .build();

        List<Issue> issues = FHIRValidator.validator().validate(observation, "http://hl7.org/fhir/StructureDefinition/invalid");

        assertEquals(countErrors(issues), 2);
    }

    @Test
    public void testFailFastValidation4() throws Exception {
        Reader reader = ExamplesUtil.resourceReader("json/spec/observation-example-f001-glucose.json");

        Observation observation = FHIRParser.parser(Format.JSON).parse(reader);
        observation = observation.toBuilder()
            .dataAbsentReason(CodeableConcept.builder()
                .text(string("unknown"))
                .build())
            .build();

        List<Issue> issues = FHIRValidator.validator(true).validate(observation, "http://hl7.org/fhir/StructureDefinition/invalid");

        assertEquals(countErrors(issues), 1);
    }
}
