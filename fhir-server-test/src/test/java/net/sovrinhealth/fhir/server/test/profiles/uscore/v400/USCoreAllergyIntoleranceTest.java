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
import net.sovrinhealth.fhir.model.resource.AllergyIntolerance;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Provenance;
import net.sovrinhealth.fhir.model.resource.Provenance.Agent;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.Xhtml;
import net.sovrinhealth.fhir.model.type.code.NarrativeStatus;
import net.sovrinhealth.fhir.server.test.profiles.ProfilesTestBase.ProfilesTestBaseV2;

/**
 * Tests the US Core 4.0.0 Profile with AllergyIntolerance and a related Provenance.
 * https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-provenance.html
 */
public class USCoreAllergyIntoleranceTest extends ProfilesTestBaseV2 {

    private String allergyIntoleranceIdActive = null;
    private String allergyIntoleranceIdInactive = null;
    private String allergyIntoleranceIdResolved = null;
    private String provenanceId = null;

    @Override
    public List<String> getRequiredProfiles() {
        return Arrays.asList(
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-allergyintolerance|4.0.0",
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient|4.0.0",
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-provenance|4.0.0");
    }

    public void loadAllergyIntoleranceActive() throws Exception {
        String resource = "AllergyIntolerance-example.json";
        String cls = "AllergyIntolerance";
        allergyIntoleranceIdActive = buildAndAssertOnResourceForUsCore(cls, "400", resource);
    }

    public void loadAllergyIntoleranceInactive() throws Exception {
        String resource = "AllergyIntolerance-example.json";

        AllergyIntolerance r = USCoreExamplesUtil.readLocalJSONResource("400", resource);
        // @formatter:off
        CodeableConcept allergyIntoleranceClinicalStatusCodes = CodeableConcept.builder()
                .coding(Coding.builder()
                    .code(Code.of("inactive"))
                    .display(net.sovrinhealth.fhir.model.type.String.of("Inactive"))
                    .system(Uri.of("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical"))
                    .build())
                .build();
        // @formatter:on
        r = r.toBuilder().clinicalStatus(allergyIntoleranceClinicalStatusCodes).build();

        allergyIntoleranceIdInactive = createResourceAndReturnTheLogicalId("AllergyIntolerance", r);
    }

    public void loadAllergyIntoleranceResolved() throws Exception {
        String resource = "AllergyIntolerance-example.json";

        AllergyIntolerance r = USCoreExamplesUtil.readLocalJSONResource("400", resource);
        // @formatter:off
        CodeableConcept allergyIntoleranceClinicalStatusCodes = CodeableConcept.builder()
                .coding(Coding.builder()
                    .code(Code.of("resolved"))
                    .display(net.sovrinhealth.fhir.model.type.String.of("Resolved"))
                    .system(Uri.of("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical"))
                    .build())
                .build();
        // @formatter:on
        r = r.toBuilder().clinicalStatus(allergyIntoleranceClinicalStatusCodes).build();

        allergyIntoleranceIdResolved = createResourceAndReturnTheLogicalId("AllergyIntolerance", r);
    }

    public void loadProvenanceForAllergyIntoleranceIdActive() throws Exception {
        // Build the Provenance
        Canonical profile = Canonical.of("http://hl7.org/fhir/us/core/StructureDefinition/us-core-provenance", "4.0.0");
        Meta meta = Meta.builder().profile(profile).build();

        // @formatter:off
        CodeableConcept type = CodeableConcept.builder()
                .coding(Coding.builder()
                    .code(Code.of("author"))
                    .display(net.sovrinhealth.fhir.model.type.String.of("Author"))
                    .system(Uri.of("http://terminology.hl7.org/CodeSystem/provenance-participant-type"))
                    .build())
                .build();
        // @formatter:on

        Provenance provenance =
                Provenance.builder()
                    .meta(meta)
                    .text(Narrative.builder()
                            .div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">loaded from the datastore</div>"))
                            .status(NarrativeStatus.GENERATED).build())
                    .target(Reference.builder()
                        .reference(net.sovrinhealth.fhir.model.type.String.of("AllergyIntolerance/" + allergyIntoleranceIdActive)).build())
                    .recorded(Instant.now())
                    .agent(Agent.builder()
                        .type(type)
                        .who(Reference.builder()
                            .reference(net.sovrinhealth.fhir.model.type.String.of("Practitioner/practitioner-1")).build())
                        .onBehalfOf(Reference.builder()
                            .reference(net.sovrinhealth.fhir.model.type.String.of("Organization/saint-luke-w-endpoint"))
                            .build())
                        .build())
                    .build();

