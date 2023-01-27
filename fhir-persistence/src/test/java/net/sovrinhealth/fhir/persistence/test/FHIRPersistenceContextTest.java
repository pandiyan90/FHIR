/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.persistence.context.FHIRHistoryContext;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContextFactory;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceEvent;
import net.sovrinhealth.fhir.search.context.FHIRSearchContext;
import net.sovrinhealth.fhir.search.context.FHIRSearchContextFactory;
import net.sovrinhealth.fhir.search.exception.FHIRSearchException;

/**
 * Tests associated with the FHIRPersistenceContextImpl class.
 */
public class FHIRPersistenceContextTest {

    @Test
    public void test1() {
        FHIRPersistenceEvent pe = new FHIRPersistenceEvent();

        FHIRPersistenceContext ctxt = FHIRPersistenceContextFactory.createPersistenceContext(pe);
        assertNotNull(ctxt);
        assertNotNull(ctxt.getPersistenceEvent());
        assertEquals(pe, ctxt.getPersistenceEvent());
        assertNull(ctxt.getHistoryContext());
        assertNull(ctxt.getSearchContext());
    }

    @Test
    public void test2() {
        FHIRPersistenceEvent pe = new FHIRPersistenceEvent();
        FHIRHistoryContext hc = FHIRPersistenceContextFactory.createHistoryContext();
        assertNotNull(hc);

        FHIRPersistenceContext ctxt = FHIRPersistenceContextFactory.createPersistenceContext(pe, hc);
        assertNotNull(ctxt);
        assertNotNull(ctxt.getPersistenceEvent());
        assertEquals(pe, ctxt.getPersistenceEvent());
        assertNotNull(ctxt.getHistoryContext());
        assertEquals(hc, ctxt.getHistoryContext());
        assertNull(ctxt.getSearchContext());
    }

    @Test
    public void test3() throws FHIRSearchException {
        FHIRPersistenceEvent pe = new FHIRPersistenceEvent();
        FHIRSearchContext sc = FHIRSearchContextFactory.createSearchContext();
        assertNotNull(sc);

        FHIRPersistenceContext ctxt = FHIRPersistenceContextFactory.createPersistenceContext(pe, sc, "pat42");
        assertNotNull(ctxt);
        assertNotNull(ctxt.getPersistenceEvent());
        assertEquals(pe, ctxt.getPersistenceEvent());
        assertNotNull(ctxt.getSearchContext());
        assertEquals(sc, ctxt.getSearchContext());
        assertNull(ctxt.getHistoryContext());
        assertEquals("pat42", ctxt.getRequestShard());
    }
}
