/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.config.FHIRConfiguration;
import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.exception.FHIRException;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.operation.bulkdata.config.ConfigurationFactory;

public class ConfigurationTest {
    // Flip to TRUE to see which tenant is running during a test (or peek at the end of the test that failed).
    // if not specified, then it's the default.
    private static final Boolean DEBUG = Boolean.FALSE;

    @BeforeClass
    public void setup() {
        FHIRConfiguration.setConfigHome("target/test-classes");
    }

    @BeforeMethod
    public void startMethod(Method method) throws FHIRException {
        // Configure the request context for our search tests
        FHIRRequestContext context = FHIRRequestContext.get();

        //Facilitate the switching of tenant configurations based on method name
        String tenant = "default";
        String methodName = method.getName();
        if (methodName.contains("_tenant_")) {
            int idx = methodName.indexOf("_tenant_") + "_tenant_".length();
            tenant = methodName.substring(idx);
            if (DEBUG) {
                System.out.println("Testing with Tenant: " + tenant);
            }
        }
        context.setTenantId(tenant);

        context.setOriginalRequestUri("https://localhost:9443/fhir-server/api/v4");
    }

    @AfterMethod
    public void clearThreadLocal() {
        FHIRRequestContext.remove();
    }

    @Test
    public void testOutcomeProvider_tenant_provider() throws FHIROperationException {
        assertEquals(ConfigurationFactory.getInstance().getDefaultImportProvider(), "in");
        assertEquals(ConfigurationFactory.getInstance().getDefaultExportProvider(), "out");
        assertEquals(ConfigurationFactory.getInstance().getOperationOutcomeProvider("out"), "out");
        assertEquals(ConfigurationFactory.getInstance().getOperationOutcomeProvider("in"), "in");
    }
}
