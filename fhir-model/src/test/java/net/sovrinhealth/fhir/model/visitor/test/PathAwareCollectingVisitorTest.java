/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.visitor.test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.model.resource.ValueSet.Expansion;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.HumanName;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Xhtml;
import net.sovrinhealth.fhir.model.type.code.NarrativeStatus;
import net.sovrinhealth.fhir.model.type.code.PublicationStatus;
import net.sovrinhealth.fhir.model.visitor.PathAwareCollectingVisitor;

public class PathAwareCollectingVisitorTest {
    @Test
    public void testPrimitiveSetterEquivalence() {
        Patient p1 = Patient.builder()
                .text(Narrative.builder()
                        .status(NarrativeStatus.ADDITIONAL)
                        .div(Xhtml.of(Xhtml.DIV_OPEN + "<div>this<br/>is<br/>a test</div>" + Xhtml.DIV_CLOSE))
                        .build())
                .meta(Meta.builder()
                    .lastUpdated(ZonedDateTime.of(2021, 8, 19, 00, 59, 59, 0, ZoneOffset.of("-05:00"))) // Instant
                    .build())
                .extension(Extension.builder()
                    .url("test")
                    .value("string")
                    .build())
                .contained(ValueSet.builder()
                    .status(PublicationStatus.DRAFT)
                    .expansion(Expansion.builder()
                        .timestamp(DateTime.of("2021-08-19T00:59:59-05:00"))
                        .total(0)                                                                       // Integer
                        .build())
                    .build())
                .active(false)                                                                          // Boolean
                .birthDate(LocalDate.of(1984, 9, 4))                                                    // Date
                .multipleBirth(1)
                .name(HumanName.builder()
                    .given("Lee")                                                                       // String
                    .build())
                .build();

        PathAwareCollectingVisitor<Extension> extCollector = new PathAwareCollectingVisitor<Extension>(Extension.class);
        p1.accept(extCollector);
        System.out.println(extCollector.getResult());
    }
}
