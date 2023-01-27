/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.config.FHIRConfiguration;
import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.core.FHIRConstants;
import net.sovrinhealth.fhir.exception.FHIRException;
import net.sovrinhealth.fhir.persistence.context.FHIRHistoryContext;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContextFactory;

public class FHIRPersistenceFactoryTest {
    @BeforeClass
    public void setUpBeforeClass() {
        FHIRConfiguration.setConfigHome("target/test-classes");
    }

    @BeforeMethod
    @AfterMethod
    public void clearThreadLocal() {
        FHIRRequestContext.remove();
    }

    @Test
    public void testCreateWithDefaultPageSize() throws Exception {
        runCreateTest("default", FHIRConstants.FHIR_PAGE_SIZE_DEFAULT, FHIRConstants.FHIR_PAGE_SIZE_DEFAULT_MAX, FHIRConstants.FHIR_PAGE_INCLUDE_COUNT_DEFAULT_MAX);
    }

    @Test
    public void testCreateWithUserConfiguredPageSize() throws Exception {
        runCreateTest("pagesize-valid", 500, FHIRConstants.FHIR_PAGE_SIZE_DEFAULT_MAX, FHIRConstants.FHIR_PAGE_INCLUDE_COUNT_DEFAULT_MAX);
    }

    @Test
    public void testCreateWithUserConfiguredPageSizeBeyondMaxium() throws Exception {
        runCreateTest("pagesize-invalid", 4000, 4000, 2500);
    }

    private void runCreateTest(String tenantId, int expectedPageSize, int expectedMaxPageSize, int expectedMaxPageIncludeCount) throws FHIRException {
        FHIRRequestContext.set(new FHIRRequestContext(tenantId));
        FHIRHistoryContext ctx = FHIRPersistenceContextFactory.createHistoryContext();
        assertEquals(ctx.getPageSize(), expectedPageSize);
        assertEquals(ctx.getMaxPageSize(), expectedMaxPageSize);
        assertEquals(ctx.getMaxPageIncludeCount(), expectedMaxPageIncludeCount);
    }
}
