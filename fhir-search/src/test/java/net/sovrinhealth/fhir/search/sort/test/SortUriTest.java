/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.sort.test;

import static org.testng.Assert.assertEquals;

import java.net.URISyntaxException;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.search.SearchConstants;
import net.sovrinhealth.fhir.search.context.FHIRSearchContext;
import net.sovrinhealth.fhir.search.context.FHIRSearchContextFactory;
import net.sovrinhealth.fhir.search.exception.FHIRSearchException;
import net.sovrinhealth.fhir.search.parameters.SortParameter;
import net.sovrinhealth.fhir.search.sort.Sort;
import net.sovrinhealth.fhir.search.uri.UriBuilder;

/**
 * Tests the Sort with UriBuilder
 */
public class SortUriTest {

    @Test
    public void testSortUriSingleDescendingParameter() throws URISyntaxException, FHIRSearchException {
        SortParameter parameter = new SortParameter("_id", SearchConstants.Type.NUMBER, Sort.Direction.DECREASING);
        FHIRSearchContext context = FHIRSearchContextFactory.createSearchContext();
        context.getSortParameters().add(parameter);

        UriBuilder builder = UriBuilder.builder();
        builder.requestUri("https://localhost/fhir-server");
        builder.context(context);

        assertEquals(builder.toSearchSelfUri(), "https://localhost/fhir-server?_count=10&_sort=-_id&_page=1");
    }
    
    @Test
    public void testSortUriSingleAscendingParameter() throws URISyntaxException, FHIRSearchException {
        SortParameter parameter = new SortParameter("_id", SearchConstants.Type.NUMBER, Sort.Direction.INCREASING);
        FHIRSearchContext context = FHIRSearchContextFactory.createSearchContext();
        context.getSortParameters().add(parameter);

        UriBuilder builder = UriBuilder.builder();
        builder.requestUri("https://localhost/fhir-server");
        builder.context(context);

        assertEquals(builder.toSearchSelfUri(), "https://localhost/fhir-server?_count=10&_sort=_id&_page=1");
    }

    @Test
    public void testSortUriDoubleDescendingParameter() throws URISyntaxException, FHIRSearchException {
        SortParameter parameter1 = new SortParameter("test1", SearchConstants.Type.NUMBER, Sort.Direction.DECREASING);
        SortParameter parameter2 = new SortParameter("test2", SearchConstants.Type.NUMBER, Sort.Direction.DECREASING);
        FHIRSearchContext context = FHIRSearchContextFactory.createSearchContext();
        context.getSortParameters().add(parameter1);
        context.getSortParameters().add(parameter2);

        UriBuilder builder = UriBuilder.builder();
        builder.requestUri("https://localhost/fhir-server");
        builder.context(context);

        assertEquals(builder.toSearchSelfUri(), "https://localhost/fhir-server?_count=10&_sort=-test1,-test2&_page=1");
    }
    
    @Test
    public void testSortUriDoubleMixingParameter() throws URISyntaxException, FHIRSearchException {
        SortParameter parameter1 = new SortParameter("test1", SearchConstants.Type.NUMBER, Sort.Direction.DECREASING);
        SortParameter parameter2 = new SortParameter("test2", SearchConstants.Type.NUMBER, Sort.Direction.INCREASING);
        FHIRSearchContext context = FHIRSearchContextFactory.createSearchContext();
        context.getSortParameters().add(parameter1);
        context.getSortParameters().add(parameter2);

        UriBuilder builder = UriBuilder.builder();
        builder.requestUri("https://localhost/fhir-server");
        builder.context(context);

        assertEquals(builder.toSearchSelfUri(), "https://localhost/fhir-server?_count=10&_sort=-test1,test2&_page=1");
    }
    
    @Test
    public void testSortUriDoubleIncreasingParameter() throws URISyntaxException, FHIRSearchException {
        SortParameter parameter1 = new SortParameter("test1", SearchConstants.Type.NUMBER, Sort.Direction.INCREASING);
        SortParameter parameter2 = new SortParameter("test2", SearchConstants.Type.NUMBER, Sort.Direction.INCREASING);
        FHIRSearchContext context = FHIRSearchContextFactory.createSearchContext();
        context.getSortParameters().add(parameter1);
        context.getSortParameters().add(parameter2);

        UriBuilder builder = UriBuilder.builder();
        builder.requestUri("https://localhost/fhir-server");
        builder.context(context);

        assertEquals(builder.toSearchSelfUri(), "https://localhost/fhir-server?_count=10&_sort=test1,test2&_page=1");
    }

}
