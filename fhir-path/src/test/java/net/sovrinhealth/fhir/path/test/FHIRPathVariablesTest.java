/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.test;

import static org.testng.Assert.assertEquals;

import java.util.Collection;

import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.type.HumanName;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.FHIRPathStringValue;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.path.util.FHIRPathUtil;
import org.testng.annotations.Test;

/**
 * Test FHIRPath expressions that use variables / externalConstants like %resource
 */
public class FHIRPathVariablesTest {
    Patient patient = Patient.builder()
            .id("test")
            .name(HumanName.builder()
                    .given("Lee")
                    .build())
            .build();

    @Test
    public void testRepeatFunction_CodeSystem() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Collection<FHIRPathNode> initialContext = evaluator.evaluate(patient, "Patient.name");

        EvaluationContext evaluationContext = new EvaluationContext(patient);
        Collection<FHIRPathNode> result = evaluator.evaluate(evaluationContext, "%resource.id", initialContext);

        assertEquals("test", FHIRPathUtil.getSingleton(result, FHIRPathStringValue.class).string());
    }
}
