/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.search.TotalValueSet;
import net.sovrinhealth.fhir.search.context.FHIRSearchContext;

/**
 * This testng test class contains methods that test the parsing of the _total parameter in the
 * SearchUtil class.
 */
public class TotalParameterParseTest extends BaseSearchTest {

    @Test
    public void testTotalNotSpecified() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();

        FHIRSearchContext context = searchHelper.parseQueryParameters(Patient.class, queryParameters);
        assertNotNull(context);
        assertNull(context.getTotalParameter());
    }

    @Test
    public void testTotal() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();

        queryParameters.put("_total", Arrays.asList("estimate"));
        FHIRSearchContext context = searchHelper.parseQueryParameters(Patient.class, queryParameters);
        assertNotNull(context);
        assertEquals(context.getTotalParameter(), TotalValueSet.ESTIMATE);
    }

    @Test
    public void testTotalInvalid_lenient() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();

        queryParameters.put("_total", Arrays.asList("invalid"));
        FHIRSearchContext context = searchHelper.parseQueryParameters(Patient.class, queryParameters, true, true);
        assertNotNull(context);
        assertNull(context.getTotalParameter());
    }

    @Test
    public void testTotalInvalid_strict() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        boolean isExceptionThrown = false;

        queryParameters.put("_total", Arrays.asList("invalid"));
        try {
            searchHelper.parseQueryParameters(Patient.class, queryParameters, false, true);
        } catch(Exception ex) {
            isExceptionThrown = true;
            assertEquals(ex.getMessage(), "An error occurred while parsing parameter '_total'.");

        }
        assertTrue(isExceptionThrown);
    }

    @Test
    public void testTotalMultipleParams_lenient() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();

        queryParameters.put("_total", Arrays.asList("none", "accurate"));
        FHIRSearchContext context = searchHelper.parseQueryParameters(Patient.class, queryParameters, true, true);
        assertNotNull(context);
        assertEquals(context.getTotalParameter(), TotalValueSet.NONE);
    }

    @Test
    public void testTotalMultipleParams_strict() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        boolean isExceptionThrown = false;

        queryParameters.put("_total", Arrays.asList("none", "accurate"));
        try {
            searchHelper.parseQueryParameters(Patient.class, queryParameters, false, true);
        } catch(Exception ex) {
            isExceptionThrown = true;
            assertEquals(ex.getMessage(), "Search parameter '_total' is specified multiple times");

        }
        assertTrue(isExceptionThrown);
    }
}
