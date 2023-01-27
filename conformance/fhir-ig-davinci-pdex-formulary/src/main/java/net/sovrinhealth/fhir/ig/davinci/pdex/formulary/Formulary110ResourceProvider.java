/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.davinci.pdex.formulary;

import net.sovrinhealth.fhir.registry.util.PackageRegistryResourceProvider;

public class Formulary110ResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "hl7.fhir.us.davinci-pdex-formulary.110";
    }
}
