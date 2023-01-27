/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.cassandra.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.generator.exception.FHIRGeneratorException;
import net.sovrinhealth.fhir.model.parser.exception.FHIRParserException;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Xhtml;
import net.sovrinhealth.fhir.model.type.code.NarrativeStatus;
import net.sovrinhealth.fhir.persistence.FHIRPersistenceSupport;
import net.sovrinhealth.fhir.persistence.cassandra.payload.CqlPayloadStream;
import net.sovrinhealth.fhir.persistence.util.InputOutputByteStream;

/**
 * Unit test for {@link CqlPayloadStream}
 */
public class PayloadStreamTest {

    @Test
    public void roundTripNormal() throws IOException, FHIRParserException, FHIRGeneratorException {
        Patient patient = Patient.builder()
                .id("a-unique-value")
                .generalPractitioner(Reference.builder()
                    .reference(string("Practitioner/1"))
                    .build())
                .text(Narrative.builder()
                    .div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">Some narrative</div>"))
                    .status(NarrativeStatus.GENERATED)
                    .build())
                .build();
        
        InputOutputByteStream iobs = FHIRPersistenceSupport.render(patient, true);
        
        // Get the data as a ByteBuffer to pretend that it was stored in Cassandra and read back
        ByteBuffer bb = iobs.wrap();
        InputOutputByteStream readStream = new InputOutputByteStream(bb);
        Patient p = FHIRPersistenceSupport.parse(Patient.class, readStream.inputStream(), null, true);
        assertNotNull(p);
        assertEquals(p.getId(), patient.getId());
    }

    @Test
    public void roundTripNoCompress() throws IOException, FHIRParserException, FHIRGeneratorException {
        Patient patient = Patient.builder()
                .id("a-unique-value")
                .generalPractitioner(Reference.builder()
                    .reference(string("Practitioner/1"))
                    .build())
                .text(Narrative.builder()
                    .div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">Some narrative</div>"))
                    .status(NarrativeStatus.GENERATED)
                    .build())
                .build();
        
        InputOutputByteStream iobs = FHIRPersistenceSupport.render(patient, false);
        
        // Get the data as a ByteBuffer to pretend that it was stored in Cassandra and read back
        ByteBuffer bb = iobs.wrap();
        InputOutputByteStream readStream = new InputOutputByteStream(bb);
        // See if we can read the resource from this stream
        Patient p = FHIRPersistenceSupport.parse(Patient.class, readStream.inputStream(), null, false);
        assertNotNull(p);
        assertEquals(p.getId(), patient.getId());
    }
}