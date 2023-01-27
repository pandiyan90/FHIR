/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.mcode.test;

import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;

import java.io.InputStream;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class MCODEValidationTest {
    @Test
    public void testMCODEValidation1() throws Exception {
        try (InputStream in = MCODEValidationTest.class.getClassLoader().getResourceAsStream("JSON/mcode-cancer-disease-status-no-subject-asserted.json")) {
            Observation observation = FHIRParser.parser(Format.JSON).parse(in);
            List<Issue> issues = FHIRValidator.validator().validate(observation);
            issues.forEach(System.out::println);
            Assert.assertEquals(countErrors(issues), 1);
        }
    }

    @Test
    public void testMCODEValidation2() throws Exception {
        try (InputStream in = MCODEValidationTest.class.getClassLoader().getResourceAsStream("JSON/mcode-cancer-disease-status-no-subject-not-asserted.json")) {
            Observation observation = FHIRParser.parser(Format.JSON).parse(in);
            List<Issue> issues = FHIRValidator.validator().validate(observation);
            issues.forEach(System.out::println);
            Assert.assertEquals(countErrors(issues), 0);
        }
    }

    @Test
    public void testMCODEValidation3() throws Exception {
        try (InputStream in = MCODEValidationTest.class.getClassLoader().getResourceAsStream("XML/mcode-cancer-disease-status-no-subject-asserted.xml")) {
            Observation observation = FHIRParser.parser(Format.XML).parse(in);
            List<Issue> issues = FHIRValidator.validator().validate(observation);
            issues.forEach(System.out::println);
            Assert.assertEquals(countErrors(issues), 1);
        }
    }

    @Test
    public void testMCODEValidation4() throws Exception {
        try (InputStream in = MCODEValidationTest.class.getClassLoader().getResourceAsStream("XML/mcode-cancer-disease-status-no-subject-not-asserted.xml")) {
            Observation observation = FHIRParser.parser(Format.XML).parse(in);
            List<Issue> issues = FHIRValidator.validator().validate(observation);
            issues.forEach(System.out::println);
            Assert.assertEquals(countErrors(issues), 0);
        }
    }
}
