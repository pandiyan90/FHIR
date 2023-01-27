/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.test;

import java.io.InputStream;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.ActivityDefinition;

public class FHIRParserGeneratorTest {
    public static void main(String[] args) throws Exception {
        try (InputStream in = FHIRParserGeneratorTest.class.getClassLoader().getResourceAsStream("JSON/activitydefinition-administer-zika-virus-exposure-assessment.json")) {
            ActivityDefinition activityDefinition = FHIRParser.parser(Format.JSON).parse(in);
            FHIRGenerator.generator(Format.JSON, true).generate(activityDefinition, System.out);
        }
    }
}
