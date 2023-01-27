/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.test;

import static net.sovrinhealth.fhir.model.type.Xhtml.xhtml;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.config.FHIRModelConfig;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Boolean;
import net.sovrinhealth.fhir.model.type.Date;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.HumanName;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.Integer;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.String;
import net.sovrinhealth.fhir.model.type.code.NarrativeStatus;
import net.sovrinhealth.fhir.model.visitor.PathAwareVisitor;
import net.sovrinhealth.fhir.model.visitor.Visitor;

public class ToStringTest {
    @Test
    public void testToStringMethod() throws Exception {
        Patient patient = buildPatient();
        
        FHIRModelConfig.setToStringPrettyPrinting(false);
        Assert.assertEquals(patient.getMeta().getVersionId().toString(), "{\"id\":\"1\"}");
        Assert.assertEquals(patient.getMeta().toString(), "{\"versionId\":\"1\",\"lastUpdated\":\"2019-01-01T12:00:00Z\"}");
        
        FHIRModelConfig.setToStringFormat(Format.XML);
        Assert.assertEquals(patient.getMeta().getVersionId().toString(), "<id value=\"1\"/>");
        Assert.assertEquals(patient.getMeta().toString(), "<Meta><versionId value=\"1\"/><lastUpdated value=\"2019-01-01T12:00:00Z\"/></Meta>");
    }
    
    
    public static void main(java.lang.String[] args) {
        Patient patient = buildPatient();
        
        FHIRModelConfig.setToStringFormat(Format.XML);
        
        Visitor visitor = new PathAwareVisitor() {
            @Override
            protected void doVisitStart(java.lang.String elementName, int elementIndex, Element element) {
                System.out.println("path: " + getPath());
                System.out.println(element);
            }

            @Override
            protected void doVisitStart(java.lang.String elementName, int elementIndex, Resource resource) {
                System.out.println("path: " + getPath());
                System.out.println(resource);
            }
        };
        
        patient.accept(visitor);
        
        FHIRModelConfig.setToStringFormat(Format.JSON);
        
        patient.accept(visitor);
        
        FHIRModelConfig.setToStringPrettyPrinting(true);
        
        patient.accept(visitor);
        
        FHIRModelConfig.setToStringFormat(Format.XML);
        FHIRModelConfig.setToStringPrettyPrinting(false);
        
        patient.accept(visitor);
        
        FHIRModelConfig.setToStringPrettyPrinting(true);
        FHIRModelConfig.setToStringIndentAmount(4);
        
        patient.accept(visitor);
    }

    private static Patient buildPatient() {
        java.lang.String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative</b></p></div>";
        
        java.lang.String id = "9aac1d9c-ea5f-4513-af9c-897ab21dd11d";
        
        Meta meta = Meta.builder().versionId(Id.of("1"))
                .lastUpdated(Instant.of("2019-01-01T12:00:00Z"))
                .build();
        
        String given = String.builder().value("John")
                .extension(Extension.builder()
                    .url("http://example.com/someExtension")
                    .value(String.of("value and extension"))
                    .build())
                .build();
        
        String otherGiven = String.builder()
                .id("someOtherId")
                .extension(Extension.builder()
                    .url("http://example.com/someExtension")
                    .value(String.of("extension only"))
                    .build())
                .build();
        
        HumanName name = HumanName.builder()
                .id("someId")
                .given(given)
                .given(otherGiven)
                .given(String.of("value no extension"))
                .family(String.of("Doe"))
                .build();
        
        Narrative text = Narrative.builder()
                .status(NarrativeStatus.GENERATED)
                .div(xhtml(div))
                .build();
                
        Patient patient = Patient.builder()
                .id(id)
                .text(text)
                .active(Boolean.TRUE)
                .multipleBirth(Integer.of(2))
                .meta(meta)
                .name(name)
                .birthDate(Date.of("1970-01-01"))
                .build();
        
        return patient;
    }
}
