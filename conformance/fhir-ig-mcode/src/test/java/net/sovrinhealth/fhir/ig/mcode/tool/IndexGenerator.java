/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.mcode.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.parser.exception.FHIRParserException;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.registry.util.Index;
import net.sovrinhealth.fhir.registry.util.Index.Entry;

public class IndexGenerator {
    public static void main(String[] args) throws Exception {
        Index index = new Index(1);
        File dir = new File("src/main/resources/hl7/fhir/us/mcode/package/");
        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                try (FileReader reader = new FileReader(file)) {
                    try {
                        Resource resource = FHIRParser.parser(Format.JSON).parse(reader);
                        index.add(Entry.entry(resource));
                    } catch (FHIRParserException e) {
                        System.err.println("Unable to add: " + file.getName() + " to the index due to exception: " + e.getMessage());
                    }
                }
            }
        }
        try (OutputStream out = new FileOutputStream("src/main/resources/hl7/fhir/us/mcode/package/.index.json")) {
            index.store(out);
        }
    }
}
