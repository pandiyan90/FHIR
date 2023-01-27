/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.cql.engine.searchparam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import net.sovrinhealth.fhir.core.ResourceType;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.model.type.code.SearchParamType;
import net.sovrinhealth.fhir.search.util.SearchHelper;

public class SearchParameterResolver {

    private final SearchHelper searchHelper;

    public SearchParameterResolver(SearchHelper searchHelper) {
        this.searchHelper = searchHelper;
    }

    public SearchParameter getSearchParameterDefinition(String resourceType, String path) throws Exception {
        return getSearchParameterDefinition(resourceType, path, null);
    }

    public SearchParameter getSearchParameterDefinition(String resourceType, String path, SearchParamType paramType)
            throws Exception {
        if (resourceType == null || path == null) {
            return null;
        }

        String name = null;
        if (path.equals("id")) {
            name = "_id";
            path = "";
        }

        // should this use the registry (all parameters) or the filtered set of search parameters for a given tenant?
        Map<String, SearchParameter> params = searchHelper.getSearchParameters(resourceType);

        for (SearchParameter param : params.values()) {
            if (name != null && param.getCode().getValue().equals(name)) {
                return param;
            }

            if (paramType == null || param.getType().equals(paramType)) {
                Set<String> normalizedPath = normalizePath(resourceType, param.getExpression().getValue());
                if (normalizedPath.contains(path)) {
                    return param;
                }
            }
        }

        return null;
    }

    public Pair<String, IQueryParameter> createSearchParameter(String context, String resourceType, String path, String value)
            throws Exception {

        Pair<String, IQueryParameter> result = null;

        SearchParameter searchParam = this.getSearchParameterDefinition(resourceType, path);
        if (searchParam != null) {

            String name = searchParam.getCode().getValue();

            if (SearchParamType.TOKEN.equals(searchParam.getType())) {
                result = Pair.of(name, new TokenParameter(value).setName(name));
            } else if (SearchParamType.REFERENCE.equals(searchParam.getType())) {
                result = Pair.of(name, new ReferenceParameter(ResourceType.from(context), value).setName(name));
            } else if (SearchParamType.QUANTITY.equals(searchParam.getType())) {
                result = Pair.of(name, new QuantityParameter(new BigDecimal(value)).setName(name));
            } else if (SearchParamType.STRING.equals(searchParam.getType())) {
                result = Pair.of(name, new StringParameter(value).setName(name));
            } else if (SearchParamType.NUMBER.equals(searchParam.getType())) {
                result = Pair.of(name, new NumberParameter(new BigDecimal(value)).setName(name));
            } else if (SearchParamType.URI.equals(searchParam.getType())) {
                result = Pair.of(name, new UriParameter(value).setName(name));
            } else {
                throw new UnsupportedOperationException(String.format("SearchParameter type '%s' is not supported", searchParam.getType().toString()));
            }
        }

        return result;
    }

    // This is actually a lot of processing. We should cache search parameter
    // resolutions.
    private Set<String> normalizePath(String resourceType, String path) {
        // TODO: What we really need is FhirPath parsing to just get the path
        // MedicationAdministration.medication.as(CodeableConcept)
        // MedicationAdministration.medication.as(Reference)
        // (MedicationAdministration.medication as CodeableConcept)
        // (MedicationAdministration.medication as Reference)
        // Condition.onset.as(Age) | Condition.onset.as(Range)
        // Observation.code | Observation.component.code

        // Trim off outer parens
        path = removeParens(path);

        Set<String> normalizedParts = new HashSet<String>();
        String[] orParts = path.split("\\|");
        for (String part : orParts) {
            part = part.trim();
            part = removeParens(part);

            // Trim off DataType
            if (part.startsWith(resourceType)) {
                part = part.substring(part.indexOf(".") + 1, part.length());

                // Split into components
                String[] pathSplit = part.split("\\.");
                List<String> newPathParts = new ArrayList<>();

                for (String p : pathSplit) {
                    // Skip the "as(X)" part.
                    if (p.startsWith("as(")) {
                        continue;
                    }

                    // Skip the "[x]" part.
                    if (p.startsWith("[x]")) {
                        continue;
                    }

                    // Filter out spaces and everything after "medication as Reference"
                    String[] ps = p.split(" ");
                    if (ps != null && ps.length > 0) {
                        newPathParts.add(ps[0]);
                    }
                }

                part = String.join(".", newPathParts);
                normalizedParts.add(part);
            }
        }

        // This handles cases such as /Condition?onset-age and /Condition?onset-date
        // where there are multiple underlying representations of the same property
        // (e.g. Condition.onset.as(Age) | Condition.onset.as(Range)), but
        // will punt on something like /Observation?combo-code where the underlying
        // representation maps to multiple places in a nested hierarchy (e.g.
        // Observation.code | Observation.component.code ).
//        if (normalizedParts.contains(path)) {
//            return path;
//        } else {
//            return null;
//        }
        return normalizedParts;
    }

    private String removeParens(String path) {
        if (path.startsWith("(")) {
            path = path.substring(1, path.length() - 1);
        }
        return path;
    }
}
