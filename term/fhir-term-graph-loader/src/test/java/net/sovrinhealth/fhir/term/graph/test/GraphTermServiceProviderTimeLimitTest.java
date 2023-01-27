/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.graph.test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

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
import net.sovrinhealth.fhir.term.service.exception.FHIRTermServiceException;
import net.sovrinhealth.fhir.term.spi.FHIRTermServiceProvider;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;

public class GraphTermServiceProviderTimeLimitTest {
    // This test was acting flaky on the GitHub actions windows-2019 environment (and only this environment)
    // and so its been temporarily disabled.  Please enable and re-test in this environment in the future.
    @Test(enabled = false)
    public void testGraphTermServiceProviderTimeLimit() throws Exception {
        FHIRTermGraph graph = null;
        try {
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(new Parameters().properties().setFileName("conf/janusgraph-berkeleyje-lucene.properties"));
            graph = FHIRTermGraphFactory.open(builder.getConfiguration());
            graph.dropAllVertices();

            CodeSystem codeSystem = CodeSystemSupport.getCodeSystem("http://example.com/fhir/CodeSystem/test");
            FHIRTermGraphLoader loader = new CodeSystemTermGraphLoader(graph, codeSystem);
            loader.load();

            FHIRTermServiceProvider provider = new GraphTermServiceProvider(graph, 1);

            Set<Concept> concepts = provider.getConcepts(codeSystem);
            assertTrue(concepts.size() > 0, "If we get a response, the returned concepts should be non-empty");

            fail("We expected the query to time out but it didn't.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FHIRTermServiceException);
            Assert.assertEquals(e.getMessage(), "Graph traversal timed out");
        } finally {
            if (graph != null) {
                graph.close();
            }
        }
    }
}