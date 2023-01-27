/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.term.service.test;

import static net.sovrinhealth.fhir.term.util.ValueSetSupport.getValueSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.CodeSystem.Concept;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.model.resource.ValueSet.Expansion.Contains;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.term.service.FHIRTermService;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;

public class PolyhierarchyExpansionTest {
    @Test
    void testConvertCodeSystemPropertiesToNested() {
        CodeSystem codeSystem = CodeSystemSupport.getCodeSystem("http://example.com/fhir/CodeSystem/poly|1.0.0");

        CodeSystem expand = CodeSystemSupport.convertToSimpleCodeSystem(codeSystem);
        assertNotNull(expand);

        List<Concept> concepts = expand.getConcept();
        assertEquals(concepts.size(), 2);

        // confirm that the same concept ("C") is referenced under both "letter" and "numeral"
        Concept c1 = null;
        Concept c2 = null;
        for (Concept concept1 : concepts) {
            if ("letter".equals(concept1.getCode().getValue())) {
                for (Concept concept2 : concept1.getConcept()) {
                    if ("uppercase".equals(concept2.getCode().getValue())) {
                        c1 = concept2.getConcept().get(2);
                    }
                }
            } else {
                for (Concept concept2 : concept1.getConcept()) {
                    if ("C".equals(concept2.getCode().getValue())) {
                        c2 = concept2;
                    }
                }
            }
        }
        assertSame(c1, c2);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    void testConvertCodeSystemPropertiesWithCycle() {
        CodeSystem codeSystem = CodeSystemSupport.getCodeSystem("http://example.com/fhir/CodeSystem/poly|1.0.0");
        // add a bogus concept that introduces a cycle
        codeSystem = codeSystem.toBuilder()
                .concept(CodeSystem.Concept.builder()
                        .code(Code.of("test1"))
                        .property(CodeSystem.Concept.Property.builder()
                                .code(Code.of("subsumedBy"))
                                .value(Code.of("test2"))
                                .build())
                        .build())
                .concept(CodeSystem.Concept.builder()
                        .code(Code.of("test2"))
                        .property(CodeSystem.Concept.Property.builder()
                                .code(Code.of("subsumedBy"))
                                .value(Code.of("test1"))
                                .build())
                        .build())
                .build();

        CodeSystemSupport.convertToSimpleCodeSystem(codeSystem);
        fail("Should have thrown but didn't");
    }

    @Test
    void testExpandValueSetWithCodeSystemPolyhierarchy() {
        ValueSet valueSet = getValueSet("http://example.com/fhir/ValueSet/poly|1.0.0");

        ValueSet expand = FHIRTermService.getInstance().expand(valueSet);
        assertNotNull(expand);
        assertNotNull(expand.getExpansion());
        assertNotNull(expand.getExpansion().getContains());

        List<Contains> concepts = expand.getExpansion().getContains();

        assertEquals(concepts.size(), 55); // 26 + 1("ch") + 1("lowercase") + 26 + 1("uppercase")
    }
}
