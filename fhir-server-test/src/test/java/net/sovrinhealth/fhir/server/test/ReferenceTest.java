/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test;

import static net.sovrinhealth.fhir.model.type.String.string;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.model.config.FHIRModelConfig;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.test.TestUtil;
import net.sovrinhealth.fhir.model.type.Reference;

/**
 * This class contains tests for reference checking.
 */
public class ReferenceTest extends FHIRServerTestBase {

    @Test(groups = { "server-reference" })
    public void testReferenceWithNoResourceType() throws Exception {
        WebTarget target = getWebTarget();
        Patient patient = TestUtil.readLocalResource("Patient_JohnDoe.json");

        boolean originalSetting = FHIRModelConfig.getCheckReferenceTypes();
        try {
            // Temporarily set setCheckReferenceTypes to false so reference type checking is skipped
            FHIRModelConfig.setCheckReferenceTypes(false);
            patient = patient.toBuilder()
                    .managingOrganization(Reference.builder().reference(string("1234")).build())
                    .build();
        } finally {
            FHIRModelConfig.setCheckReferenceTypes(originalSetting);
        }

        // Request should fail since the server will perform the reference type checking
        Entity<Patient> entity = Entity.entity(patient, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Patient").request().post(entity, Response.class);
        assertResponse(response, Response.Status.BAD_REQUEST.getStatusCode());
    }
}
