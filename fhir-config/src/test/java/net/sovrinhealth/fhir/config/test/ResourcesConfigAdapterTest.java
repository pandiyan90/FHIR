/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.config.test;

import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.config.Interaction;
import net.sovrinhealth.fhir.config.PropertyGroup;
import net.sovrinhealth.fhir.config.ResourcesConfigAdapter;
import net.sovrinhealth.fhir.core.FHIRVersionParam;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class ResourcesConfigAdapterTest {
    @Test
    public void testGetSupportedResourceTypes() throws Exception {
        JsonObject json = Json.createObjectBuilder().build();
        PropertyGroup pg = new PropertyGroup(json);
        ResourcesConfigAdapter resourcesConfigAdapter = new ResourcesConfigAdapter(pg, FHIRVersionParam.VERSION_40);

        Set<String> supportedResourceTypes = resourcesConfigAdapter.getSupportedResourceTypes();
        assertEquals(supportedResourceTypes.size(), 126);

        System.out.println(supportedResourceTypes);

        for (Interaction interaction : Interaction.values()) {
            supportedResourceTypes = resourcesConfigAdapter.getSupportedResourceTypes(interaction);
            assertEquals(supportedResourceTypes.size(), 126);
        }
    }

    @Test
    public void testGetSupportedResourceTypes_r4b() throws Exception {
        JsonObject json = Json.createObjectBuilder().build();
        PropertyGroup pg = new PropertyGroup(json);
        ResourcesConfigAdapter resourcesConfigAdapter = new ResourcesConfigAdapter(pg, FHIRVersionParam.VERSION_43);

        Set<String> supportedResourceTypes = resourcesConfigAdapter.getSupportedResourceTypes();
        assertEquals(supportedResourceTypes.size(), 141);

        for (Interaction interaction : Interaction.values()) {
            supportedResourceTypes = resourcesConfigAdapter.getSupportedResourceTypes(interaction);
            assertEquals(supportedResourceTypes.size(), 141);
        }
    }
}
