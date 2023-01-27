/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.us.spl;

import static org.testng.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.path.function.ResolveFunction;
import net.sovrinhealth.fhir.path.function.registry.FHIRPathFunctionRegistry;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class ExamplesValidationTest {
    @BeforeClass
    public void beforeClass() throws Exception {
        FHIRPathFunctionRegistry.getInstance().register(new FileResolveFunction());
    }

    @Test
    public void testValidationJson() throws Exception {
        List<Path> goodPaths = new ArrayList<>();
        List<Path> badPaths = new ArrayList<>();

        for (Path path : Files.list(Paths.get("src/test/resources/json")).collect(Collectors.toList())) {
            if (isValid(path)) {
                goodPaths.add(path);
            } else {
                badPaths.add(path);
            }
        }

        System.out.println("Good Paths");
        goodPaths.forEach(path -> System.out.println(path));
        System.out.println();

        System.out.println("Bad Paths");
        badPaths.forEach(path -> System.out.println(path));

        assertTrue(badPaths.isEmpty());
    }

    @Test
    public void testValidationSingle() throws Exception {
        isValid(Paths.get("src/test/resources/json/MessageHeader-SampleEstablishmentInactivationMessage.json"));
    }

    public boolean isValid(Path path) {
        boolean result = true;
        try (InputStream in = new FileInputStream(path.toFile())) {
            System.out.println("Path: " + path);
            Resource resource = FHIRParser.parser(Format.JSON).parse(in);

            // Before we can validate, we need to update resources with a
            // timestamp so we don't violate:
            // spl-X.1.1.2: The effective time year is equal to the current year
            if (resource.is(Bundle.class)) {
                Bundle bundle = resource.as(Bundle.class);
                if (bundle.getTimestamp() != null && bundle.getTimestamp().getValue() != null) {
                    resource = bundle.toBuilder()
                            .timestamp(Instant.now())
                            .build();
                }
            }
            List<Issue> issues = FHIRValidator.validator().validate(resource);
            for (Issue issue : issues) {
                if (IssueSeverity.ERROR.equals(issue.getSeverity())) {
                    result = false;
                    System.out.println(issue);
                }
            }
            System.out.println("--------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public static class FileResolveFunction extends ResolveFunction {
        @Override
        protected Resource resolveRelativeReference(EvaluationContext evaluationContext, FHIRPathNode node, String type, String logicalId, String versionId) {
            try (InputStream in = ExamplesValidationTest.class.getClassLoader().getResourceAsStream("json/" + type + "-" + logicalId + ".json")) {
                return FHIRParser.parser(Format.JSON).parse(in);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
}
