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
import net.sovrinhealth.fhir.model.resource.Binary;

public class BinaryTest {
    public static void main(String[] args) throws Exception {
        // JSON
        try (InputStream in = BinaryTest.class.getClassLoader().getResourceAsStream("JSON/binary-example.json")) {
            Binary binary = FHIRParser.parser(Format.JSON).parse(in);
            FHIRGenerator.generator(Format.JSON, true).generate(binary, System.out);
        }
        
        // XML
        try (InputStream in = BinaryTest.class.getClassLoader().getResourceAsStream("XML/binary-example.xml")) {
            Binary binary = FHIRParser.parser(Format.XML).parse(in);
            FHIRGenerator.generator(Format.XML, true).generate(binary, System.out);
        }
    }
}
