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
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.FHIRPathQuantityNode;
import net.sovrinhealth.fhir.path.FHIRPathQuantityValue;
import net.sovrinhealth.fhir.path.FHIRPathResourceNode;
import net.sovrinhealth.fhir.path.FHIRPathStringValue;
import net.sovrinhealth.fhir.path.FHIRPathTimeValue;
import net.sovrinhealth.fhir.path.FHIRPathTypeInfoNode;

public class FHIRPathDefaultNodeVisitor implements FHIRPathNodeVisitor {
    protected void doVisit(FHIRPathResourceNode node) {
        // do nothing
    }
    
    protected void doVisit(FHIRPathElementNode node) {
        // do nothing
    }
    
    protected void visitChildren(FHIRPathNode node) {
        for (FHIRPathNode child : node.children()) {
            child.accept(this);
        }
    }
    
    @Override
    public void visit(FHIRPathBooleanValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathDateValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathDateTimeValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathDecimalValue value) {
        // do nothing
    }

    @Override
    public final void visit(FHIRPathElementNode node) {
        doVisit(node);
        visitChildren(node);
    }

    @Override
    public void visit(FHIRPathIntegerValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathQuantityValue value) {
        // do nothing
    }
    
    @Override
    public void visit(FHIRPathQuantityNode node) {
        visit((FHIRPathElementNode) node);
    }

    @Override
    public final void visit(FHIRPathResourceNode node) {
        doVisit(node);
        visitChildren(node);
    }

    @Override
    public void visit(FHIRPathStringValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathTimeValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathTypeInfoNode node) {
        // do nothing
    }
}