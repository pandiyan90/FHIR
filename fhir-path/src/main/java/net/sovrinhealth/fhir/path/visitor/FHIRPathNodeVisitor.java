/*
 * (C) Copyright IBM Corp. 2019
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.visitor;

import net.sovrinhealth.fhir.path.FHIRPathBooleanValue;
import net.sovrinhealth.fhir.path.FHIRPathDateTimeValue;
import net.sovrinhealth.fhir.path.FHIRPathDateValue;
import net.sovrinhealth.fhir.path.FHIRPathDecimalValue;
import net.sovrinhealth.fhir.path.FHIRPathElementNode;
import net.sovrinhealth.fhir.path.FHIRPathIntegerValue;
import net.sovrinhealth.fhir.path.FHIRPathQuantityNode;
import net.sovrinhealth.fhir.path.FHIRPathQuantityValue;
import net.sovrinhealth.fhir.path.FHIRPathResourceNode;
import net.sovrinhealth.fhir.path.FHIRPathStringValue;
import net.sovrinhealth.fhir.path.FHIRPathTimeValue;
import net.sovrinhealth.fhir.path.FHIRPathTypeInfoNode;

public interface FHIRPathNodeVisitor {
    void visit(FHIRPathBooleanValue value);
    void visit(FHIRPathDateValue value);
    void visit(FHIRPathDateTimeValue value);
    void visit(FHIRPathDecimalValue value);
    void visit(FHIRPathElementNode node);
    void visit(FHIRPathIntegerValue value);
    void visit(FHIRPathQuantityNode node);
    void visit(FHIRPathQuantityValue value);
    void visit(FHIRPathResourceNode node);
    void visit(FHIRPathStringValue value);
    void visit(FHIRPathTimeValue value);
    void visit(FHIRPathTypeInfoNode node);
}