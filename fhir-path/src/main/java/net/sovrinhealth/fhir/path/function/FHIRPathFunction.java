/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import java.util.Collection;
import java.util.List;

import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.path.function.registry.FHIRPathFunctionRegistry;

public interface FHIRPathFunction {
    String getName();
    int getMinArity();
    int getMaxArity();
    
    Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments);
    
    static FHIRPathFunctionRegistry registry() {
        return FHIRPathFunctionRegistry.getInstance();
    }
}
