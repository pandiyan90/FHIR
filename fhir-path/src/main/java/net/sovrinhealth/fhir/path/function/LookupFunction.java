/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.empty;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.getElementNode;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.isStringValue;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.path.FHIRPathElementNode;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.FHIRPathResourceNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.term.service.LookupOutcome;
import net.sovrinhealth.fhir.term.service.LookupParameters;

public class LookupFunction extends FHIRPathAbstractTermFunction {
    @Override
    public String getName() {
        return "lookup";
    }

    @Override
    public int getMinArity() {
        return 1;
    }

    @Override
    public int getMaxArity() {
        return 2;
    }

    @Override
    protected Map<String, Function<String, Element>> buildElementFactoryMap() {
        Map<String, Function<String, Element>> map = new HashMap<>();
        map.put("date", DateTime::of);
        map.put("displayLanguage", Code::of);
        map.put("property", Code::of);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        if (!isTermServiceNode(context) ||
                !isCodedElementNode(arguments.get(0), Coding.class, Code.class) ||
                (arguments.size() == 2 && !isStringValue(arguments.get(1)))) {
            return empty();
        }
        FHIRPathElementNode codedElementNode = getElementNode(arguments.get(0));
        Coding coding = getCoding(evaluationContext.getTree(), codedElementNode);
        Parameters parameters = getParameters(arguments);
        LookupOutcome outcome = service.lookup(coding, LookupParameters.from(parameters));
        if (outcome == null) {
            generateIssue(evaluationContext, IssueSeverity.ERROR, IssueType.NOT_SUPPORTED, "Lookup cannot be performed", "%terminologies");
            return empty();
        }
        return singleton(FHIRPathResourceNode.resourceNode(outcome.toParameters()));
    }
}
