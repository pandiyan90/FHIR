/*
 * (C) Copyright IBM Corp. 2019
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.buildClassInfo;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.buildSimpleTypeInfo;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.buildTupleTypeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.FHIRPathType;
import net.sovrinhealth.fhir.path.FHIRPathTypeInfoNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class TypeFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "type";
    }

    @Override
    public int getMinArity() {
        return 0;
    }

    @Override
    public int getMaxArity() {
        return 0;
    }
    
    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        List<FHIRPathNode> result = new ArrayList<>();
        for (FHIRPathNode node : context) {
            FHIRPathType type = node.type();
            if ("System".equals(type.namespace()) && !FHIRPathType.SYSTEM_TYPE_INFO.isAssignableFrom(type)) {
                result.add(FHIRPathTypeInfoNode.typeInfoNode(buildSimpleTypeInfo(type)));
            } else if (FHIRPathType.FHIR_BACKBONE_ELEMENT.equals(type)){
                result.add(FHIRPathTypeInfoNode.typeInfoNode(buildTupleTypeInfo(node.asElementNode().element().getClass())));
            } else {
                result.add(FHIRPathTypeInfoNode.typeInfoNode(buildClassInfo(type)));
            }
        }
        return result;
    }
}