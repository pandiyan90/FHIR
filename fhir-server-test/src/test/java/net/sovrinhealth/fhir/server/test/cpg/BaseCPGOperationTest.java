/*
 * (C) Copyright IBM Corp. 2021
 * asdf
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test.cpg;

import java.util.Collection;
import java.util.stream.Collectors;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

import net.sovrinhealth.fhir.model.resource.CapabilityStatement;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.server.test.FHIRServerTestBase;

public class BaseCPGOperationTest extends FHIRServerTestBase {

    @BeforeClass
    public void checkForOperationSupport() throws Exception {
        CapabilityStatement conf = retrieveConformanceStatement();
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        EvaluationContext evaluationContext = new EvaluationContext(conf);
        Collection<FHIRPathNode> tmpResults = evaluator.evaluate(evaluationContext, "rest.resource.where(type = 'Library').operation.name");
        Collection<String> listOfOperations = tmpResults.stream().map(x -> x.getValue().asStringValue().string()).collect(Collectors.toList());
        if( !listOfOperations.contains("evaluate") ) {
            throw new SkipException("CPG Operations are not enabled");
        }
    }
}
