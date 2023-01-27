/*
 * (C) Copyright IBM Corp. 2016, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.sort.test;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.search.SearchConstants;
import net.sovrinhealth.fhir.search.parameters.SortParameter;
import net.sovrinhealth.fhir.search.sort.Sort;

/**
 * Tests the SortParameter
 */
public class SortParameterTest {
    @Test
    public void testSortParameter() {
        SortParameter parameter = new SortParameter("_id", SearchConstants.Type.NUMBER, Sort.Direction.DECREASING);
        assertEquals(parameter.getCode(), "_id");
        assertEquals(parameter.getDirection().value(), '-');
        assertEquals(parameter.getType().value(), "number");

        assertEquals(parameter.toString(), "_sort=-_id");

        parameter = new SortParameter("_id", SearchConstants.Type.STRING, Sort.Direction.INCREASING);
        assertEquals(parameter.getCode(), "_id");
        assertEquals(parameter.getDirection().value(), '+');
        assertEquals(parameter.getType().value(), "string");
        assertEquals(parameter.toString(), "_sort=_id");
    }
}