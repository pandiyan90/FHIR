/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.model.visitor.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static org.testng.Assert.assertFalse;

import java.io.StringWriter;
import java.util.Collections;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.test.TestUtil;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Xhtml;
import net.sovrinhealth.fhir.model.type.code.NarrativeStatus;
import net.sovrinhealth.fhir.model.visitor.EncodingVisitor;
import net.sovrinhealth.fhir.model.visitor.EncodingVisitor.EncodingContext;

public class EncodingVisitorTest {
    private static OperationOutcome oo;

    @BeforeClass
    public static void setup() throws Exception {
        oo = TestUtil.getMinimalResource(OperationOutcome.class);
        Issue issue = oo.getIssue().get(0).toBuilder()
                .extension(Extension.builder()
                    .id("\" onload=\"alert('test1')")
                    .url("http://example.com/evil")
                    .value(string("<script>alert('powned')</script>"))
                    .build())
                .details(CodeableConcept.builder()
                    .text(string("<body onload=alert('powned2')>"))
                    .build())
                .diagnostics(string("<META HTTP-EQUIV=\"refresh\"\n"
                        + "CONTENT=\"0;url=data:text/html;base64,PHNjcmlwdD5hbGVydCgndGVzdDMnKTwvc2NyaXB0Pg\">"))
                .build();

        oo = oo.toBuilder()
                .text(Narrative.builder()
                    .id("\" onmouseover=\"alert('Wufff!')")
                    .extension(Collections.singleton(Extension.builder()
                        .url("http://example.com/evil")
                        .value(string("<script>alert('powned')</script>"))
                        .build()))
                    .status(NarrativeStatus.EMPTY)
                    .div(Xhtml.from("Narrative text is tested elsewhere"))
                    .build())
                .issue(Collections.singleton(issue))
                .build();
    }

    @Test
    public void testEscape() throws Exception {
        EncodingVisitor<OperationOutcome> v = new EncodingVisitor<>(EncodingContext.HTML_CONTENT);
        oo.accept(v);

        OperationOutcome result = v.getResult();

        StringWriter writer = new StringWriter();
        FHIRGenerator.generator(Format.JSON, true).generate(result, writer);
        System.out.println(writer);

        String resultSansXhtml = writer.toString()
                .replace("<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">", "")
                .replace("</div>", "");
        assertFalse(resultSansXhtml.contains("<"));
    }
}
