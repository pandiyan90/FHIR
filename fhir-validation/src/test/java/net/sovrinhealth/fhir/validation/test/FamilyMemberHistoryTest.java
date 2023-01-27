/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.FamilyMemberHistory;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.profile.ConstraintGenerator;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class FamilyMemberHistoryTest {
    @Test
    public void testFamilyMemberHistory() throws Exception {
        StructureDefinition profile = ProfileSupport.getProfile("http://example.com/fhir/StructureDefinition/my-family-member-history|0.1.0");
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();

        constraints.forEach(System.out::println);

        assertEquals(constraints.size(), 4);
        assertEquals(constraints.get(3).expression(), "condition.count() >= 1 and condition.all("
                + "code.exists() and code.all(memberOf('http://example.com/fhir/ValueSet/condition-value-set', 'extensible')) and "
                + "(outcome.exists() implies (outcome.exists() and outcome.all(memberOf('http://example.com/fhir/ValueSet/outcome-value-set', 'extensible'))))"
                + ")");

        InputStream in = FamilyMemberHistoryTest.class.getClassLoader().getResourceAsStream("JSON/familymemberhistory.json");
        FamilyMemberHistory familyMemberHistory = FHIRParser.parser(Format.JSON).parse(in);
        List<Issue> issues = FHIRValidator.validator().validate(familyMemberHistory);

        issues.forEach(System.out::println);

        assertEquals(issues.size(), 1);
        assertTrue(issues.get(0).getDetails().getText().getValue().startsWith("generated-my-family-member-history-4"));
    }
}
