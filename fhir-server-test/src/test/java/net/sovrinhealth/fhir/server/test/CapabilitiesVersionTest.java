/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test;

import static net.sovrinhealth.fhir.core.FHIRMediaType.FHIR_VERSION_PARAMETER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.core.ResourceType;
import net.sovrinhealth.fhir.model.resource.CapabilityStatement;
import net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest;
import net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.Resource;
import net.sovrinhealth.fhir.model.type.code.FHIRVersion;
import net.sovrinhealth.fhir.path.exception.FHIRPathException;
import net.sovrinhealth.fhir.validation.exception.FHIRValidationException;

public class CapabilitiesVersionTest extends FHIRServerTestBase {
    private static final Set<ResourceType> R4B_ONLY_RESOURCES = new HashSet<>();
    {
        R4B_ONLY_RESOURCES.add(ResourceType.ADMINISTRABLE_PRODUCT_DEFINITION);
        R4B_ONLY_RESOURCES.add(ResourceType.CITATION);
        R4B_ONLY_RESOURCES.add(ResourceType.CLINICAL_USE_DEFINITION);
        R4B_ONLY_RESOURCES.add(ResourceType.EVIDENCE_REPORT);
        R4B_ONLY_RESOURCES.add(ResourceType.INGREDIENT);
        R4B_ONLY_RESOURCES.add(ResourceType.MANUFACTURED_ITEM_DEFINITION);
        R4B_ONLY_RESOURCES.add(ResourceType.MEDICINAL_PRODUCT_DEFINITION);
        R4B_ONLY_RESOURCES.add(ResourceType.NUTRITION_PRODUCT);
        R4B_ONLY_RESOURCES.add(ResourceType.PACKAGED_PRODUCT_DEFINITION);
        R4B_ONLY_RESOURCES.add(ResourceType.REGULATED_AUTHORIZATION);
        R4B_ONLY_RESOURCES.add(ResourceType.SUBSCRIPTION_STATUS);
        R4B_ONLY_RESOURCES.add(ResourceType.SUBSCRIPTION_TOPIC);
        R4B_ONLY_RESOURCES.add(ResourceType.SUBSTANCE_DEFINITION);
        // The following resource types existed in R4, but have breaking changes in R4B.
        // Because we only support the R4B version, we don't want to advertise these in our 4.0.1 statement.
        R4B_ONLY_RESOURCES.add(ResourceType.EVIDENCE);
        R4B_ONLY_RESOURCES.add(ResourceType.EVIDENCE_VARIABLE);
    }

    /**
     * Verify the 'metadata' API.
     */
    @Test(dataProvider = "dataMethod")
    public void testWithTenantAndFHIRVersion(String tenant, String requestFhirVersion, FHIRVersion expectedVersion) throws FHIRPathException, FHIRValidationException {
        WebTarget target = getWebTarget();
        Map<String,String> fhirVersionParameterMap = (requestFhirVersion == null) ? null : Collections.singletonMap(FHIR_VERSION_PARAMETER, requestFhirVersion);
        MediaType mediaType = new MediaType("application", FHIRMediaType.SUBTYPE_FHIR_JSON, fhirVersionParameterMap);

        Response response = target.path("metadata")
                .request(mediaType)
                .header("X-FHIR-TENANT-ID", tenant)
                .get();
        assertResponse(response, Response.Status.OK.getStatusCode());

        CapabilityStatement conf = response.readEntity(CapabilityStatement.class);
        assertNotNull(conf);
        assertEquals(conf.getFhirVersion(), expectedVersion);

        switch (conf.getFhirVersion().getValueAsEnum()) {
        case VERSION_4_0_1:
            // verify it has no "R4B-only" resource types
            for (Rest rest : conf.getRest()) {
                for (Resource resource : rest.getResource()) {
                    assertFalse(R4B_ONLY_RESOURCES.contains(resource.getType().getValueAsEnum()),
                            "unexpected resource type: " + resource.getType().getValue());
                }
            }
            break;
        case VERSION_4_3_0:
            // nothing to verify at the moment
            break;
        default:
            fail("unexpected fhirVersion: " + conf.getFhirVersion().getValue());
        }
    }

    /**
     * tenant, requestedVersion, expectedVersion
     */
    @DataProvider
    public static Object[][] dataMethod() {
        return new Object[][] {
            { "default", null, FHIRVersion.VERSION_4_0_1 },
            { "default", "4.0", FHIRVersion.VERSION_4_0_1 },
            { "default", "4.3", FHIRVersion.VERSION_4_3_0 },
            { "tenant1", null, FHIRVersion.VERSION_4_3_0 },
            { "tenant1", "4.0", FHIRVersion.VERSION_4_0_1 },
            { "tenant1", "4.3", FHIRVersion.VERSION_4_3_0 },
            { "tenant2", null, FHIRVersion.VERSION_4_0_1 },
            { "tenant2", "4.0", FHIRVersion.VERSION_4_0_1 },
            { "tenant2", "4.3", FHIRVersion.VERSION_4_3_0 }
        };
    }
}
