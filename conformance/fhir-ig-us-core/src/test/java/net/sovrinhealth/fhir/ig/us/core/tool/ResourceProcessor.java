/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.us.core.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.term.util.ValueSetSupport;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

/**
 * This class fixes two issues with the packaged US Core artifacts:
 * <ol>
 * <li>The provided structure definitions contain invalid XHTML</li>
 * <li>The provided structure definitions do not contain a version element</li>
 * </ol>
 */
public class ResourceProcessor {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please specify one or more version qualifiers (e.g. '4.0.0 5.0.0')");
        }

        for (String version : args) {
            String packageVersion = version.replace(".", "");
            expandValueSets(packageVersion);
            updateInPlace(packageVersion);
            updateExamplesInPlace(packageVersion);
        }
    }

    private static void expandValueSets(String packageVersion) throws Exception {
        File dir = new File("src/main/resources/hl7/fhir/us/core/" + packageVersion + "/package/");
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (!fileName.endsWith(".json") || file.isDirectory()
                    || !fileName.startsWith("ValueSet")
                    ) {
                continue;
            }
            ValueSet expandedValueSet;
            try (Reader reader = new FileReader(file)) {
                ValueSet vs = FHIRParser.parser(Format.JSON).parse(reader);
                // Use the Registry to expand the ValueSet.
                // US Core ValueSets are built on VSAC ValueSets and so in this case, these expansions
                // indirectly depend on the VSACRegistryResourceProvider for those.
                expandedValueSet = ValueSetSupport.expand(vs);
            }
            try (FileWriter writer = new FileWriter(file)) {
                FHIRGenerator.generator(Format.JSON, true).generate(expandedValueSet, writer);
            }
        }
    }

    private static void updateInPlace(String version) throws Exception {
        Map<String, Object> writerConfig = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
        JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
        JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(writerConfig);
        JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(null);

        File dir = new File("src/main/resources/hl7/fhir/us/core/" + version + "/package/");
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (!fileName.endsWith(".json") || file.isDirectory()
                    || fileName.startsWith(".index.json")
                    || fileName.startsWith("package.json")
                    ) {
                continue;
            }
            JsonObject jsonObject = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                JsonReader jsonReader = jsonReaderFactory.createReader(reader);
                jsonObject = jsonReader.readObject();

                JsonObjectBuilder jsonObjectBuilder = jsonBuilderFactory.createObjectBuilder(jsonObject);

                JsonObject text = jsonObject.getJsonObject("text");
                if (text != null) {
                    // Replace the generated text with some [much smaller] generic placeholder
                    JsonObjectBuilder textBuilder = jsonBuilderFactory.createObjectBuilder();
                    textBuilder.add("status", "empty");
                    textBuilder.add("div", "<div xmlns=\"http://www.w3.org/1999/xhtml\">Redacted for size</div>");
                    jsonObjectBuilder.add("text", textBuilder);
                }

                if (!jsonObject.containsKey("version")) {
                    System.out.println("file: " + file + " does not have a version");
                    jsonObjectBuilder.add("version", version);
                }

                jsonObject = jsonObjectBuilder.build();
            }
            try (FileWriter writer = new FileWriter(file)) {
                JsonWriter jsonWriter = jsonWriterFactory.createWriter(writer);
                jsonWriter.write(jsonObject);
            }
        }
    }

    private static void updateExamplesInPlace(String version) throws Exception {
        Map<String, Object> writerConfig = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
        JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
        JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(writerConfig);
        JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(null);

        File dir = new File("src/test/resources/JSON/" + version + "/");
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (!fileName.endsWith(".json") || file.isDirectory()
                    || fileName.startsWith(".index.json")
                    || fileName.startsWith("package.json")
                    ) {
                continue;
            }
            JsonObject jsonObject = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                JsonReader jsonReader = jsonReaderFactory.createReader(reader);
                jsonObject = jsonReader.readObject();

                JsonObjectBuilder jsonObjectBuilder = jsonBuilderFactory.createObjectBuilder(jsonObject);
                jsonObject = jsonObjectBuilder.build();
            }
            try (FileWriter writer = new FileWriter(file)) {
                JsonWriter jsonWriter = jsonWriterFactory.createWriter(writer);
                jsonWriter.write(jsonObject);
            }
        }
    }
}