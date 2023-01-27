package net.sovrinhealth.fhir.operation.convert;
import static org.testng.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

/*
 * (C) Copyright Merative 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Extension;

public class ConversionTest {
    // https://github.com/LinuxForHealth/FHIR/issues/3577
    @Test(enabled = false)
    void testStringCompare() throws Exception {
        Resource original = Patient.builder()
                .extension(Extension.builder()
                        .url("test")
                        .value("test\none")
                        .build())
                .build();

        StringWriter sw = new StringWriter();
        FHIRGenerator.generator(Format.XML).generate(original, sw);

        StringReader sr = new StringReader(sw.toString());
        Resource roundtripped = FHIRParser.parser(Format.XML).parse(sr);

        assertEquals(roundtripped, original);
    }
}
