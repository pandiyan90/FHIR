/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.resources.filters;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.UriInfo;

import net.sovrinhealth.fhir.config.FHIRConfigHelper;
import net.sovrinhealth.fhir.config.FHIRRequestContext;

/**
 * Replaces the Base URL in the OriginalRequestURI
 */
public class OriginalRequestFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(OriginalRequestFilter.class.getName());

    public static final String PROPERTY_EXTERNAL_BASE_URL = "fhirServer/core/externalBaseUrl";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo info = requestContext.getUriInfo();

        // This will never be null
        FHIRRequestContext ctx = FHIRRequestContext.get();
        String baseUrl = FHIRConfigHelper.getStringProperty(PROPERTY_EXTERNAL_BASE_URL, null);
        if (baseUrl != null) {
            StringBuilder originalRequestUri = new StringBuilder();
            originalRequestUri.append(baseUrl);
            if (!info.getPath().equals("/")) {
                originalRequestUri
                    .append("/")
                    .append(info.getPath());
            }

            // Conditionally add the Query
            String rawQuery = info.getRequestUri().getRawQuery();
            if (rawQuery != null) {
                originalRequestUri
                        .append("?")
                        .append(rawQuery);
            }
            ctx.setOriginalRequestUri(originalRequestUri.toString());
            LOG.fine(() ->  "originalRequestUri override [" + originalRequestUri + "]");
        }
    }
}