/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test.profiles.uscore.v311;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.client.FHIRParameters;
import net.sovrinhealth.fhir.client.FHIRResponse;
import net.sovrinhealth.fhir.ig.us.core.tool.USCoreExamplesUtil;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.server.test.profiles.ProfilesTestBase.ProfilesTestBaseV2;

/**
 * Tests the US Core 3.1.1 Profile with Observation.
 * https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-Observation.html
 */
public class USCoreObservationLabTest extends ProfilesTestBaseV2 {

    private String observationId1 = null;
    private String observationId2 = null;
    private String observationId3 = null;

    @Override
    public List<String> getRequiredProfiles() {
        return Arrays.asList("http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab|3.1.1");
    }

    @Override
    public void loadResources() throws Exception {
        loadObservation1();
        loadObservation2();
        loadObservation3();
    }

    public void loadObservation1() throws Exception {
        String resource = "Observation-usg.json";
        Observation observation = USCoreExamplesUtil.readLocalJSONResource("311", resource);
        observationId1 = createResourceAndReturnTheLogicalId("Observation", observation);
    }

    public void loadObservation2() throws Exception {
        String resource = "Observation-serum-total-bilirubin.json";
        Observation observation = USCoreExamplesUtil.readLocalJSONResource("311", resource);
        observationId2 = createResourceAndReturnTheLogicalId("Observation", observation);
    }

    public void loadObservation3() throws Exception {
        String resource = "Observation-erythrocytes.json";
        Observation observation = USCoreExamplesUtil.readLocalJSONResource("311", resource);
        observationId3 = createResourceAndReturnTheLogicalId("Observation", observation);
    }

    @Test
    public void testSearchForPatient() throws Exception {
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(Observation.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, observationId1);
        assertContainsIds(bundle, observationId2);
        assertContainsIds(bundle, observationId3);
    }

    @Test
    public void testSearchForPatientAndCategory() throws Exception {
        // SHALL support searching using the combination of the patient and category search parameters:
        // GET
        // [base]/Observation?patient=[reference]&category=http://terminology.hl7.org/CodeSystem/observation-category|laboratory
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://terminology.hl7.org/CodeSystem/observation-category|laboratory");
        FHIRResponse response = client.search(Observation.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, observationId1);
        assertContainsIds(bundle, observationId2);
        assertContainsIds(bundle, observationId3);
    }

    @Test
    public void testSearchForPatientAndMultipleCodes() throws Exception {
        // SHALL support searching using the combination of the patient and code search parameters:
        // including optional support for composite OR search on code (e.g.code={system|}[code],{system|}[code],...)
        // GET [base]/Observation?patient=[reference]&code={system|}[code]{,{system|}[code],...}
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("code", "http://loinc.org|5811-5,1975-2");
        FHIRResponse response = client.search(Observation.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, observationId1);
        assertContainsIds(bundle, observationId2);
        assertDoesNotContainsIds(bundle, observationId3);
    }

    @Test
    public void testSearchForPatientAndCategoryAndDate() throws Exception {
        // SHALL support searching using the combination of the patient and category search parameters:
        // GET
        // [base]/Observation?patient=[reference]&category=http://terminology.hl7.org/CodeSystem/observation-category|laboratory
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://terminology.hl7.org/CodeSystem/observation-category|laboratory");
        parameters.searchParam("date", "ge2005");
        parameters.searchParam("date", "lt2006");
        FHIRResponse response = client.search(Observation.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, observationId1);
        assertContainsIds(bundle, observationId2);
        assertContainsIds(bundle, observationId3);
    }

    @Test
    public void testSearchForPatientAndCategoryAndDateAndStatus() throws Exception {
        // SHOULD support searching using the combination of the patient and category and status search parameters:
        // including support for composite OR search on status (e.g.status={system|}[code],{system|}[code],...)
        // GET
        // [base]/Observation?patient=[reference]&category=http://terminology.hl7.org/CodeSystem/observation-category|laboratory&status={system|}[code]{,{system|}[code],...}
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://terminology.hl7.org/CodeSystem/observation-category|laboratory");
        parameters.searchParam("date", "ge2005");
        parameters.searchParam("date", "lt2006");
        parameters.searchParam("status", "final");

        FHIRResponse response = client.search(Observation.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, observationId1);
        assertContainsIds(bundle, observationId2);
        assertContainsIds(bundle, observationId3);
    }

    @Test
    public void testSearchForPatientAndMultipleCodesAndDate() throws Exception {
        // SHOULD support searching using the combination of the patient and code and date search parameters:
        // including optional support for composite OR search on code (e.g.code={system|}[code],{system|}[code],...)
        // including support for these date comparators: gt,lt,ge,le
        // including optional support for composite AND search on date (e.g.date=[date]&date=[date]]&...)
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("code", "http://loinc.org|5811-5,1975-2");
        parameters.searchParam("date", "2005-07-05");

        FHIRResponse response = client.search(Observation.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, observationId1);
        assertDoesNotContainsIds(bundle, observationId2);
        assertDoesNotContainsIds(bundle, observationId3);
    }
}