/*
 * (C) Copyright IBM Corp. 2018, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.search.test;

import static net.sovrinhealth.fhir.model.test.TestUtil.isResourceInResponse;
import static net.sovrinhealth.fhir.model.type.String.string;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.generator.exception.FHIRGeneratorException;
import net.sovrinhealth.fhir.model.resource.Basic;
import net.sovrinhealth.fhir.model.resource.Device;
import net.sovrinhealth.fhir.model.resource.Encounter;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Practitioner;
import net.sovrinhealth.fhir.model.resource.RelatedPerson;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.test.TestUtil;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.persistence.MultiResourceResult;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.util.FHIRPersistenceTestSupport;
import net.sovrinhealth.fhir.search.context.FHIRSearchContext;

/**
 * <a href="https://hl7.org/fhir/search.html#date">FHIR Specification: Search
 * - _id and _lastUpdated</a> Tests
 */
public abstract class AbstractSearchIdAndLastUpdatedTest extends AbstractPLSearchTest {
    private Boolean DEBUG = Boolean.FALSE;

    @Override
    protected Basic getBasicResource() throws Exception {
        return TestUtil.readExampleResource("json/basic/BasicDate.json");
    }

    private Reference buildReference(Resource resource) {
        assertNotNull(resource);
        assertNotNull(resource.getId());

        String resourceTypeName = resource.getClass().getSimpleName();
        return Reference.builder()
                .reference(string(resourceTypeName + "/" + resource.getId()))
                .build();
    }

    @Override
    protected void setTenant() throws Exception {
        FHIRRequestContext.get().setTenantId("default");

        // this might deserve its own method, but just use setTenant for now
        // since its called before creating any resources
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-4:00"));
    }

    @Test
    public void testSearchWholeSystemUsingIdAndLastUpdated() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        List<String> savedId = Collections.singletonList(savedResource.getId());

        String dateTime = savedResource.getMeta().getLastUpdated().getValue().toString();
        List<String> savedLastUpdated = Collections.singletonList(dateTime);
        queryParms.put("_id", savedId);
        queryParms.put("_lastUpdated", savedLastUpdated);

        if (DEBUG) {
            generateOutput(savedResource);
        }

