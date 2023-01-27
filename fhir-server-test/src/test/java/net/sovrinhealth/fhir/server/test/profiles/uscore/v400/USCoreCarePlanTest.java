/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test.profiles.uscore.v400;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.client.FHIRParameters;
import net.sovrinhealth.fhir.client.FHIRResponse;
import net.sovrinhealth.fhir.ig.us.core.tool.USCoreExamplesUtil;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.CarePlan;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.code.CarePlanStatus;
import net.sovrinhealth.fhir.server.test.profiles.ProfilesTestBase.ProfilesTestBaseV2;

/**
 * Tests the US Core 4.0.0 Profile with CarePlan.
 *
 * The specification says the following parameters should work:
 * <code>status=http://hl7.org/fhir/ValueSet/request-status|active</code>
 * It's a default binding and should work without a bound system. We only extract active, and not the default system.
 */
public class USCoreCarePlanTest extends ProfilesTestBaseV2 {
    private String carePlanId = null;

    @Override
    public List<String> getRequiredProfiles() {
        return Arrays.asList("http://hl7.org/fhir/us/core/StructureDefinition/us-core-careplan|4.0.0");
    }

    @Override
    public void loadResources() throws Exception {
        String resource = "CarePlan-colonoscopy.json";

        CarePlan carePlan = USCoreExamplesUtil.readLocalJSONResource("400", resource);
        net.sovrinhealth.fhir.model.type.Period period =
                net.sovrinhealth.fhir.model.type.Period.builder()
                    .start(DateTime.of("2019-01-01"))
                    .end(DateTime.of("2020-01-01"))
                    .build();

        // Note: The test uses ACTIVE as a CodeableConcept rather than a plain string.
        carePlan = carePlan.toBuilder().period(period).status(CarePlanStatus.ACTIVE).build();
        carePlanId = createResourceAndReturnTheLogicalId("CarePlan", carePlan);
    }

    @Test
    public void testSearchForPatientAndCategory() throws Exception {
        // SHALL support searching using the combination of the patient and category search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-careplan.html
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://hl7.org/fhir/us/core/CodeSystem/careplan-category|assess-plan");
        FHIRResponse response = client.search(CarePlan.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, carePlanId);
    }

    @Test
    public void testSearchForPatientAndCategoryAndDate() throws Exception {
        // SHOULD support searching using the combination of the patient and category and date search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-careplan.html
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://hl7.org/fhir/us/core/CodeSystem/careplan-category|assess-plan");
        parameters.searchParam("date", "gt2018");
        parameters.searchParam("date", "le2021");
        FHIRResponse response = client.search(CarePlan.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, carePlanId);
    }

    @Test
    public void testSearchForPatientAndCategoryAndDateGe() throws Exception {
        // SHOULD support searching using the combination of the patient and category and date search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-careplan.html
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://hl7.org/fhir/us/core/CodeSystem/careplan-category|assess-plan");
        parameters.searchParam("date", "ge2019");
        parameters.searchParam("date", "le2021");
        FHIRResponse response = client.search(CarePlan.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, carePlanId);
    }

    @Test
    public void testSearchForPatientAndCategoryAndStatus() throws Exception {
        // SHOULD support searching using the combination of the patient and category and status search parameters:
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-careplan.html
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://hl7.org/fhir/us/core/CodeSystem/careplan-category|assess-plan");
        parameters.searchParam("status", "active");
        FHIRResponse response = client.search(CarePlan.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, carePlanId);
    }

    // See the note at the top of this class as to why this test is disabled.
    @Test(enabled = false)
    public void testSearchForPatientAndCategoryAndStatusSystem() throws Exception {
        // SHOULD support searching using the combination of the patient and category and status search parameters:
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-careplan.html
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://hl7.org/fhir/us/core/CodeSystem/careplan-category|assess-plan");
        parameters.searchParam("status", "http://hl7.org/fhir/ValueSet/request-status|");
        FHIRResponse response = client.search(CarePlan.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, carePlanId);
    }

    @Test
    public void testSearchForPatientAndCategoryAndStatusSystemStatus() throws Exception {
        // SHOULD support searching using the combination of the patient and category and status search parameters:
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-careplan.html
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://hl7.org/fhir/us/core/CodeSystem/careplan-category|assess-plan");
        parameters.searchParam("status", "active");
        FHIRResponse response = client.search(CarePlan.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, carePlanId);
    }

    @Test
    public void testSearchForPatientAndCategoryAndStatusSystemStatusAndDate() throws Exception {
        // SHOULD support searching using the combination of the patient and category and status search parameters:
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-careplan.html
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("category", "http://hl7.org/fhir/us/core/CodeSystem/careplan-category|assess-plan");
        parameters.searchParam("status", "active");
        parameters.searchParam("date", "ge2019");
        parameters.searchParam("date", "le2021");
        FHIRResponse response = client.search(CarePlan.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, carePlanId);
    }
}