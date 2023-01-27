/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.test.examples;

import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import net.sovrinhealth.fhir.config.FHIRConfiguration;
import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.spec.test.DriverMetrics;
import net.sovrinhealth.fhir.model.spec.test.IExampleProcessor;
import net.sovrinhealth.fhir.model.util.SaltHash;
import net.sovrinhealth.fhir.model.util.test.ResourceComparatorVisitor;
import net.sovrinhealth.fhir.model.visitor.ResourceFingerprintVisitor;
import net.sovrinhealth.fhir.server.test.FHIRServerTestBase;

/**
 * Exercises the FHIR REST API. Create is called for each resource, then the
 * resource is retrieved with a GET and its fingerprint is compared with the
 * original to verify its integrity.
 *
 * Fingerprinting is used for the comparison because the FHIR server adds
 * additional (meta) content, which is ignored when the fingerprint is
 * computed.
 */
public class ExampleRequestProcessor implements IExampleProcessor {
    private static final Logger logger = Logger.getLogger(ExampleRequestProcessor.class.getName());

    private static final int MAX_TRIES = 2;

    private final FHIRServerTestBase base;

    // The id of the tenant to use in each FHIR server request
    private final String tenantId;

    // Some simple intrumentation (optional)
    private final DriverMetrics metrics;

    // Read (GET) multiplier for more interesting (simple) performance checks.
    private final int readIterations;

    // Target (once configured) can be shared amongst threads
    private final WebTarget target;

    /**
     * Public constructor
     * @param base
     * @param tenantId
     * @param metrics
     * @param readIterations
     */
    public ExampleRequestProcessor(FHIRServerTestBase base, String tenantId, DriverMetrics metrics, int readIterations) {
        this.base = base;
        this.tenantId = tenantId;
        this.metrics = metrics;
        this.readIterations = readIterations;
        this.target = base.getWebTarget();
    }

    @Override
    public void process(String jsonFile, Resource resource) throws Exception {
        String resourceTypeName = resource.getClass().getSimpleName();

        // clear the id value from the example so we can assign our own
        resource = resource.toBuilder().id(null).build();

        // Compute a fingerprint which can be used to check the consistency of the
        // resource we read back from FHIR
        ResourceFingerprintVisitor v = new ResourceFingerprintVisitor();
        resource.accept(resourceTypeName, v);
        SaltHash baseline = v.getSaltAndHash();

        // Build a new resource and then call the 'create' API.
        long postStart = System.nanoTime();
        // Explicitly use fhirVersion 4.3 so that we can use all the R4B resource types
        Entity<Resource> entity = Entity.entity(resource, FHIRMediaType.APPLICATION_FHIR_43_JSON_TYPE);

        int attempt = 1;
        Response response;
        do {
            response = target.path(resourceTypeName).request()
                    .header(FHIRConfiguration.DEFAULT_TENANT_ID_HEADER_NAME, tenantId)
                    .post(entity, Response.class);

            if (response.getStatus() == 500) {
                logger.warning("Unexpected server error for '" + jsonFile + "' POST "
                        + "on attempt " + attempt + " of " + MAX_TRIES);
            }
        } while (response.getStatus() == 500 && attempt++ < MAX_TRIES);

        try {
            base.assertResponse(response, Response.Status.CREATED.getStatusCode());
        } catch (AssertionError x) {
            // definitely not what we were expecting, so log what the FHIR server gave us
            String msg = response.readEntity(String.class);
            logger.warning("Response body for POST: " + msg);
            throw new Exception("Unexpected response for JSON file: " + jsonFile, x);
        }
        long postEnd = System.nanoTime();
        metrics.addPostTime((postEnd - postStart) / DriverMetrics.NANOS_MS);

        // Get the logical id value.
        String logicalId = base.getLocationLogicalId(response);

        // Next, call the 'read' API to retrieve the new resource and verify it. We
        // can repeat this a number of times to help get some more useful performance numbers
        for (int i=0; i<this.readIterations; i++) {
            postEnd = System.nanoTime(); // update for each iteration
            response = target.path(resourceTypeName + "/" + logicalId)
                    .request(FHIRMediaType.APPLICATION_FHIR_43_JSON_TYPE)
                    .header(FHIRConfiguration.DEFAULT_TENANT_ID_HEADER_NAME, tenantId)
                    .get();

            try {
                base.assertResponse(response, Response.Status.OK.getStatusCode());
            } catch (AssertionError x) {
                // definitely not what we were expecting, so log what the FHIR server gave us
                String msg = response.readEntity(String.class);
                logger.warning("Response body for GET: " + msg);
                throw new Exception("Unexpected response for JSON file: " + jsonFile, x);
            }

            metrics.addGetTime((System.nanoTime() - postEnd) / DriverMetrics.NANOS_MS);
        }

        // Now...do we need some reflection here?
        Resource responseResource = response.readEntity(resource.getClass());

        // Now we can check what we sent equals what we got back (minus the meta changes)
        // making sure to seed the visitor with the same salt we used above
        ResourceFingerprintVisitor v2 = new ResourceFingerprintVisitor(baseline);
        responseResource.accept(responseResource.getClass().getSimpleName(), v2);
        SaltHash responseHash = v2.getSaltAndHash();

        if (!responseHash.equals(baseline)) {
            // Use the ResourceComparatorVisitor to provide some detail about what's different
            ResourceComparatorVisitor originals = new ResourceComparatorVisitor();
            resource.accept(resource.getClass().getSimpleName(), originals);

            ResourceComparatorVisitor others = new ResourceComparatorVisitor();
            responseResource.accept(responseResource.getClass().getSimpleName(), others);

            // Perform a bi-directional comparison of values in the maps
            ResourceComparatorVisitor.compare(originals.getValues(), others.getValues());

            // throw the error so it is handled by the test framework
            throw new Exception("The retrieved resource does not match the expected resource.");
        }
    }
}
