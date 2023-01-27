/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.test;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.getSingleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.examples.ExamplesUtil;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.TupleTypeInfo;
import net.sovrinhealth.fhir.path.TupleTypeInfoElement;
import net.sovrinhealth.fhir.path.TypeInfo;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class TypeFunctionTest {
    @Test
    public void testTypeFunction() throws Exception {
        Patient patient = FHIRParser.parser(Format.JSON).parse(ExamplesUtil.resourceReader("json/spec/patient-example-a.json"));
        EvaluationContext evaluationContext = new EvaluationContext(patient);
        Collection<FHIRPathNode> result = FHIRPathEvaluator.evaluator().evaluate(evaluationContext, "Patient.contact.type()");
        TypeInfo actual = getSingleton(result).asTypeInfoNode().typeInfo();
        List<TupleTypeInfoElement> element = new ArrayList<>();
        element.add(new TupleTypeInfoElement("relationship", "FHIR.CodeableConcept", false));
        element.add(new TupleTypeInfoElement("name", "FHIR.HumanName", true));
        element.add(new TupleTypeInfoElement("telecom", "FHIR.ContactPoint", false));
        element.add(new TupleTypeInfoElement("address", "FHIR.Address", true));
        element.add(new TupleTypeInfoElement("gender", "FHIR.code", true));
        element.add(new TupleTypeInfoElement("organization", "FHIR.Reference", true));
        element.add(new TupleTypeInfoElement("period", "FHIR.Period", true));
        TypeInfo expected = new TupleTypeInfo(element);
        Assert.assertEquals(actual, expected);
    }
}