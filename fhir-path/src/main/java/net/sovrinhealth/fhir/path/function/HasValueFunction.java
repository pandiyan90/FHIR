/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.getSingleton;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.isSingleton;

import java.util.Collection;
import java.util.List;

import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class HasValueFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "hasValue";
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
        if (isSingleton(context)) {
            FHIRPathNode node = getSingleton(context);
            if (node.isElementNode() && node.asElementNode().hasValue()) {
                return SINGLETON_TRUE;
            }
        }
        return SINGLETON_FALSE;        
    }
}
