/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.FHIRPathStringValue.stringValue;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.getString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class ToCharsFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "toChars";
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
        Collection<FHIRPathNode> result = new ArrayList<>();        
        String string = getString(context);
        for (int i = 0; i < string.length(); i++) {
            result.add(stringValue(String.valueOf(string.charAt(i))));
        }
        return result;
    }
}
