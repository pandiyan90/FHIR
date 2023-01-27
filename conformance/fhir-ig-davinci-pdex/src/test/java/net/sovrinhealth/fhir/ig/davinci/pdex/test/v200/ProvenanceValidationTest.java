/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.pdex.test.v200;

import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countInformation;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countWarnings;

import java.io.InputStream;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Provenance;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class ProvenanceValidationTest {
    @Test
    public void testProvenanceValidation1() throws Exception {

        try (InputStream in = ProvenanceValidationTest.class.getClassLoader().getResourceAsStream("JSON/200/Provenance-Practitioner.json")) {
            Provenance provenance = FHIRParser.parser(Format.JSON).parse(in);
            List<Issue> issues = FHIRValidator.validator().validate(provenance);
            issues.forEach(System.out::println);
            Assert.assertEquals(countErrors(issues), 0);
            Assert.assertEquals(countWarnings(issues), 0);
            Assert.assertEquals(countInformation(issues), 1);
        }
    }
}