/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import net.sovrinhealth.fhir.registry.util.PackageRegistryResourceProvider;

public class FHIRValidationTestResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "fhir.validation.test";
    }
}
