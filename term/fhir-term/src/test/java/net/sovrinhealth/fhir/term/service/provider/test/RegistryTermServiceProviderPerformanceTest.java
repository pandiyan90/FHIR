/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.service.provider.test;

import net.sovrinhealth.fhir.cache.CachingProxy;
import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.term.service.provider.RegistryTermServiceProvider;
import net.sovrinhealth.fhir.term.spi.FHIRTermServiceProvider;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;

public class RegistryTermServiceProviderPerformanceTest {
    public static final int MAX_ITERATIONS = 1000000;

    public static void main(String[] args) {
        FHIRTermServiceProvider provider = new RegistryTermServiceProvider();

        CodeSystem codeSystem = CodeSystemSupport.getCodeSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
        Code code = Code.of("STORE");

        long start = System.currentTimeMillis();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            provider.getConcept(codeSystem, code);
        }

        long end = System.currentTimeMillis();

        System.out.println("Running time: " + (end - start) + " milliseconds");

        provider = CachingProxy.newInstance(FHIRTermServiceProvider.class, new RegistryTermServiceProvider());

        start = System.currentTimeMillis();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            provider.getConcept(codeSystem, code);
        }

        end = System.currentTimeMillis();

        System.out.println("Running time: " + (end - start) + " milliseconds");
    }
}
