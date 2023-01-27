/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.test;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.examples.ExamplesUtil;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.model.visitor.PathAwareVisitor;
import net.sovrinhealth.fhir.path.FHIRPathElementNode;
import net.sovrinhealth.fhir.path.FHIRPathResourceNode;
import net.sovrinhealth.fhir.path.FHIRPathTree;
import net.sovrinhealth.fhir.path.visitor.FHIRPathDefaultNodeVisitor;

public class NodeVisitorTest {
    @Test
    public void testNodeVisitor() throws Exception {
        try (BufferedReader reader = new BufferedReader(ExamplesUtil.resourceReader("json/spec/patient-example.json"))) {
            Patient patient = FHIRParser.parser(Format.JSON).parse(reader);
                        
            List<String> paths = new ArrayList<>();
            patient.accept(new PathAwareVisitor() {
                @Override
                public void doVisitStart(String elementName, int elementIndex, Resource resource) {
                    paths.add(getPath());
                }
                
                @Override
                public void doVisitStart(String elementName, int elementIndex, Element element) {
                    paths.add(getPath());
                }
            });
                        
            List<String> treePaths = new ArrayList<>();
            FHIRPathTree tree = FHIRPathTree.tree(patient);
            tree.getRoot().accept(new FHIRPathDefaultNodeVisitor() {
                @Override
                public void doVisit(FHIRPathElementNode node) {
                    treePaths.add(node.path());
                }

                @Override
                public void doVisit(FHIRPathResourceNode node) {
                    treePaths.add(node.path());
                }
            });
            
            Assert.assertEquals(paths, treePaths);
            Assert.assertEquals(treePaths, paths);
        }
    }
}