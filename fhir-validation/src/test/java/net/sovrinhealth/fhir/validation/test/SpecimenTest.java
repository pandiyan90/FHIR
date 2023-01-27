/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import static org.testng.Assert.fail;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.resource.Specimen;
import net.sovrinhealth.fhir.model.resource.Specimen.Collection;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.Duration;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.Xhtml;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.NarrativeStatus;
import net.sovrinhealth.fhir.validation.FHIRValidator;

/**
 * Tests FHIR spec validation of PlanDefinition.
 */
public class SpecimenTest {

    @Test
    public void testValid() {
        Specimen s = buildTestSpecimen();
        checkForIssuesWithValidation(s, 0);
    }

    @Test
    public void testValidWithCollectionFastingStatusCodeableConcept() {
        Specimen s = buildTestSpecimen();
        s = s.toBuilder().collection(Collection.builder().fastingStatus(CodeableConcept.builder().coding(Coding.builder().system(Uri.of("http://terminology.hl7.org/CodeSystem/v2-0916")).code(Code.of("F")).build()).build()).build()).build();
        checkForIssuesWithValidation(s, 0);
    }

    @Test
    public void testWarningWithCollectionFastingStatusCodeableConcept() {
        Specimen s = buildTestSpecimen();
        s = s.toBuilder().collection(Collection.builder().fastingStatus(CodeableConcept.builder().coding(Coding.builder().system(Uri.of("http://terminology.hl7.org/CodeSystem/v2-0916")).code(Code.of("INVALID")).build()).build()).build()).build();
        checkForIssuesWithValidation(s, 1);
    }

    @Test
    public void testValidWithCollectionFastingStatusDuration() {
        Specimen s = buildTestSpecimen();
        s = s.toBuilder().collection(Collection.builder().fastingStatus(Duration.builder().system(Uri.of("http://unitsofmeasure.org")).code(Code.of("a")).value(Decimal.of(1)).build()).build()).build();
        checkForIssuesWithValidation(s, 0);
    }

    @Test
    public void testWarningWithCollectionFastingStatusDuration() {
        Specimen s = buildTestSpecimen();
        s = s.toBuilder().collection(Collection.builder().fastingStatus(Duration.builder().system(Uri.of("http://unitsofmeasure.org")).code(Code.of("INVALID")).value(Decimal.of(1)).build()).build()).build();
        checkForIssuesWithValidation(s, 2);
    }

    /**
     * Builds a valid Specimen.
     * 
     * @return a valid Specimen
     */
    public static Specimen buildTestSpecimen() {
        Meta meta = Meta.builder().versionId(Id.of("1")).lastUpdated(Instant.now(ZoneOffset.UTC)).build();
        return Specimen.builder().meta(meta).text(Narrative.builder().div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">loaded from the datastore</div>")).status(NarrativeStatus.GENERATED).build()).build();
    }

    /**
     * Checks for validation issues.
     * 
     * @param resource
     *            the resource
     * @param numWarningsExpected
     *            number of expected validation warnings
     */
    public static void checkForIssuesWithValidation(Resource resource, int numWarningsExpected) {

        List<Issue> issues = Collections.emptyList();
        try {
            issues = FHIRValidator.validator().validate(resource);
        } catch (Exception e) {
            fail("Unable to validate the resource");
        }

        if (!issues.isEmpty()) {
            System.out.println("Printing Issue with Validation");
            int nonWarning = 0;
            int allOtherIssues = 0;
            for (Issue issue : issues) {
                if (IssueSeverity.ERROR.getValue().compareTo(issue.getSeverity().getValue()) == 0
                        || IssueSeverity.FATAL.getValue().compareTo(issue.getSeverity().getValue()) == 0) {
                    nonWarning++;
                } else {
                    allOtherIssues++;
                }
                System.out.println("level: " + issue.getSeverity().getValue() + ", details: "
                        + issue.getDetails().getText().getValue() + ", expression: "
                        + issue.getExpression().get(0).getValue());
            }

            System.out.println("count = [" + issues.size() + "]");

            if (nonWarning > 0) {
                fail("Fail on Errors " + nonWarning);
            }

            if (numWarningsExpected != allOtherIssues) {
                fail("Fail on Warnings " + allOtherIssues);
            }
        } else {
            System.out.println("Passed with no issues in validation");
        }
    }

}
