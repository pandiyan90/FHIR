/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.model.util.ModelSupport.FHIR_STRING;
import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.getResourceNode;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.isResourceNode;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.isSingleton;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.singleton;
import static net.sovrinhealth.fhir.term.service.util.FHIRTermServiceUtil.getParameter;
import static net.sovrinhealth.fhir.term.service.util.FHIRTermServiceUtil.getParameterValue;
import static net.sovrinhealth.fhir.term.service.util.FHIRTermServiceUtil.getPartValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Parameters.Parameter;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.model.type.Boolean;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.ConceptMapEquivalence;
import net.sovrinhealth.fhir.model.type.code.ConceptSubsumptionOutcome;
import net.sovrinhealth.fhir.path.FHIRPathElementNode;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class FHIRPathTermFunctionTest {
    @Test
    public void testExpand() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Collection<FHIRPathNode> result = evaluator.evaluate("%terminologies.expand('http://example.com/fhir/ValueSet/vs1')");
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(ValueSet.class));
        assertNotNull(getResourceNode(result).resource().as(ValueSet.class).getExpansion());
        assertEquals(getResourceNode(result).resource().as(ValueSet.class).getExpansion().getContains().size(), 3);
    }

    @Test
    public void testLookup() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding coding = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs1"))
                .version(string("1.0.0"))
                .code(Code.of("a"))
                .build();

        Collection<FHIRPathNode> result = evaluator.evaluate(coding, "%terminologies.lookup(%context)");
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(Parameters.class));

        Parameters parameters = getResourceNode(result).resource().as(Parameters.class);
        assertEquals(getParameterValue(parameters, "name", FHIR_STRING), string("Code System 5"));
        assertEquals(getParameterValue(parameters, "version", FHIR_STRING), string("1.0.0"));
        assertEquals(getParameterValue(parameters, "display", FHIR_STRING), string("concept a"));
    }

    @Test
    public void testSubsumedBy1() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding codingA = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("u"))
                .build();

        Coding codingB = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Collection<FHIRPathNode> initialContext = Arrays.asList(FHIRPathElementNode.elementNode(codingA), FHIRPathElementNode.elementNode(codingB));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%context[0].subsumedBy(%context[1])", initialContext);
        assertEquals(result, SINGLETON_TRUE);
    }

    @Test
    public void testSubsumedBy2() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding codingA = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Coding codingB = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("u"))
                .build();

        Collection<FHIRPathNode> initialContext = Arrays.asList(FHIRPathElementNode.elementNode(codingA), FHIRPathElementNode.elementNode(codingB));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%context[0].subsumedBy(%context[1])", initialContext);
        assertEquals(result, SINGLETON_FALSE);
    }

    @Test
    public void testSubsumedBy3() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding codingA = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Coding codingB = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Collection<FHIRPathNode> initialContext = Arrays.asList(FHIRPathElementNode.elementNode(codingA), FHIRPathElementNode.elementNode(codingB));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%context[0].subsumedBy(%context[1])", initialContext);
        assertEquals(result, SINGLETON_TRUE);
    }

    @Test
    public void testSubsumes1() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding codingA = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Coding codingB = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("u"))
                .build();

        Collection<FHIRPathNode> initialContext = Arrays.asList(FHIRPathElementNode.elementNode(codingA), FHIRPathElementNode.elementNode(codingB));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%context[0].subsumes(%context[1])", initialContext);
        assertEquals(result, SINGLETON_TRUE);

        result = evaluator.evaluate(new EvaluationContext(), "%terminologies.subsumes(%context[0], %context[1])", initialContext);
        assertEquals(result, singleton(FHIRPathElementNode.elementNode(ConceptSubsumptionOutcome.SUBSUMES)));
    }

    @Test
    public void testSubsumes2() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding codingA = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("u"))
                .build();

        Coding codingB = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Collection<FHIRPathNode> initialContext = Arrays.asList(FHIRPathElementNode.elementNode(codingA), FHIRPathElementNode.elementNode(codingB));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%context[0].subsumes(%context[1])", initialContext);
        assertEquals(result, SINGLETON_FALSE);

        result = evaluator.evaluate(new EvaluationContext(), "%terminologies.subsumes(%context[0], %context[1])", initialContext);
        assertEquals(result, singleton(FHIRPathElementNode.elementNode(ConceptSubsumptionOutcome.SUBSUMED_BY)));
    }

    @Test
    public void testSubsumes3() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding codingA = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Coding codingB = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("t"))
                .build();

        Collection<FHIRPathNode> initialContext = Arrays.asList(FHIRPathElementNode.elementNode(codingA), FHIRPathElementNode.elementNode(codingB));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%context[0].subsumes(%context[1])", initialContext);
        assertEquals(result, SINGLETON_TRUE);

        result = evaluator.evaluate(new EvaluationContext(), "%terminologies.subsumes(%context[0], %context[1])", initialContext);
        assertEquals(result, singleton(FHIRPathElementNode.elementNode(ConceptSubsumptionOutcome.EQUIVALENT)));
    }

    @Test
    public void testTranslate1() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding coding = Coding.builder()
                .system(Uri.of("http://snomed.info/sct"))
                .code(Code.of("258672001"))
                .build();

        Collection<FHIRPathNode> initialContext = singleton(FHIRPathElementNode.elementNode(coding));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%terminologies.translate('http://example.com/fhir/ConceptMap/snomed-ucum', %context)", initialContext);
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(Parameters.class));

        Parameters parameters = getResourceNode(result).resource().as(Parameters.class);
        assertEquals(getParameterValue(parameters, "result", Boolean.class), Boolean.TRUE);

        Parameter matchParameter = getParameter(parameters, "match");
        assertNotNull(matchParameter);

        ConceptMapEquivalence equivalence = getPartValue(matchParameter, "equivalence", ConceptMapEquivalence.class);
        assertEquals(equivalence, ConceptMapEquivalence.EQUIVALENT);

        Coding expected = Coding.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .version(string("2015"))
                .code(Code.of("cm"))
                .build();

        Coding actual = getPartValue(matchParameter, "concept", Coding.class);
        assertEquals(actual, expected);
    }

    @Test
    public void testTranslate2() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding coding = Coding.builder()
                .system(Uri.of("http://snomed.info/sct"))
                .code(Code.of("258773002"))
                .build();

        Collection<FHIRPathNode> initialContext = singleton(FHIRPathElementNode.elementNode(coding));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%terminologies.translate('http://example.com/fhir/ConceptMap/snomed-ucum', %context)", initialContext);
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(Parameters.class));

        Parameters parameters = getResourceNode(result).resource().as(Parameters.class);
        assertEquals(getParameterValue(parameters, "result", Boolean.class), Boolean.TRUE);

        Parameter matchParameter = getParameter(parameters, "match");
        assertNotNull(matchParameter);

        ConceptMapEquivalence equivalence = getPartValue(matchParameter, "equivalence", ConceptMapEquivalence.class);
        assertEquals(equivalence, ConceptMapEquivalence.EQUIVALENT);

        Coding expected = Coding.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .version(string("2015"))
                .code(Code.of("mL"))
                .build();

        Coding actual = getPartValue(matchParameter, "concept", Coding.class);
        assertEquals(actual, expected);
    }

    @Test
    public void testValidateCS1() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding coding = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("m"))
                .build();

        Collection<FHIRPathNode> initialContext = singleton(FHIRPathElementNode.elementNode(coding));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%terminologies.validateCS('http://example.com/fhir/CodeSystem/cs5', %context)", initialContext);
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(Parameters.class));

        Parameters parameters = getResourceNode(result).resource().as(Parameters.class);
        assertEquals(getParameterValue(parameters, "result", Boolean.class), Boolean.TRUE);
    }

    @Test
    public void testValidateCS2() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding coding = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs5"))
                .version(string("1.0.0"))
                .code(Code.of("x"))
                .build();

        Collection<FHIRPathNode> initialContext = singleton(FHIRPathElementNode.elementNode(coding));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%terminologies.validateCS('http://example.com/fhir/CodeSystem/cs5', %context)", initialContext);
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(Parameters.class));

        Parameters parameters = getResourceNode(result).resource().as(Parameters.class);
        assertEquals(getParameterValue(parameters, "result", Boolean.class), Boolean.FALSE);
    }

    @Test
    public void testValidateVS1() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding coding = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs1"))
                .version(string("1.0.0"))
                .code(Code.of("a"))
                .build();

        Collection<FHIRPathNode> initialContext = singleton(FHIRPathElementNode.elementNode(coding));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%terminologies.validateVS('http://example.com/fhir/ValueSet/vs1', %context)", initialContext);
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(Parameters.class));

        Parameters parameters = getResourceNode(result).resource().as(Parameters.class);
        assertEquals(getParameterValue(parameters, "result", Boolean.class), Boolean.TRUE);
    }

    @Test
    public void testValidateVS2() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Coding coding = Coding.builder()
                .system(Uri.of("http://example.com/fhir/CodeSystem/cs1"))
                .version(string("1.0.0"))
                .code(Code.of("x"))
                .build();

        Collection<FHIRPathNode> initialContext = singleton(FHIRPathElementNode.elementNode(coding));
        Collection<FHIRPathNode> result = evaluator.evaluate(new EvaluationContext(), "%terminologies.validateVS('http://example.com/fhir/ValueSet/vs1', %context)", initialContext);
        assertTrue(isSingleton(result));
        assertTrue(isResourceNode(result));
        assertTrue(getResourceNode(result).resource().is(Parameters.class));

        Parameters parameters = getResourceNode(result).resource().as(Parameters.class);
        assertEquals(getParameterValue(parameters, "result", Boolean.class), Boolean.FALSE);
    }
}
