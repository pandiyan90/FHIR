/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.davinci.hrex.configuration.impl;

import net.sovrinhealth.fhir.config.FHIRConfigHelper;
import net.sovrinhealth.fhir.config.PropertyGroup;
import net.sovrinhealth.fhir.operation.davinci.hrex.configuration.ConfigurationAdapter;

/**
 * Grabs the Configuration Properties
 */
public class ConfigurationAdapterImpl implements ConfigurationAdapter {
    @Override
    public boolean enabled() {
        return FHIRConfigHelper.getBooleanProperty("fhirServer/operations/membermatch/enabled", true);
    }

    @Override
    public String getStrategyKey() {
        return FHIRConfigHelper.getStringProperty("fhirServer/operations/membermatch/strategy", "default");
    }

    @Override
    public PropertyGroup getExtendedStrategyPropertyGroup() {
        return FHIRConfigHelper.getPropertyGroup("fhirServer/operations/membermatch/extendedProps");
    }
}