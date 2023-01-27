/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.service.provider;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.sovrinhealth.fhir.cache.annotation.Cacheable;
import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.CodeSystem.Concept;
import net.sovrinhealth.fhir.model.resource.ValueSet.Compose.Include.Filter;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.code.CodeSystemContentMode;
import net.sovrinhealth.fhir.term.exception.FHIRTermException;
import net.sovrinhealth.fhir.term.service.exception.FHIRTermServiceException;
import net.sovrinhealth.fhir.term.spi.AbstractTermServiceProvider;
import net.sovrinhealth.fhir.term.spi.FHIRTermServiceProvider;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;

/**
 * Registry-based implementation of the {@link FHIRTermServiceProvider} interface using {@link CodeSystemSupport}
 */
public class RegistryTermServiceProvider extends AbstractTermServiceProvider {
    @Override
    public Set<Concept> closure(CodeSystem codeSystem, Code code) {
        checkArguments(codeSystem, code);
        Concept concept = CodeSystemSupport.findConcept(codeSystem, code);
        return CodeSystemSupport.getConcepts(concept);
    }

    @Cacheable(maximumSize = 1024)
    @Override
    public Concept getConcept(CodeSystem codeSystem, Code code) {
        checkArguments(codeSystem, code);
        Concept concept = CodeSystemSupport.findConcept(codeSystem, code);
        if (concept != null) {
            // child concepts are removed for consistency with the other providers
            return CodeSystemSupport.CONCEPT_NO_CHILDREN_FUNCTION.apply(concept);
        }
        return null;
    }

    @Override
    public Set<Concept> getConcepts(CodeSystem codeSystem) {
        return getConcepts(codeSystem, CodeSystemSupport.SIMPLE_CONCEPT_FUNCTION);
    }

    @Override
    public <R> Set<R> getConcepts(CodeSystem codeSystem, Function<Concept, ? extends R> function) {
        checkArguments(codeSystem, function);
        return CodeSystemSupport.getConcepts(codeSystem, function);
    }

    @Override
    public Set<Concept> getConcepts(CodeSystem codeSystem, List<Filter> filters) {
        return getConcepts(codeSystem, filters, CodeSystemSupport.SIMPLE_CONCEPT_FUNCTION);
    }

    @Override
    public <R> Set<R> getConcepts(CodeSystem codeSystem, List<Filter> filters, Function<Concept, ? extends R> function) {
        checkArguments(codeSystem, filters, function);
        try {
            CodeSystem simpleCodeSystem = CodeSystemSupport.convertToSimpleCodeSystem(codeSystem);
            return CodeSystemSupport.getConcepts(simpleCodeSystem, filters, function);
        } catch (FHIRTermException e) {
            throw new FHIRTermServiceException(e.getMessage(), e, e.getIssues());
        }
    }

    @Override
    public boolean hasConcept(CodeSystem codeSystem, Code code) {
        return getConcept(codeSystem, code) != null;
    }

    @Override
    public boolean isSupported(CodeSystem codeSystem) {
        checkArgument(codeSystem);
        return CodeSystemContentMode.COMPLETE.equals(codeSystem.getContent());
    }

    @Override
    public boolean subsumes(CodeSystem codeSystem, Code codeA, Code codeB) {
        checkArguments(codeSystem, codeA, codeB);
        Concept concept = CodeSystemSupport.findConcept(codeSystem, codeA);
        return (CodeSystemSupport.findConcept(codeSystem, concept, codeB) != null);
    }
}
