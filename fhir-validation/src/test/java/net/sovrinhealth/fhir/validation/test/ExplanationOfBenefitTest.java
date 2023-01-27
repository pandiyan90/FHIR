/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import java.io.InputStream;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry;
import net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.type.code.BundleType;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class ExplanationOfBenefitTest {
    @Test
    public static void testExplanationOfBenefit() throws Exception {
        try (InputStream in = ExplanationOfBenefitTest.class.getClassLoader().getResourceAsStream("JSON/explanationofbenefit.json")) {
            ExplanationOfBenefit explanationOfBenefit = FHIRParser.parser(Format.JSON).parse(in);
            FHIRGenerator.generator(Format.JSON, true).generate(explanationOfBenefit, System.out);
            FHIRValidator.validator().validate(explanationOfBenefit).forEach(System.out::println);
            Bundle bundle = Bundle.builder()
                .type(BundleType.COLLECTION)
                .entry(Entry.builder()
                    .resource(explanationOfBenefit)
                    .build())
                .build();
            List<Issue> issues = FHIRValidator.validator().validate(bundle);
            Assert.assertEquals(issues.size(), 3);
            for (Issue issue : issues) {
                Assert.assertNotEquals(issue.getSeverity(), IssueSeverity.ERROR);
            }
        }
    }
}