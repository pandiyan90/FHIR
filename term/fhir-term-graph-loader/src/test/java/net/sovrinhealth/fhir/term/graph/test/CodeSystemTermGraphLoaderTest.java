/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.graph.test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.CodeSystem.Concept;
import net.sovrinhealth.fhir.term.graph.FHIRTermGraph;
import net.sovrinhealth.fhir.term.graph.factory.FHIRTermGraphFactory;
import net.sovrinhealth.fhir.term.graph.loader.FHIRTermGraphLoader;
import net.sovrinhealth.fhir.term.graph.loader.impl.CodeSystemTermGraphLoader;
import net.sovrinhealth.fhir.term.graph.provider.GraphTermServiceProvider;
import net.sovrinhealth.fhir.term.spi.FHIRTermServiceProvider;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;

public class CodeSystemTermGraphLoaderTest {
    @Test
    public void testCodeSystemTermGraphLoader() throws Exception {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters().properties().setFileName("conf/janusgraph-berkeleyje-lucene.properties"));
        FHIRTermGraph graph = FHIRTermGraphFactory.open(builder.getConfiguration());
        graph.dropAllVertices();

        CodeSystem codeSystem = CodeSystemSupport.getCodeSystem("http://example.com/fhir/CodeSystem/test");
        FHIRTermGraphLoader loader = new CodeSystemTermGraphLoader(graph, codeSystem);
        loader.load();

        FHIRTermServiceProvider provider = new GraphTermServiceProvider(graph);

        Set<Concept> actual = new LinkedHashSet<>();
        for (Concept concept : provider.getConcepts(codeSystem)) {
            actual.add(provider.getConcept(codeSystem, concept.getCode()));
        }

        Set<Concept> expected = new LinkedHashSet<>();
        for (Concept concept : CodeSystemSupport.getConcepts(codeSystem)) {
            expected.add(concept.toBuilder()
                .concept(Collections.emptyList())
                .build());
        }

        Assert.assertEquals(actual, expected);

        graph.close();
    }
}