/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.ig.us.spl;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class FHIRRegistryTest {
    @Test
    public void testRegistry() {
        StructureDefinition definition =
                FHIRRegistry.getInstance()
                    .getResource("http://hl7.org/fhir/us/spl/StructureDefinition/DualSubmissionProvenance",
                        StructureDefinition.class);
        Assert.assertNotNull(definition);
    }
}