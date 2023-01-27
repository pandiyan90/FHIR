/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.plannet.test;

import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.registry.test.ExampleIndex;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class ExamplesValidationTest implements ITest {
    private static final String EXAMPLES_PATH = "src/test/resources/examples/";
    private static final String INDEX_FILE_NAME = ".index.json";

    private final String path;

    public ExamplesValidationTest(String path) {
        this.path = path;
    }

    @Override
    public String getTestName() {
        if (!path.startsWith(EXAMPLES_PATH)) {
            throw new IllegalArgumentException("unexpected test path");
        }
        return path.substring(EXAMPLES_PATH.length());
    }

    @Test
    public void testDaVinciPlanNetValidation() throws Exception {
        try (Reader r = Files.newBufferedReader(Paths.get(path))) {
            Resource resource = FHIRParser.parser(Format.JSON).parse(r);
            List<Issue> issues = FHIRValidator.validator().validate(resource);
            issues.forEach(item -> {
                if (item.getSeverity().getValue().equals("error")) {
                    System.out.println(path + " " + item);
                }
            });
            assertEquals(countErrors(issues), 0);
        } catch (Exception e) {
            fail("Exception with " + path, e);
        }
    }

    @Factory
    public Object[] createInstances() {
        List<Object> result = new ArrayList<>();

        File[] directories = new File(EXAMPLES_PATH).listFiles(File::isDirectory);
        for (File versionDir : directories) {
            String versionPath = EXAMPLES_PATH + versionDir.getName() + "/";
            for (ExampleIndex.Entry entry : ExampleIndex.readIndex(Paths.get(versionPath, INDEX_FILE_NAME))) {
                result.add(new ExamplesValidationTest(versionPath + entry.getFileName()));
            }
        }

        return result.toArray();
    }
}