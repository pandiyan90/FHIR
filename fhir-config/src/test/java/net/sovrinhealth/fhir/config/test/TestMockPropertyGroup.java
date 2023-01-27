/*
 * (C) Copyright IBM Corp. 2016, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.config.test;

import jakarta.json.JsonObject;

import net.sovrinhealth.fhir.config.PropertyGroup;

public class TestMockPropertyGroup extends PropertyGroup {

    public TestMockPropertyGroup(JsonObject jsonObj, String foo) {
        super(jsonObj);
    }
}
