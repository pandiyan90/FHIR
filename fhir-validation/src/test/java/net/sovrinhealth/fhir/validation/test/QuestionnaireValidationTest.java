/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import java.io.Reader;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.examples.ExamplesUtil;
import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Questionnaire;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.profile.ConstraintGenerator;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class QuestionnaireValidationTest {
    @Test
    public void testQuestionnaireValidation() throws Exception {
        try (Reader reader = ExamplesUtil.resourceReader("json/spec/questionnaire-cqf-example.json")) {
            Questionnaire questionnaire = FHIRParser.parser(Format.JSON).parse(reader);

            StructureDefinition profile = ProfileSupport.getProfile(questionnaire.getMeta().getProfile().get(0).getValue());
            ConstraintGenerator generator = new ConstraintGenerator(profile);
            List<Constraint> constraints = generator.generate();
            constraints.forEach(System.out::println);

            List<Issue> issues = FHIRValidator.validator().validate(questionnaire);
            issues.forEach(System.out::println);

            Assert.assertEquals(issues.size(), 0);
        }
    }
}
