/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.util;

import java.util.List;
import java.util.stream.Collectors;

import net.sovrinhealth.fhir.model.resource.ConceptMap;
import net.sovrinhealth.fhir.model.resource.ConceptMap.Group;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public final class ConceptMapSupport {
    private ConceptMapSupport() { }

    public static List<Group> translate(ConceptMap conceptMap, Coding coding) {
        return conceptMap.getGroup().stream()
                .filter(group -> group.getSource() != null)
                .filter(group -> group.getSource().equals(coding.getSystem()))
                .filter(group -> group.getSourceVersion() == null ||
                    coding.getVersion() == null ||
                    group.getSourceVersion().equals(coding.getVersion()))
                .map(group -> group.toBuilder()
                    .element(group.getElement().stream()
                        .filter(element -> element.getCode() != null)
                        .filter(element -> element.getCode().equals(coding.getCode()))
                        .collect(Collectors.toList()))
                    .build())
                .filter(group -> !group.getElement().isEmpty())
                .collect(Collectors.toList());
    }

    public static ConceptMap getConceptMap(java.lang.String url) {
        return FHIRRegistry.getInstance().getResource(url, ConceptMap.class);
    }
}
