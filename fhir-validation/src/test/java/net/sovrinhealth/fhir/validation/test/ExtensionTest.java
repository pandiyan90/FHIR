/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.profile.ConstraintGenerator;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class ExtensionTest {
    @Test
    public void testExtension() throws Exception {
        StructureDefinition structureDefinition = FHIRRegistry.getInstance().getResource("http://example.com/fhir/StructureDefinition/test-extension", StructureDefinition.class);
        ConstraintGenerator generator = new ConstraintGenerator(structureDefinition);

        List<Constraint> constraints = generator.generate();
        constraints.forEach(System.out::println);
        assertEquals(constraints.size(), 2);
        assertEquals(constraints.get(1).expression(), "value.where(isTypeEqual(CodeableConcept)).exists() and value.where(isTypeEqual(CodeableConcept)).all(memberOf('http://example.com/fhir/ValueSet/test-value-set', 'required'))");

        Extension extension = Extension.builder()
                .url("http://example.com/fhir/pdm/StructureDefinition/test-extension")
                .value(string("test"))
                .build();

        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Collection<FHIRPathNode> result = evaluator.evaluate(extension, "value.where(isTypeEqual(CodeableConcept)).exists() and value.where(isTypeEqual(CodeableConcept)).all(memberOf('http://example.com/fhir/ValueSet/test-value-set', 'required'))");

        System.out.println("result: " + result);

        assertEquals(result, SINGLETON_FALSE);
    }
}