        List<Resource> resources = runQueryTest(Resource.class, queryParms);
        assertNotNull(resources);
        assertEquals(resources.size(), 1, "Number of resources returned");
        assertTrue(isResourceInResponse(savedResource, resources), "Expected resource not found in the response");
    }

    @Test
    public void testSearchWholeSystemUsingIdAndLastUpdatedWithSort() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        List<String> savedId = Collections.singletonList(savedResource.getId());

        String dateTime = savedResource.getMeta().getLastUpdated().getValue().toString();
        List<String> savedLastUpdated = Collections.singletonList(dateTime);
        queryParms.put("_id", savedId);
        queryParms.put("_lastUpdated", savedLastUpdated);

        // Sort id and then lastUpdated
        queryParms.put("_sort", Collections.singletonList("_id,-_lastUpdated"));

        if (DEBUG) {
            generateOutput(savedResource);
        }

        List<Resource> resources = runQueryTest(Resource.class, queryParms);
        assertNotNull(resources);
        assertEquals(resources.size(), 1, "Number of resources returned");
        assertTrue(isResourceInResponse(savedResource, resources), "Expected resource not found in the response");
    }

    @Test
    public void testSearchWholeSystemUsingIdAndLastUpdatedResource() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        List<String> savedId = Collections.singletonList(savedResource.getId());

        String dateTime = savedResource.getMeta().getLastUpdated().getValue().toString();
        List<String> savedLastUpdated = Collections.singletonList(dateTime);
        queryParms.put("_id", savedId);
        queryParms.put("_lastUpdated", savedLastUpdated);

        if (DEBUG) {
            generateOutput(savedResource);
        }

        List<Resource> resources = runQueryTest(Basic.class, queryParms);
        assertNotNull(resources);
        assertEquals(resources.size(), 1, "Number of resources returned");
        assertTrue(isResourceInResponse(savedResource, resources), "Expected resource not found in the response");
    }

    @Test
    public void testSearchWholeSystemUsingIdAndLastUpdatedResourceWithSort() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        List<String> savedId = Collections.singletonList(savedResource.getId());

        String dateTime = savedResource.getMeta().getLastUpdated().getValue().toString();
        List<String> savedLastUpdated = Collections.singletonList(dateTime);
        queryParms.put("_id", savedId);
        queryParms.put("_lastUpdated", savedLastUpdated);

        // Sort id and then lastUpdated
        queryParms.put("_sort", Collections.singletonList("_id,-_lastUpdated"));

        if (DEBUG) {
            generateOutput(savedResource);
        }

        List<Resource> resources = runQueryTest(Basic.class, queryParms);
        assertNotNull(resources);
        assertEquals(resources.size(), 1, "Number of resources returned");
        assertTrue(isResourceInResponse(savedResource, resources), "Expected resource not found in the response");
    }

    @Test
    public void testSearchWholeSystemUsingIdAndLastUpdatedResourceWithSortGreaterThanEquals() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        List<String> savedId = Collections.singletonList(savedResource.getId());

        String dateTime = savedResource.getMeta().getLastUpdated().getValue().toString();
        List<String> savedLastUpdated = Collections.singletonList("ge" + dateTime);
        queryParms.put("_id", savedId);
        queryParms.put("_lastUpdated", savedLastUpdated);

        // Sort id and then lastUpdated
        queryParms.put("_sort", Collections.singletonList("_id,-_lastUpdated"));

        if (DEBUG) {
            generateOutput(savedResource);
        }

        List<Resource> resources = runQueryTest(Basic.class, queryParms);
        assertNotNull(resources);
        assertEquals(resources.size(), 1, "Number of resources returned");
        assertTrue(isResourceInResponse(savedResource, resources), "Expected resource not found in the response");
    }

    @Test
    public void testSearchWholeSystemUsingLastUpdatedResourceWithSortGreaterThanEquals() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();

        String dateTime = savedResource.getMeta().getLastUpdated().getValue().toString();
        List<String> savedLastUpdated = Collections.singletonList("ge" + dateTime);
        queryParms.put("_lastUpdated", savedLastUpdated);

        // Sort id and then lastUpdated
        queryParms.put("_sort", Collections.singletonList("-_lastUpdated"));

        if (DEBUG) {
            generateOutput(savedResource);
        }

        List<Resource> resources = runQueryTest(Basic.class, queryParms);
        assertNotNull(resources);
        assertEquals(resources.size(), 1, "Number of resources returned");
        assertTrue(isResourceInResponse(savedResource, resources), "Expected resource not found in the response");
    }

    @Test()
    public void testPatientCompartmentForBulkData() throws Exception {

        Patient savedPatient;
        Device savedDevice;
        Encounter savedEncounter;
        Practitioner savedPractitioner;
        RelatedPerson savedRelatedPerson;
        Observation savedObservation;

        Observation.Builder observationBuilder =
                TestUtil.getMinimalResource(Observation.class).toBuilder();

        Patient patient = TestUtil.getMinimalResource(Patient.class);
        savedPatient = FHIRPersistenceTestSupport.create(persistence, getDefaultPersistenceContext(), patient).getResource();
        observationBuilder.subject(buildReference(savedPatient));
        observationBuilder.performer(buildReference(savedPatient));

        Device device = TestUtil.getMinimalResource(Device.class);
        savedDevice = FHIRPersistenceTestSupport.create(persistence, getDefaultPersistenceContext(), device).getResource();
        observationBuilder.device(buildReference(savedDevice));

        Encounter encounter = TestUtil.getMinimalResource(Encounter.class);
        savedEncounter = FHIRPersistenceTestSupport.create(persistence, getDefaultPersistenceContext(), encounter).getResource();
        observationBuilder.encounter(buildReference(savedEncounter));

        Practitioner practitioner = TestUtil.getMinimalResource(Practitioner.class);
        savedPractitioner = FHIRPersistenceTestSupport.create(persistence, getDefaultPersistenceContext(), practitioner).getResource();
        observationBuilder.performer(buildReference(savedPractitioner));

        RelatedPerson relatedPerson = TestUtil.getMinimalResource(RelatedPerson.class);
        savedRelatedPerson = FHIRPersistenceTestSupport.create(persistence, getDefaultPersistenceContext(), relatedPerson).getResource();
        observationBuilder.performer(buildReference(savedRelatedPerson));

        savedObservation = FHIRPersistenceTestSupport.create(persistence, getDefaultPersistenceContext(), observationBuilder.build()).getResource();
        assertNotNull(savedObservation);
        assertNotNull(savedObservation.getId());
        assertNotNull(savedObservation.getMeta());
        assertNotNull(savedObservation.getMeta().getVersionId().getValue());
        assertEquals("1", savedObservation.getMeta().getVersionId().getValue());

        Map<String, List<String>> queryParms = new HashMap<String, List<String>>(2);
        String parmName = "_lastUpdated";
        queryParms.put(parmName, Collections.singletonList("ge2000"));
        queryParms.put("_sort", Collections.singletonList("_id"));

        FHIRSearchContext searchContext =
                searchHelper.parseCompartmentQueryParameters("Patient", savedPatient.getId(), Observation.class, queryParms);
        FHIRPersistenceContext persistenceContext = getPersistenceContextForSearch(searchContext);
        MultiResourceResult result = persistence.search(persistenceContext, Observation.class);
        assertNotNull(result.getResourceResults());
        assertTrue(result.getResourceResults().size() > 0);
    }

    /*
     * generates the output into a resource.
     */
    public static void generateOutput(Resource resource) {
        try (StringWriter writer = new StringWriter();) {
            FHIRGenerator.generator(Format.JSON, true).generate(resource, System.out);
            System.out.println(writer.toString());
        } catch (FHIRGeneratorException e) {
            fail("unable to generate the fhir resource to JSON", e);
        } catch (IOException e1) {
            fail("unable to generate the fhir resource to JSON (io problem) ", e1);
        }
    }
}