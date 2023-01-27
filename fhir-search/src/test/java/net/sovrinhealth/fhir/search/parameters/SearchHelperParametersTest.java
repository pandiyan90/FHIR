/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.parameters;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.search.test.BaseSearchTest;

/**
 * Tests the ParametersHelper through the SearchHelper.
 */
public class SearchHelperParametersTest extends BaseSearchTest {
    @Test
    public void testGetSearchParameters1Default() throws Exception {
        // Simple test looking only for built-in search parameters for Observation.class.
        // Use default tenant id ("default") which has no Observation tenant-specific
        // search parameters.
        Map<String, SearchParameter> result = searchHelper.getSearchParameters(Observation.class.getSimpleName());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        printSearchParameters("testGetSearchParameters1", result);

        assertEquals(44, result.size());
    }

    @Test
    public void testGetSearchParameters2Default() throws Exception {
        // Looking for built-in and tenant-specific search parameters for "Patient"
        // and "Observation". Use the extended tenant since it has some Patient search
        // parameters defined.
        FHIRRequestContext.get().setTenantId("extended");

        Map<String, SearchParameter> result = searchHelper.getSearchParameters("Patient");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters2/Patient", result);
        assertEquals(37, result.size());

        result = searchHelper.getSearchParameters("Observation");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters2/Observation", result);
        assertEquals(44, result.size());
    }

    @Test
    public void testGetSearchParameters3Tenant() throws Exception {
        // Looking for built-in and tenant-specific search parameters for "Observation".

        // Use tenant1 since it has some Patient search parameters defined.
        FHIRRequestContext.get().setTenantId("tenant1");

        // tenant1's filtering includes only 1 search parameter for Observation.
        Map<String, SearchParameter> result = searchHelper.getSearchParameters("Observation");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters3/Observation", result);

        assertEquals(8, result.size());
        Set<String> codes = result.keySet();
        assertTrue(codes.contains("code"));
        assertTrue(codes.contains("value-range"));
        assertTrue(codes.contains("_lastUpdated"));
        assertTrue(codes.contains("_id"));

        result = searchHelper.getSearchParameters("Immunization");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters3/Immunization", result);
        assertEquals(22, result.size());
    }

    @Test
    public void testGetSearchParameters4Tenant() throws Exception {
        // Test filtering of search parameters for Device (tenant1).
        FHIRRequestContext.get().setTenantId("tenant1");

        Map<String, SearchParameter> result = searchHelper.getSearchParameters("Device");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters4/Device", result);
        assertEquals(8, result.size());
        Set<String> codes = result.keySet();
        assertTrue(codes.contains("patient"));
        assertTrue(codes.contains("organization"));
    }

    @Test
    public void testGetSearchParameters5Tenant() throws Exception {
        // Test filtering of search parameters for Patient (tenant1).
        FHIRRequestContext.get().setTenantId("tenant1");

        Map<String, SearchParameter> result = searchHelper.getSearchParameters("Patient");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters5/Patient", result);
        assertEquals(10, result.size());
        Set<String> codes = result.keySet();
        assertTrue(codes.contains("active"));
        assertTrue(codes.contains("address"));
        assertTrue(codes.contains("birthdate"));
        assertTrue(codes.contains("name"));

        // Make sure we get all of the MedicationAdministration search parameters.
        // (No filtering configured for these)
        result = searchHelper.getSearchParameters("MedicationAdministration");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters5/MedicationAdministration", result);
        assertEquals(19, result.size());
    }

    @Test
    public void testGetSearchParameters6Default() throws Exception {
        // Test filtering of search parameters for Patient (extended tenant).
        FHIRRequestContext.get().setTenantId("extended");

        Map<String, SearchParameter> result = searchHelper.getSearchParameters("Patient");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters6/Patient", result);
        assertEquals(37, result.size());

        result = searchHelper.getSearchParameters("Device");
        assertNotNull(result);
        printSearchParameters("testGetSearchParameters6/Device", result);
        assertEquals(20, result.size());
    }

    @Test
    public void testVersionedSearchParameterFilter() throws Exception {
        // Test filtering of search parameters for Patient (default tenant).
        FHIRRequestContext.get().setTenantId("tenant4");

        Map<String, SearchParameter> result = searchHelper.getSearchParameters("Device");
        assertNotNull(result);
        printSearchParameters("testVersionedSearchParameterFilter/Device", result);
        boolean found = false;
        for (SearchParameter sp : result.values()) {
            System.out.println(sp.getUrl().getValue() + "|" + sp.getVersion().getValue());
            if ("http://example.com/SearchParameter/sp_a".equals(sp.getUrl().getValue())) {
                assertNotEquals("1.0.1", sp.getVersion());
                found = true;
            }
        }
        assertTrue(found);

        FHIRRequestContext.get().setTenantId("tenant5");

        result = searchHelper.getSearchParameters("Device");
        assertNotNull(result);
        printSearchParameters("testVersionedSearchParameterFilter/Device", result);
        found = false;
        for (SearchParameter sp : result.values()) {
            if ("http://example.com/SearchParameter/sp_a".equals(sp.getUrl().getValue())) {
                assertNotEquals("1.0.0", sp.getVersion());
                found = true;
            }
        }
        assertTrue(found);
    }
}
