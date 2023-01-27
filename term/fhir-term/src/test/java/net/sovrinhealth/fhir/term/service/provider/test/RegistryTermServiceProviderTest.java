/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.service.provider.test;

import net.sovrinhealth.fhir.term.service.provider.RegistryTermServiceProvider;
import net.sovrinhealth.fhir.term.spi.FHIRTermServiceProvider;

public class RegistryTermServiceProviderTest extends FHIRTermServiceProviderTest {
    @Override
    public FHIRTermServiceProvider createProvider() throws Exception {
        return new RegistryTermServiceProvider();
    }
}
