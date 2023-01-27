/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.registry.tool;

import static net.sovrinhealth.fhir.registry.util.FHIRRegistryUtil.getUrl;
import static net.sovrinhealth.fhir.registry.util.FHIRRegistryUtil.getVersion;
import static net.sovrinhealth.fhir.registry.util.FHIRRegistryUtil.isDefinitionalResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.model.type.code.ResourceTypeCode;
import net.sovrinhealth.fhir.registry.util.Index;
import net.sovrinhealth.fhir.registry.util.Index.Entry;

public class IndexGenerator {
    private static final String VERSION = "R4B";
    private static final List<String> DEFINITIONS = Arrays.asList(
        // "definitions/" + VERSION + "/conceptmaps.json",
        "definitions/" + VERSION + "/dataelements.json",
        "definitions/" + VERSION + "/extension-definitions.json",
        "definitions/" + VERSION + "/profiles-others.json",
        "definitions/" + VERSION + "/profiles-resources.json",
        "definitions/" + VERSION + "/profiles-types.json",
        "definitions/" + VERSION + "/search-parameters.json",
        // "definitions/" + VERSION + "/v2-tables.json",
        // "definitions/" + VERSION + "/v3-codesystems.json",
        "definitions/" + VERSION + "/valuesets.json");

    public static void main(String[] args) throws Exception {
        Index index = new Index(1);
        for (String definition : DEFINITIONS) {
            System.out.println("Processing " + definition + "...");
            try (FileReader reader = new FileReader(definition)) {
                Bundle bundle = FHIRParser.parser(Format.JSON).parse(reader);
                for (Bundle.Entry entry : bundle.getEntry()) {
                    Resource resource = entry.getResource();

                    if (!isDefinitionalResource(resource)) {
                        continue;
                    }

                    String id = resource.getId();
                    if (id == null) {
                        continue;
                    }

                    String url = getUrl(resource);
                    String version = getVersion(resource);
                    if (url == null || version == null) {
                        continue;
                    }

                    if (resource instanceof SearchParameter &&
                            (id.equals("clinical-patient") || id.equals("Provenance-patient"))) {
                        // Workaround for https://jira.hl7.org/browse/FHIR-13601
                        resource = ((SearchParameter) resource).toBuilder()
                                .target(Collections.singleton(ResourceTypeCode.PATIENT))
                                .build();
                    }

                    String fileName = resource.getClass().getSimpleName() + "-" + id + ".json";
                    File file = new File("src/main/resources/hl7/fhir/core/410/package/" + fileName);

                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                    }

                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(resource.toString());
                    }

                    index.add(Entry.entry(resource));
                }
            }
        }
        try (OutputStream out = new FileOutputStream("src/main/resources/hl7/fhir/core/410/package/.index.json")) {
            index.store(out);
        }
    }
}