/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.config;

import net.sovrinhealth.fhir.core.TenantIdProvider;

/**
 * A tenant id provider that gets the tenant id from the request context
 */
public class FHIRRequestContextTenantIdProvider implements TenantIdProvider {
    @Override
    public String getTenantId() {
        return FHIRRequestContext.get().getTenantId();
    }
}
