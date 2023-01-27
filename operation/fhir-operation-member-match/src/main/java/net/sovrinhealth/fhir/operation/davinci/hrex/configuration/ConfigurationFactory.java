/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.davinci.hrex.configuration;

import net.sovrinhealth.fhir.operation.davinci.hrex.configuration.impl.ConfigurationAdapterImpl;

/**
 * Factory to get a configuration
 */
public final class ConfigurationFactory {
    private ConfigurationFactory() {
        // NOP
    }

    /**
     * gets the factory
     * @return
     */
    public static ConfigurationFactory factory() {
        return new ConfigurationFactory();
    }

    /**
     * Gets the configuration adapter
     * @return
     */
    public ConfigurationAdapter getConfigurationAdapter() {
        return new ConfigurationAdapterImpl();
    }
}