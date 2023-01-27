/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function;

import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.empty;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.isResourceNode;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.isStringValue;
import static net.sovrinhealth.fhir.path.util.FHIRPathUtil.singleton;
import static net.sovrinhealth.fhir.term.util.ValueSetSupport.isExpanded;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.model.type.Boolean;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.model.type.Integer;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.FHIRPathResourceNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.term.service.ExpansionParameters;

public class ExpandFunction extends FHIRPathAbstractTermFunction {
    @Override
    public String getName() {
        return "expand";
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
        map.put("context", Uri::of);
        map.put("contextDirection", Code::of);
        map.put("filter", net.sovrinhealth.fhir.model.type.String::of);
        map.put("date", DateTime::of);
        map.put("offset", Integer::of);
        map.put("count", Integer::of);
        map.put("includeDesignations", Boolean::of);
        map.put("activeOnly", Boolean::of);
        map.put("excludeNested", Boolean::of);
        map.put("excludeNotForUI", Boolean::of);
        map.put("excludePostCoordinated", Boolean::of);
        map.put("displayLanguage", Code::of);
        map.put("excludeSystem", Canonical::of);
        map.put("systemVersion", Canonical::of);
        map.put("checkSystemVersion", Canonical::of);
        map.put("forceSystemVersion", Canonical::of);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        if (!isTermServiceNode(context) ||
                (!isResourceNode(arguments.get(0)) && !isStringValue(arguments.get(0))) ||
                (arguments.size() == 2 && !isStringValue(arguments.get(1)))) {
            return empty();
        }
        ValueSet valueSet = getResource(arguments, ValueSet.class);
        if (!isExpanded(valueSet) && !service.isExpandable(valueSet)) {
            String url = (valueSet.getUrl() != null) ? valueSet.getUrl().getValue() : null;
            generateIssue(evaluationContext, IssueSeverity.ERROR, IssueType.NOT_SUPPORTED, "ValueSet with url '" + url + "' is not expandable", "%terminologies");
            return empty();
        }
        Parameters parameters = getParameters(arguments);
        ValueSet expanded = service.expand(valueSet, ExpansionParameters.from(parameters));
        return singleton(FHIRPathResourceNode.resourceNode(expanded));
    }
}
