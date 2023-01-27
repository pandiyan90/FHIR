/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.ig.us.core.tool;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.sovrinhealth.fhir.core.FHIRConstants;
import net.sovrinhealth.fhir.core.ResourceType;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.model.resource.ValueSet.Compose.Include;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.ElementDefinition;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.BindingStrength;
import net.sovrinhealth.fhir.model.type.code.FHIRDefinedType;
import net.sovrinhealth.fhir.model.type.code.ResourceTypeCode;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

/**
 * A utility for adding the http://fhir.linuxforhealth.org/extension/implicit-system extension
 * to search parameters which always reference code values with a particular system
 */
public class SearchParameterAugmenter {
    private static final String IMPLICIT_SYSTEM_EXT_URL = FHIRConstants.EXT_BASE + "implicit-system";
    private static final FHIRGenerator generator = FHIRGenerator.generator(Format.JSON, true);

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please specify one or more version qualifiers (e.g. '4.0.0 5.0.0')");
        }

        for (String packageVersion : args) {
            updateInPlace(packageVersion);
        }
    }

    public static void updateInPlace(String version) throws Exception {
        Collection<SearchParameter> tokenParams = FHIRRegistry.getInstance().getSearchParameters("token");

        for (SearchParameter searchParameter : tokenParams) {
            if (searchParameter.getUrl() != null && searchParameter.getUrl().hasValue()
                    && searchParameter.getUrl().getValue().startsWith("http://hl7.org/fhir/SearchParameter")) {
                continue; // skip the parameters defined in the base spec
            }

            if (searchParameter.getVersion() == null || !searchParameter.getVersion().hasValue()) {
                System.err.println("Found a SearchParameter that has no version string: " + searchParameter);
            } else if (!version.equals(searchParameter.getVersion().getValue())) {
                continue; // handle a single version at a time
            }

            List<ResourceTypeCode> base = searchParameter.getBase();
            if (base.size() != 1 || base.get(0).getValueAsEnum() == ResourceType.RESOURCE) {
                continue; // too complicated to handle this case right now
            }

            String implicitSystem = getImplicitSystem(searchParameter);

            if (implicitSystem != null) {
                System.out.println(searchParameter.getId() + ": " + implicitSystem);

                String currentValue = searchParameter.getExtension().stream()
                        .filter(e -> IMPLICIT_SYSTEM_EXT_URL.equals(e.getUrl()) && e.getValue() != null)
                        .reduce((a, b) -> {
                            throw new IllegalStateException("Multiple existing extension values: " + a + ", " + b);
                        })
                        .map(e -> e.getValue().as(Uri.class).getValue())
                        .orElse(null);

                if (currentValue == null) {
                    searchParameter = searchParameter.toBuilder()
                            .extension(buildImplicitSystemExtension(implicitSystem))
                            .build();
                } else if (currentValue.equals(implicitSystem)){
                    continue;
                } else {
                    throw new IllegalStateException("Existing SearchParameter '" + searchParameter.getId() +
                            "' already has an implicity system extension and it doesn't match '" + implicitSystem + "'");
                }

                String packageVersion = version.replace(".", "");
                Path path = Paths.get("src/main/resources/hl7/fhir/us/core/" + packageVersion + "/package/SearchParameter-"
                        + searchParameter.getId() + ".json");
                BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"));
                generator.generate(searchParameter, writer);
            }
        }
    }

    private static String getImplicitSystem(SearchParameter searchParameter) {
        String system = null;
        ElementDefinition def = ProfileSupport.getElementDefinition(searchParameter.getExpression().getValue());

        if (def != null &&
                def.getBinding() != null &&
                def.getType().size() == 1 &&
                FHIRDefinedType.CODE.getValue().equals(def.getType().get(0).getCode().getValue()) &&
                BindingStrength.Value.REQUIRED == def.getBinding().getStrength().getValueAsEnum()) {
            Canonical valueSetRef = def.getBinding().getValueSet();
            ValueSet valueSet = FHIRRegistry.getInstance().getResource(valueSetRef.getValue(), ValueSet.class);

            if (valueSet.getCompose() != null) {
                Set<Include> systems = valueSet.getCompose().getInclude().stream().collect(Collectors.toSet());
                if (systems.size() == 1) {
                    Uri systemUri = systems.iterator().next().getSystem();
                    if (systemUri != null) {
                        system = systemUri.getValue();
                    }
                }
            }
        }
        return system;
    }

    public static Extension buildImplicitSystemExtension(String implicitSystemValue) {
        if (implicitSystemValue == null) return null;

        return Extension.builder()
                .url(IMPLICIT_SYSTEM_EXT_URL)
                .value(Uri.of(implicitSystemValue))
                .build();
    }
}
