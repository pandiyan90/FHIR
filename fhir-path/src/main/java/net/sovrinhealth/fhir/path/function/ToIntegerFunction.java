/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.FHIRPathIntegerValue.integerValue;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.empty;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.getSystemValue;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.hasSystemValue;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.singleton;

import java.util.Collection;
import java.util.List;

import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.FHIRPathSystemValue;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class ToIntegerFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "toInteger";
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
        if (!hasSystemValue(context)) {
            return empty();
        }
        FHIRPathSystemValue value = getSystemValue(context);
        if (value.isNumberValue() && value.asNumberValue().isIntegerValue()) {
            return singleton(value);
        }
        if (value.isStringValue()) {
            String string = value.asStringValue().string();
            try {
                return singleton(integerValue(Integer.parseInt(string)));
            } catch (NumberFormatException e) {
                return empty();
            }
        }
        if (value.isBooleanValue()) {
            return singleton(integerValue(value.asBooleanValue().isTrue() ? 1 : 0));
        }            
        return empty();        
    }
}
