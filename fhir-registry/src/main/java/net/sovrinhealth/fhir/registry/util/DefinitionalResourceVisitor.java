/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.registry.util;

import net.sovrinhealth.fhir.model.resource.ActivityDefinition;
import net.sovrinhealth.fhir.model.resource.CapabilityStatement;
import net.sovrinhealth.fhir.model.resource.ChargeItemDefinition;
import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.CompartmentDefinition;
import net.sovrinhealth.fhir.model.resource.ConceptMap;
import net.sovrinhealth.fhir.model.resource.EventDefinition;
import net.sovrinhealth.fhir.model.resource.Evidence;
import net.sovrinhealth.fhir.model.resource.EvidenceVariable;
import net.sovrinhealth.fhir.model.resource.ExampleScenario;
import net.sovrinhealth.fhir.model.resource.GraphDefinition;
import net.sovrinhealth.fhir.model.resource.ImplementationGuide;
import net.sovrinhealth.fhir.model.resource.Library;
import net.sovrinhealth.fhir.model.resource.Measure;
import net.sovrinhealth.fhir.model.resource.MessageDefinition;
import net.sovrinhealth.fhir.model.resource.NamingSystem;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.PlanDefinition;
import net.sovrinhealth.fhir.model.resource.Questionnaire;
import net.sovrinhealth.fhir.model.resource.ResearchDefinition;
import net.sovrinhealth.fhir.model.resource.ResearchElementDefinition;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.resource.StructureMap;
import net.sovrinhealth.fhir.model.resource.TerminologyCapabilities;
import net.sovrinhealth.fhir.model.resource.TestScript;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.visitor.DefaultVisitor;

public class DefinitionalResourceVisitor extends DefaultVisitor {
    private String url;
    private String version;
    
    public DefinitionalResourceVisitor() {
        super(true);
    }
    
    public String getUrl() {
        return url;
    }
    
    private String getUrl(Uri uri) {
        return (uri != null) ? uri.getValue() : null;
    }
    
    public String getVersion() {
        return version;
    }
    
    private String getVersion(net.sovrinhealth.fhir.model.type.String string) {
        return (string != null) ? string.getValue() : null;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ActivityDefinition activityDefinition) {
        url = getUrl(activityDefinition.getUrl());
        version = getVersion(activityDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, CapabilityStatement capabilityStatement) {
        url = getUrl(capabilityStatement.getUrl());
        version = getVersion(capabilityStatement.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ChargeItemDefinition chargeItemDefinition) {
        url = getUrl(chargeItemDefinition.getUrl());
        version = getVersion(chargeItemDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, CodeSystem codeSystem) {
        url = getUrl(codeSystem.getUrl());
        version = getVersion(codeSystem.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, CompartmentDefinition compartmentDefinition) {
        url = getUrl(compartmentDefinition.getUrl());
        version = getVersion(compartmentDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ConceptMap conceptMap) {
        url = getUrl(conceptMap.getUrl());
        version = getVersion(conceptMap.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, EventDefinition eventDefinition) {
        url = getUrl(eventDefinition.getUrl());
        version = getVersion(eventDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, Evidence evidence) {
        url = getUrl(evidence.getUrl());
        version = getVersion(evidence.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, EvidenceVariable evidenceVariable) {
        url = getUrl(evidenceVariable.getUrl());
        version = getVersion(evidenceVariable.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ExampleScenario exampleScenario) {
        url = getUrl(exampleScenario.getUrl());
        version = getVersion(exampleScenario.getVersion());
        return false;
    } 
    
    @Override
    public boolean visit(String elementName, int elementIndex, GraphDefinition graphDefinition) {
        url = getUrl(graphDefinition.getUrl());
        version = getVersion(graphDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ImplementationGuide implementationGuide) {
        url = getUrl(implementationGuide.getUrl());
        version = getVersion(implementationGuide.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, Library library) {
        url = getUrl(library.getUrl());
        version = getVersion(library.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, Measure measure) {
        url = getUrl(measure.getUrl());
        version = getVersion(measure.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, MessageDefinition messageDefinition) {
        url = getUrl(messageDefinition.getUrl());
        version = getVersion(messageDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, NamingSystem namingSystem) {
        /*
        url = getUrl(namingSystem.getUrl());
        version = getVersion(namingSystem.getVersion());
        */
        url = version = null;
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, OperationDefinition operationDefinition) {
        url = getUrl(operationDefinition.getUrl());
        version = getVersion(operationDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, PlanDefinition planDefinition) {
        url = getUrl(planDefinition.getUrl());
        version = getVersion(planDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, Questionnaire questionnaire) {
        url = getUrl(questionnaire.getUrl());
        version = getVersion(questionnaire.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ResearchDefinition researchDefinition) {
        url = getUrl(researchDefinition.getUrl());
        version = getVersion(researchDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ResearchElementDefinition researchElementDefinition) {
        url = getUrl(researchElementDefinition.getUrl());
        version = getVersion(researchElementDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, SearchParameter searchParameter) {
        url = getUrl(searchParameter.getUrl());
        version = getVersion(searchParameter.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, StructureDefinition structureDefinition) {
        url = getUrl(structureDefinition.getUrl());
        version = getVersion(structureDefinition.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, StructureMap structureMap) {
        url = getUrl(structureMap.getUrl());
        version = getVersion(structureMap.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, TerminologyCapabilities terminologyCapabilities) {
        url = getUrl(terminologyCapabilities.getUrl());
        version = getVersion(terminologyCapabilities.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, TestScript testScript) {
        url = getUrl(testScript.getUrl());
        version = getVersion(testScript.getVersion());
        return false;
    }
    
    @Override
    public boolean visit(String elementName, int elementIndex, ValueSet valueSet) {
        url = getUrl(valueSet.getUrl());
        version = getVersion(valueSet.getVersion());
        return false;
    }
}