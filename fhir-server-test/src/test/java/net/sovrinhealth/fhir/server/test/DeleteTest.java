/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.client.FHIRParameters;
import net.sovrinhealth.fhir.client.FHIRResponse;
import net.sovrinhealth.fhir.core.FHIRConstants;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry.Request;
import net.sovrinhealth.fhir.model.resource.MedicationAdministration;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.test.TestUtil;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.BundleType;
import net.sovrinhealth.fhir.model.type.code.HTTPVerb;
import net.sovrinhealth.fhir.model.util.ModelSupport;

/**
 * This class tests delete interactions.
 */
public class DeleteTest extends FHIRServerTestBase {

    private String deletedId = null;
    private String deletedType = null;
    private MedicationAdministration deletedResource = null;
    private List<String> patientIds = null;
    private String uniqueFamilyName = null;
    private boolean deleteSupported = false;

    /**
     * Retrieve the server's conformance statement to determine the status of certain runtime options.
     *
     * @throws Exception
     */
    @BeforeClass
    public void retrieveConfig() throws Exception {
        deleteSupported = isDeleteSupported();
        System.out.println("Delete operation supported?: " + Boolean.valueOf(deleteSupported).toString());
    }

    @Test
    public void testCreateNewResource() throws Exception {
        // Create a MedicationAdministration resource.
        MedicationAdministration ma = TestUtil.readLocalResource("MedicationAdministration.json");
        FHIRResponse response = client.create(ma);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.CREATED.getStatusCode());

        // Obtain the resource type and id from the location string.
        String[] locationTokens = response.parseLocation(response.getLocation());
        deletedType = locationTokens[0];
        deletedId = locationTokens[1];

        // Make sure we can read back the resource.
        response = client.read(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        deletedResource = response.getResource(MedicationAdministration.class);
        assertNotNull(deletedResource);
    }

    @Test(dependsOnMethods = {"testCreateNewResource"})
    public void testDeleteNewResource() throws Exception {
        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.delete(deletedType, deletedId);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
            assertNotNull(response.getETag());
            assertEquals("W/\"2\"", response.getETag());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }

