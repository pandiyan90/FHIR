/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.test;

import static org.testng.Assert.assertEquals;

import java.util.Collection;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.CodeSystem.Concept;
import net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit;
import net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Insurance;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.CodeSystemContentMode;
import net.sovrinhealth.fhir.model.type.code.ExplanationOfBenefitStatus;
import net.sovrinhealth.fhir.model.type.code.PublicationStatus;
import net.sovrinhealth.fhir.model.type.code.RemittanceOutcome;
import net.sovrinhealth.fhir.model.type.code.Use;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;

public class RepeatFunctionTest {
    /**
     * This test shows that repeat works for multiple inputs and properly collects
     * each child named "concept" in a recursive fashion
     */
    @Test
    public void testRepeatFunction_CodeSystem() throws Exception {
        CodeSystem codeSystem = CodeSystem.builder()
                .status(PublicationStatus.DRAFT)
                .content(CodeSystemContentMode.EXAMPLE)
                .concept(Concept.builder()
                        .code(Code.of("a"))
                        .concept(Concept.builder()
                                .code(Code.of("b"))
                                .concept(Concept.builder()
                                        .code(Code.of("c"))
                                        .build())
                                .build())
                        .build())
                .concept(Concept.builder()
                        .code(Code.of("x"))
                        .concept(Concept.builder()
                                .code(Code.of("y"))
                                .concept(Concept.builder()
                                        .code(Code.of("z"))
                                        .build())
                                .build())
                        .build())
                .build();
        Collection<FHIRPathNode> result = FHIRPathEvaluator.evaluator()
                .evaluate(codeSystem, "repeat(concept).code");
        assertEquals(result.size(), 6);
    }

    /**
     * This test shows that {@code repeat(elementName)} behaves differently from {@code descendants().select(elementName)}
     */
    @Test
    public void testRepeatFunction_EoB() throws Exception {
        // I chose EoB because I knew it had a lot of elements named "type" at various levels in the structure
        ExplanationOfBenefit eob = ExplanationOfBenefit.builder()
                .status(ExplanationOfBenefitStatus.DRAFT)
                .type(CodeableConcept.builder().text("test").build())
                .use(Use.CLAIM)
                .patient(Reference.builder().type(Uri.of("Patient")).build())
                .created(DateTime.now())
                .insurer(Reference.builder().type(Uri.of("Organization")).build())
                .provider(Reference.builder().type(Uri.of("PractitionerRole")).build())
                .outcome(RemittanceOutcome.PARTIAL)
                .insurance(Insurance.builder()
                        .focal(false)
                        .coverage(Reference.builder().display("test").build())
                        .build())
                .build();
        Collection<FHIRPathNode> result = FHIRPathEvaluator.evaluator().evaluate(eob, "ExplanationOfBenefit.repeat(type)");
        assertEquals(result.size(), 1);
    }
}
