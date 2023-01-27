/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.model.type.String.string;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public abstract class FHIRPathAbstractFunction implements FHIRPathFunction {
    @Override
    public abstract String getName();

    @Override
    public abstract int getMinArity();

    @Override
    public abstract int getMaxArity();

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        throw new UnsupportedOperationException("Function: '" + getName() + "' is not supported");
    }

    protected void generateIssue(
            EvaluationContext evaluationContext,
            IssueSeverity severity,
            IssueType code,
            String description,
            String expression) {
        evaluationContext.getIssues().add(Issue.builder()
            .severity(severity)
            .code(code)
            .details(CodeableConcept.builder()
                .text(string(description))
                .build())
            .expression(string(expression))
            .build());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FHIRPathFunction)) {
            return false;
        }
        FHIRPathFunction other = (FHIRPathFunction) obj;
        return Objects.equals(getName(), other.getName()) &&
                Objects.equals(getMinArity(), other.getMinArity()) &&
                Objects.equals(getMaxArity(), other.getMaxArity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMinArity(), getMaxArity());
    }

    protected Collection<FHIRPathNode> getCachedFunctionResult(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        return evaluationContext.getCachedFunctionResult(getName(), context, arguments);
    }

    protected Collection<FHIRPathNode> cacheFunctionResult(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments, Collection<FHIRPathNode> result) {
        evaluationContext.cacheFunctionResult(getName(), context, arguments, result);
        return result;
    }

    protected boolean hasCachedFunctionResult(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        return evaluationContext.hasCachedFunctionResult(getName(), context, arguments);
    }
}
