/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.bucket.client;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.generator.exception.FHIRGeneratorException;
import net.sovrinhealth.fhir.model.resource.Resource;

/**
 * Utilities for working with the FHIR client
 */
public class FHIRBucketClientUtil {
    
    
    /**
     * Render the resource as a string
     * @param resource
     * @return
     */
    public static String resourceToString(Resource resource) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
        try {
            FHIRGenerator.generator(Format.JSON, false).generate(resource, os);
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (FHIRGeneratorException e) {
            throw new IllegalStateException(e);
        }
    }

}