    @Test(dependsOnMethods = {"testDeleteNewResource"})
    public void testReadDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.read(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.GONE.getStatusCode());
    }

    @Test(dependsOnMethods = {"testDeleteNewResource"})
    public void testVreadDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.vread(deletedType, deletedId, "2");
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.GONE.getStatusCode());
    }

    @Test(dependsOnMethods = {"testDeleteNewResource"})
    public void testDeleteDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.delete(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        assertNotNull(response.getETag());
        assertNotNull(response.getResource(OperationOutcome.class));
        assertEquals("W/\"2\"", response.getETag());
    }

    /**
     * Ensure we get back a 200 OK for deleting a resource with an invalid id
     * @throws Exception
     */
    @Test()
    public void testDeleteInvalidResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        FHIRResponse response = client.delete(MedicationAdministration.class.getSimpleName(), "invalid-resource-id-testDeleteInvalidResource");
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
    }


    @Test(dependsOnMethods = {"testDeleteDeletedResource"})
    public void testHistory1() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.history(deletedType, deletedId, null);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertNotNull(bundle.getEntry());
        assertEquals(2, bundle.getEntry().size());
        assertBundleEntry(bundle.getEntry().get(0), deletedType + "/" + deletedId, "2", HTTPVerb.Value.DELETE);
        assertBundleEntry(bundle.getEntry().get(1), deletedType, "1", HTTPVerb.Value.POST);
    }

    private void assertBundleEntry(Bundle.Entry entry, String expectedURL, String expectedVersionId, HTTPVerb.Value expectedMethod) throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(entry);
        Bundle.Entry.Request request = entry.getRequest();
        assertNotNull(request);
        assertEquals(expectedURL, request.getUrl().getValue());
        assertEquals(expectedMethod.toString(), request.getMethod().getValue());

        // The the bundle entry is for a DELETE, we don't get a resource so need
        // to do a different check
        if (expectedMethod == HTTPVerb.Value.DELETE) {
            assertEquals("W/\"" + expectedVersionId + "\"", entry.getResponse().getEtag().getValue());
            assertNull(entry.getResource());
        } else {
            Resource rc = entry.getResource();
            assertNotNull(rc);
            MedicationAdministration ma = (MedicationAdministration) rc;
            String actualVersionId = ma.getMeta().getVersionId().getValue();
            assertEquals(expectedVersionId, actualVersionId);
        }
    }

    @Test(dependsOnMethods = {"testHistory1"})
    public void testUndeleteDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.update(deletedResource);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.CREATED.getStatusCode());
    }

    @Test(dependsOnMethods = {"testUndeleteDeletedResource"})
    public void testReadUndeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.read(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
    }

    @Test(dependsOnMethods = {"testReadUndeletedResource"})
    public void testVreadUndeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.vread(deletedType, deletedId, "3");
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
    }

    @Test(dependsOnMethods = {"testVreadUndeletedResource"})
    public void testHistory2() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.history(deletedType, deletedId, null);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertNotNull(bundle.getEntry());
        assertEquals(3, bundle.getEntry().size());
        assertBundleEntry(bundle.getEntry().get(0), deletedType + "/" + deletedId, "3", HTTPVerb.Value.PUT);
        assertBundleEntry(bundle.getEntry().get(1), deletedType + "/" + deletedId, "2", HTTPVerb.Value.DELETE);
        assertBundleEntry(bundle.getEntry().get(2), deletedType, "1", HTTPVerb.Value.POST);
    }

    @Test(dependsOnMethods = {"testHistory2"})
    public void testDeleteUndeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.delete(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
    }

    @Test(dependsOnMethods = {"testDeleteUndeletedResource"})
    public void testHistory3() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.history(deletedType, deletedId, null);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertNotNull(bundle.getEntry());
        assertEquals(4, bundle.getEntry().size());
        assertBundleEntry(bundle.getEntry().get(0), deletedType + "/" + deletedId, "4", HTTPVerb.Value.DELETE);
        assertBundleEntry(bundle.getEntry().get(1), deletedType + "/" + deletedId, "3", HTTPVerb.Value.PUT);
        assertBundleEntry(bundle.getEntry().get(2), deletedType + "/" + deletedId, "2", HTTPVerb.Value.DELETE);
        assertBundleEntry(bundle.getEntry().get(3), deletedType, "1", HTTPVerb.Value.POST);
    }

    @Test
    public void testSearch1() throws Exception {
        if (!deleteSupported) {
            return;
        }

        // Create a collection of resources then perform searches and verify that deleted
        // resources are not included in search results.
        FHIRResponse response;
        uniqueFamilyName = UUID.randomUUID().toString();
        patientIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // Read in the resource template.
            Patient patient = TestUtil.readLocalResource("Patient_MookieBetts.json");

            // Add the uniqueFamily name
            patient = setUniqueFamilyName(patient, uniqueFamilyName);

            response = client.create(patient);
            assertResponse(response.getResponse(), Response.Status.CREATED.getStatusCode());
            String[] tokens = response.parseLocation(response.getLocation());
            patientIds.add(tokens[1]);
        }

        FHIRParameters parameters = new FHIRParameters().searchParam("name", uniqueFamilyName);
        response = client.search("Patient", parameters);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle searchResults = response.getResource(Bundle.class);
        assertEquals(10, searchResults.getEntry().size());
    }

    @Test(dependsOnMethods = {"testSearch1"})
    public void testSearch2() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(uniqueFamilyName);
        assertNotNull(patientIds);
        FHIRResponse response;

        // Delete a couple of resources created by the search test, then re-invoke the search.
        response = client.delete("Patient", patientIds.get(3));
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());

        response = client.delete("Patient", patientIds.get(7));
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());

        FHIRParameters parameters = new FHIRParameters().searchParam("name", uniqueFamilyName);
        response = client.search("Patient", parameters);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle searchResults = response.getResource(Bundle.class);
        assertEquals(8, searchResults.getEntry().size());
    }

    @Test(dependsOnMethods = {"testSearch2"})
    public void testSearch3() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(uniqueFamilyName);
        assertNotNull(patientIds);
        FHIRResponse response;

        // Delete 3 more patients.
        response = client.delete("Patient", patientIds.get(2));
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());

        response = client.delete("Patient", patientIds.get(8));
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());

        response = client.delete("Patient", patientIds.get(9));
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());

        FHIRParameters parameters = new FHIRParameters().searchParam("name", uniqueFamilyName);
        response = client.search("Patient", parameters);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle searchResults = response.getResource(Bundle.class);
        assertEquals(5, searchResults.getEntry().size());
    }

    @Test
    public void testConditionalDeleteNoResource() throws Exception {
        String fakePatientRef = "Patient/" + UUID.randomUUID().toString();
        String obsId = UUID.randomUUID().toString();
        Observation obs = TestUtil.readLocalResource("Observation1.json");

        obs = obs.toBuilder().id(obsId).subject(Reference.builder().reference(string(fakePatientRef)).build()).build();

        // This conditional delete should find no matches, so we should get back a 200 OK.
        FHIRParameters query = new FHIRParameters().searchParam("_id", obsId);
        FHIRResponse response = client.conditionalDelete("Observation", query);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }

    @Test
    public void testConditionalDeleteOneResource() throws Exception {
        String fakePatientRef = "Patient/" + UUID.randomUUID().toString();
        String obsId = UUID.randomUUID().toString();
        Observation obs = TestUtil.readLocalResource("Observation1.json");

        obs = obs.toBuilder().id(obsId).subject(Reference.builder().reference(string(fakePatientRef)).build()).build();

        // Create an Observation (using update for create) so that we can test conditional delete.
        FHIRResponse response = client.update(obs);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.CREATED.getStatusCode());


        FHIRParameters query = new FHIRParameters().searchParam("_id", obsId);
        response = client.conditionalDelete("Observation", query);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
            assertNotNull(response.getETag());
            assertEquals("W/\"2\"", response.getETag());
            // Check for resourceId in details message
            OperationOutcome operationOutcome = response.getResource(OperationOutcome.class);
            assertTrue(operationOutcome.getIssue().get(0).getDetails().getText().getValue().contains(obsId));
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }

    @Test
    public void testConditionalDeleteTenResource() throws Exception {
        final String TESTNAME = "testConditionalDeleteTwoResource";

        // Create 2 observations with the same tag (in a single request)
        Observation obs = TestUtil.getMinimalResource(Observation.class);
        obs = obs.toBuilder()
                .meta(Meta.builder()
                    .tag(Coding.builder().code(Code.of(TESTNAME)).build())
                    .build())
                .build();
        Bundle observations = buildRequestBundle(FHIRConstants.FHIR_CONDITIONAL_DELETE_MAX_NUMBER_DEFAULT, obs);
        client.batch(observations);

        // If matches <= FHIRConstants.FHIR_CONDITIONAL_DELETE_MAX_NUMBER_DEFAULT, then result in a 204 status code.
        FHIRParameters multipleMatches = new FHIRParameters().searchParam("_tag", TESTNAME);
        FHIRResponse response = client.conditionalDelete("Observation", multipleMatches);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Status.OK.getStatusCode());
            assertNotNull(response.getResource(OperationOutcome.class));
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }

    @Test
    public void testConditionalDeleteElevenResource() throws Exception {
        final String TESTNAME = "testConditionalDeleteElevenResource";

        // Create 11 observations with the same tag (in a single request)
        Observation obs = TestUtil.getMinimalResource(Observation.class);
        obs = obs.toBuilder()
                .meta(Meta.builder()
                    .tag(Coding.builder().code(Code.of(TESTNAME)).build())
                    .build())
                .build();
        Bundle observations = buildRequestBundle(FHIRConstants.FHIR_CONDITIONAL_DELETE_MAX_NUMBER_DEFAULT + 1, obs);
        client.batch(observations);

        // If matches > FHIRConstants.FHIR_CONDITIONAL_DELETE_MAX_NUMBER_DEFAULT, then result in a 412 status code.
        FHIRParameters multipleMatches = new FHIRParameters().searchParam("_tag", TESTNAME);
        FHIRResponse response = client.conditionalDelete("Observation", multipleMatches);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response, Status.PRECONDITION_FAILED.getStatusCode());
            assertExceptionOperationOutcome(response.getResponse().readEntity(OperationOutcome.class),
                    "The search criteria specified for a conditional delete operation returned too many matches");
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }

    private Bundle buildRequestBundle(int numberOfEntries, Resource rsc) {
        Entry entry = Entry.builder()
                .request(Request.builder()
                    .method(HTTPVerb.POST)
                    .url(Uri.of(ModelSupport.getTypeName(rsc.getClass())))
                    .build())
                .resource(rsc)
                .build();
        Bundle observations = Bundle.builder()
                .type(BundleType.BATCH)
                .entry(Collections.nCopies(numberOfEntries, entry))
                .build();
        return observations;
    }

    public void testConditionalDeleteInvalidSearch() throws Exception {
        String fakePatientRef = "Patient/" + UUID.randomUUID().toString();
        String obsId = UUID.randomUUID().toString();
        Observation obs = TestUtil.readLocalResource("Observation1.json");

        obs = obs.toBuilder().id(obsId).subject(Reference.builder().reference(string(fakePatientRef)).build()).build();

        // An invalid search should result in a 400 status code.
        FHIRParameters badSearch = new FHIRParameters().searchParam("invalid:search", "foo");
        FHIRResponse response = client.conditionalUpdate(obs, badSearch);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.BAD_REQUEST.getStatusCode());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }
}
