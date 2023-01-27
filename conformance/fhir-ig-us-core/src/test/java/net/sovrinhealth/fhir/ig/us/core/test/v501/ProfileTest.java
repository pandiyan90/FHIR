/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.us.core.test.v501;

import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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

/**
 * Runs through the Profile index for US Core 5.0.1
 */
public class ProfileTest implements ITest {

    private static final String EXAMPLES_PATH = "JSON/501/";

    private static final String INDEX_FILE_NAME = ".index.json";
    private static final String TEST_RESOURCES_PATH = "src/test/resources/";


    private final String path;

    public ProfileTest(String path) {
        this.path = path;
    }

    @Override
    public String getTestName() {
        return path.substring(TEST_RESOURCES_PATH.length());
    }

    @Test
    public void testUSCoreValidation() throws Exception {
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
        for (ExampleIndex.Entry entry : ExampleIndex.readIndex(EXAMPLES_PATH + INDEX_FILE_NAME)) {
            result.add(new ProfileTest(TEST_RESOURCES_PATH + EXAMPLES_PATH + entry.getFileName()));
        }
        return result.toArray();
    }
}