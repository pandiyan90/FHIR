/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.operation.bulkdata.model.type.Input;
import net.sovrinhealth.fhir.operation.bulkdata.model.type.JobParameter;
import net.sovrinhealth.fhir.operation.bulkdata.model.type.StorageDetail;

/**
 * Tests for Input and StorageDetail
 */
public class ImportsTest {
    @Test
    public void testInput() throws IOException {
        Input input = new Input("a", "b");
        assertNotNull(input);
        assertEquals(input.getType(), "a");
        assertEquals(input.getUrl(), "b");
        input.setType("c");
        assertEquals(input.getType(), "c");
        input.setUrl("d");
        assertEquals(input.getUrl(), "d");
    }

    @Test
    public void testStorageDetail() throws IOException {
        StorageDetail detail = new StorageDetail("https", Arrays.asList("a", "b"));
        assertNotNull(detail);
        assertEquals(detail.getType(), "https");
        assertFalse(detail.getContentEncodings().isEmpty());
        assertEquals(detail.getContentEncodings().size(), 2);

        detail.setType("httpx");
        assertEquals(detail.getType(), "httpx");

        detail.addContentEncodings("x");
        assertEquals(detail.getContentEncodings().size(), 3);

        detail = new StorageDetail("https", "a", "b");
        assertNotNull(detail);
        assertEquals(detail.getType(), "https");
        assertFalse(detail.getContentEncodings().isEmpty());
        assertEquals(detail.getContentEncodings().size(), 2);
    }

    @Test
    public void testJobParametersSerialization() throws IOException {
        Input input1 = new Input("a", "b");
        Input input2 = new Input("c", "d");
        List<Input> inputs = Arrays.asList(input1, input2);
        String base64str = JobParameter.Writer.writeToBase64(inputs);
        List<Input> roundtripInputs = JobParameter.Parser.parseInputsFromString(base64str);
        assertNotNull(roundtripInputs);
        assertFalse(roundtripInputs.isEmpty());
        assertEquals(roundtripInputs.size(), 2);
    }
}