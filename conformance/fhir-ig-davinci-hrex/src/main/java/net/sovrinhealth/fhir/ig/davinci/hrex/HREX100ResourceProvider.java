/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.hrex;

import net.sovrinhealth.fhir.registry.util.PackageRegistryResourceProvider;

public class HREX100ResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "hl7.fhir.us.davinci-hrex.100";
    }
}