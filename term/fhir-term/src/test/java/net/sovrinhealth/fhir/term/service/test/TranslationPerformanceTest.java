/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.service.test;

import static net.sovrinhealth.fhir.term.util.ConceptMapSupport.getConceptMap;

import net.sovrinhealth.fhir.model.resource.ConceptMap;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.term.service.FHIRTermService;

public class TranslationPerformanceTest {
    public static final int ITERATIONS = 10000000;

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        for (int i = 0; i < ITERATIONS; i++) {
            ConceptMap conceptMap = getConceptMap("http://example.com/fhir/ConceptMap/snomed-ucum");

            Coding coding = Coding.builder()
                    .system(Uri.of("http://snomed.info/sct"))
                    .code(Code.of("258773002"))
                    .build();

            FHIRTermService.getInstance().translate(conceptMap, coding);
        }

        long end = System.currentTimeMillis();

        System.out.println("Processing time for " + ITERATIONS + " iterations: " + (end - start) + " milliseconds");
    }
}
