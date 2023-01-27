/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.core.r4b;

import net.sovrinhealth.fhir.registry.util.PackageRegistryResourceProvider;

public class Core430ResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "hl7.fhir.core.430";
    }
}
