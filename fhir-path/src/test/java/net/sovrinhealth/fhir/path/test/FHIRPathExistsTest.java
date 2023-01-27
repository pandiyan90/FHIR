/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.test;

import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static org.testng.Assert.assertEquals;

import java.io.Reader;
import java.util.Collection;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.examples.ExamplesUtil;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;

public class FHIRPathExistsTest {
    private static final Patient patient = readPatient();

    @Test
    public void testExists1() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(patient, "Patient.birthDate.exists()");
        assertEquals(result, SINGLETON_TRUE);
    }

    @Test
    public void testExists2() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(patient, "Patient.birthDate.exists($this = @1974-12-25)");
        assertEquals(result, SINGLETON_TRUE);
    }

    @Test
    public void testExists3() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(patient, "Patient.birthDate.exists($this = @2020-01-01)");
        assertEquals(result, SINGLETON_FALSE);
    }

    @Test
    public void testExists4() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(patient, "Patient.birthDate.exists(true)");
        assertEquals(result, SINGLETON_TRUE);
    }

    @Test
    public void testExists5() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(patient, "Patient.birthDate.exists(false)");
        assertEquals(result, SINGLETON_FALSE);
    }

    private static Patient readPatient() {
        try (Reader reader = ExamplesUtil.resourceReader("json/spec/patient-example.json")) {
            return FHIRParser.parser(Format.JSON).parse(reader);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