        provenanceId = createResourceAndReturnTheLogicalId("Provenance", provenance);
    }

    // Load Resources
    @Override
    public void loadResources() throws Exception {
        loadAllergyIntoleranceActive();
        loadAllergyIntoleranceInactive();
        loadAllergyIntoleranceResolved();
        loadProvenanceForAllergyIntoleranceIdActive();
    }

    @Test
    public void testSearchForAllAllergiesForAPatient() throws Exception {
        // SHALL support searching for all allergies for a patient using the patient search parameter
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html#mandatory-search-parameters
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdResolved);
        assertContainsIds(bundle, allergyIntoleranceIdActive);
        assertContainsIds(bundle, allergyIntoleranceIdInactive);
    }

    @Test
    public void testSearchForAllAllergiesForAPatientByInactiveStatusWithSystem() throws Exception {
        // SHOULD support searching using the combination of the patient and clinical-status search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html#optional-search-parameters
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("clinical-status", "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical|inactive");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdInactive);
    }

    @Test
    public void testSearchForAllAllergiesForAPatientByResolvedStatusWithSystem() throws Exception {
        // SHOULD support searching using the combination of the patient and clinical-status search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html#optional-search-parameters
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("clinical-status", "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical|resolved");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdResolved);
    }

    @Test
    public void testSearchForAllAllergiesForAPatientByActiveStatusWithSystem() throws Exception {
        // SHOULD support searching using the combination of the patient and clinical-status search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html#optional-search-parameters
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("clinical-status", "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical|active");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdActive);
    }

    @Test
    public void testSearchForAllAllergiesForAPatientByInactiveStatusWithoutSystem() throws Exception {
        // SHOULD support searching using the combination of the patient and clinical-status search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html#optional-search-parameters
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("clinical-status", "inactive");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdInactive);
    }

    @Test
    public void testSearchForAllAllergiesForAPatientByResolvedStatusWithoutSystem() throws Exception {
        // SHOULD support searching using the combination of the patient and clinical-status search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html#optional-search-parameters
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("clinical-status", "resolved");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdResolved);
    }

    @Test
    public void testSearchForAllAllergiesForAPatientByActiveStatusWithoutSystem() throws Exception {
        // SHOULD support searching using the combination of the patient and clinical-status search parameters
        // http://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html#optional-search-parameters
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("clinical-status", "active");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdActive);
    }

    @Test
    public void testSearchForAllAllergiesForAPatientByActiveStatusWithoutSystemAndRevinclude() throws Exception {
        // http://example.org/fhir/AllergyIntolerance?_revinclude=Provenance%3Atarget&patient=Examples
        FHIRParameters parameters = new FHIRParameters();
        parameters.searchParam("patient", "Patient/example");
        parameters.searchParam("_revinclude", "Provenance:target");
        parameters.searchParam("_sort", "-_lastUpdated");
        FHIRResponse response = client.search(AllergyIntolerance.class.getSimpleName(), parameters);
        assertSearchResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertBaseBundleNotEmpty(bundle);
        assertContainsIds(bundle, allergyIntoleranceIdActive);
        assertContainsIds(bundle, provenanceId);
    }
}