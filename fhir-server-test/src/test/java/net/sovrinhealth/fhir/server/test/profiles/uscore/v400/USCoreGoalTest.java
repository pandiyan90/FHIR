/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test.profiles.uscore.v400;

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
import net.sovrinhealth.fhir.model.resource.Goal;
import net.sovrinhealth.fhir.server.test.profiles.ProfilesTestBase.ProfilesTestBaseV2;

/**
 * Tests the US Core 4.0.0 Profile with Goal.
 * https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-goal.html
 */
public class USCoreGoalTest extends ProfilesTestBaseV2 {

    private String goalId1 = null;

    @Override
    public List<String> getRequiredProfiles() {
        return Arrays.asList("http://hl7.org/fhir/us/core/StructureDefinition/us-core-goal|4.0.0");
    }

    @Override
    public void loadResources() throws Exception {
        String resource = "Goal-goal-1.json";
        Goal goal = USCoreExamplesUtil.readLocalJSONResource("400", resource);
        goalId1 = createResourceAndReturnTheLogicalId("Goal", goal);
    }

    @Test
    public void testSearchForPatient() throws Exception {
        // SHALL support searching for all goals for a patient using the patient search parameter:
        // GET [base]/Goal?patient=[reference]
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(Goal.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, goalId1);
    }

    @Test
    public void testSearchForPatientLifecycleStatus() throws Exception {
        // SHOULD support searching using the combination of the patient and lifecycle-status search parameters:
        // GET [base]/Goal?patient=[reference]&lifecycle-status={system|}[code]
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("lifecycle-status", "active");
        FHIRResponse response = client.search(Goal.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, goalId1);
    }

    @Test
    public void testSearchForPatientTargetDate() throws Exception {
        // SHOULD support searching using the combination of the patient and target-date search parameters:
        // including support for these target-date comparators: gt,lt,ge,le
        // including optional support for composite AND search on target-date
        // (e.g.target-date=[date]&target-date=[date]]&...)
        // GET [base]/Goal?patient=[reference]&target-date={gt|lt|ge|le}[date]{&target-date={gt|lt|ge|le}[date]&...}
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("target-date", "ge2016");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(Goal.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().size() >= 1);
        assertContainsIds(bundle, goalId1);
    }
}