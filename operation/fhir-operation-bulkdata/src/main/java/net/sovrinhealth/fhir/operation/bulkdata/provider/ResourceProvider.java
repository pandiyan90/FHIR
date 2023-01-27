/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata.provider;

import net.sovrinhealth.fhir.registry.util.PackageRegistryResourceProvider;

/**
 * Resource Provider for the IG Capability Statement for Bulk Data
 */
public class ResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "hl7.fhir.uv.bulkdata";
    }
}