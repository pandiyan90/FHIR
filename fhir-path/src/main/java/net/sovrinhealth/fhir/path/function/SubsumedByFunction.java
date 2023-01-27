/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.empty;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.getElementNode;

import java.util.Collection;
import java.util.List;

import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.code.ConceptSubsumptionOutcome;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class SubsumedByFunction extends FHIRPathAbstractTermFunction {
    @Override
    public String getName() {
        return "subsumedBy";
    }

    @Override
    public int getMinArity() {
        return 1;
    }

    @Override
    public int getMaxArity() {
        return 1;
    }

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        if (!isCodedElementNode(context, Coding.class, Code.class) || !isCodedElementNode(arguments.get(0), Coding.class, Code.class)) {
            return empty();
        }

        Coding codingA = getCoding(evaluationContext.getTree(), getElementNode(context));
        Coding codingB = getCoding(evaluationContext.getTree(), getElementNode(arguments.get(0)));

        ConceptSubsumptionOutcome outcome = service.subsumes(codingA, codingB);

        if (outcome == null) {
            generateIssue(evaluationContext, IssueSeverity.ERROR, IssueType.NOT_SUPPORTED, "Subsumption cannot be tested", getElementNode(context).path());
            return empty();
        }

        switch (outcome.getValueAsEnum()) {
        case EQUIVALENT:
        case SUBSUMED_BY:
            return SINGLETON_TRUE;
        default:
            return SINGLETON_FALSE;
        }
    }
}
