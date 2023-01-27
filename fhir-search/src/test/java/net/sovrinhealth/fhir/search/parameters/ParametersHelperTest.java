/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.parameters;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.search.compartment.CompartmentHelper;
import net.sovrinhealth.fhir.search.test.BaseSearchTest;

/**
 * Tests ParametersHelper
 */
public class ParametersHelperTest extends BaseSearchTest {
    public static final boolean DEBUG = false;

    CompartmentHelper compartmentHelper = new CompartmentHelper();
    ParametersHelper parametersHelper = new ParametersHelper(compartmentHelper);

    @Test
    public void testGetAllSearchParameters() throws IOException {
        // Tests JSON
        Set<SearchParameter> params = ParametersHelper.getAllSearchParameters();
        assertNotNull(params);
        // Intentionally the data is captured in the bytearray output stream.
        try (ByteArrayOutputStream outBA = new ByteArrayOutputStream(); PrintStream out = new PrintStream(outBA, true, UTF_8);) {
            parametersHelper.print(out);
            Assert.assertNotNull(outBA);
        }
        assertEquals(params.size(), 1418);
    }

    @Test
    public void testPrint() {
        // Test the output, OK, if it gets through.
        try (ByteArrayOutputStream outBA = new ByteArrayOutputStream(); PrintStream out = new PrintStream(outBA, true, UTF_8);) {
            parametersHelper.print(out);
            assertNotNull(outBA);
            assertNotNull(outBA.toByteArray());
            if (DEBUG) {
                System.out.println(outBA);
            }
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void testGetTenantSPs() {
        // getBuiltInSearchParameterMapByResourceType
        Map<String, ParametersMap> result = parametersHelper.getTenantSPs("default");
        assertNotNull(result);
        assertNull(result.get("Junk"));
        assertFalse(result.get("Observation").getCodes().isEmpty());
    }

    /**
     * To execute: update net.sovrinhealth.fhir.search.parameters.ParametersUtil log level to FINE
     * and manually invoke this test.  You should see only a single message:
     * <pre>
     * The code and name of the search parameter does not match [_code] [_notcode]
     * </pre>
     */
    @Test(enabled = false)
    public void testCheckAndWarnForIssueWithCodeAndName() {
        // Issue 202 : added warning and corresponding test.
        ParametersHelper.checkAndWarnForIssueWithCodeAndName(null, null);
        ParametersHelper.checkAndWarnForIssueWithCodeAndName(null, "");
        ParametersHelper.checkAndWarnForIssueWithCodeAndName("", null);
        ParametersHelper.checkAndWarnForIssueWithCodeAndName("", "");
        ParametersHelper.checkAndWarnForIssueWithCodeAndName("_code", "_code");

        // This is the one line that should produce a log message (level=FINE)
        ParametersHelper.checkAndWarnForIssueWithCodeAndName("_code", "_notcode");
    }
}
