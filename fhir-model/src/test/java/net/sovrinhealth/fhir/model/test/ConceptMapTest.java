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
import net.sovrinhealth.fhir.model.resource.ConceptMap;

public class ConceptMapTest {
    public static void main(String[] args) throws Exception {
        try (InputStream in = ConceptMapTest.class.getClassLoader().getResourceAsStream("XML/conceptmap-example.xml")) {
            ConceptMap conceptMap = FHIRParser.parser(Format.XML).parse(in);
            FHIRGenerator.generator(Format.XML, true).generate(conceptMap, System.out);
        }
    }
}
