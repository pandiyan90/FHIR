/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test;

import static org.testng.AssertJUnit.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.test.TestUtil;

/**
 * Tests for Valid Tenant Configuration
 */
public class PersistenceLayerServerTest extends FHIRServerTestBase {

    @Test(groups = { "server-persistence-layer-tests" })
    public void testPersistenceLayerResponseBadTenant() throws Exception {
        WebTarget target = getWebTarget();

        // Build a new Patient and then call the 'create' API.
        Patient patient = TestUtil.readLocalResource("Patient_JohnDoe.json");
        Entity<Patient> entity = Entity.entity(patient, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Patient").request().header("X-FHIR-TENANT-ID", "IAMNOTAREALTENANT").post(entity, Response.class);
        assertResponse(response, Response.Status.BAD_REQUEST.getStatusCode());
        OperationOutcome responsePatient = response.readEntity(OperationOutcome.class);
        assertEquals(responsePatient.getIssue().get(0).getDetails().getText().getValue(),
            "FHIRException: Tenant configuration does not exist: IAMNOTAREALTENANT");
    }
}
