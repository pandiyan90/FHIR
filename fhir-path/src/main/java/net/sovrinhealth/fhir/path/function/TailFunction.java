/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.empty;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.isUnordered;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class TailFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "tail";
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
        if (isUnordered(context)) {
            throw new IllegalArgumentException("Context must be an ordered collection for function: 'tail'");
        }
        if (!context.isEmpty()) {
            return context.stream()
                .skip(1)
                .collect(Collectors.toList());
        }
        return empty();
    }
}
