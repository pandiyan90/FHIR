/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.us.core;

import net.sovrinhealth.fhir.registry.util.PackageRegistryResourceProvider;

public class USCore501ResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "hl7.fhir.us.core.501";
    }
}
