/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata.tool.helpers.dynamic;

import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.model.type.Xhtml.xhtml;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry.Request;
import net.sovrinhealth.fhir.model.resource.Group;
import net.sovrinhealth.fhir.model.resource.Group.Characteristic;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.Observation.Component;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.type.Age;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Date;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.HumanName;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Quantity;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.Xhtml;
import net.sovrinhealth.fhir.model.type.code.BundleType;
import net.sovrinhealth.fhir.model.type.code.GroupType;
import net.sovrinhealth.fhir.model.type.code.HTTPVerb;
import net.sovrinhealth.fhir.model.type.code.NarrativeStatus;
import net.sovrinhealth.fhir.model.type.code.ObservationStatus;
import net.sovrinhealth.fhir.model.type.code.QuantityComparator;

/**
 * Shows an age range with blood pressure included in a Dynamic Group.
 */
public class AgeRangeBloodPressureGroup extends GroupExample {
    @Override
    public String filename() {
        return "age-range-blood-pressure";
    }

    @Override
    public Group group() {
        java.lang.String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative</b></p></div>";

        java.lang.String id = UUID.randomUUID().toString();

        Meta meta = Meta.builder()
                .versionId(Id.of("1"))
                .lastUpdated(Instant.now(ZoneOffset.UTC))
                .build();

        Narrative text = Narrative.builder()
                .status(NarrativeStatus.GENERATED)
                .div(xhtml(div))
                .build();

        net.sovrinhealth.fhir.model.type.Boolean active = net.sovrinhealth.fhir.model.type.Boolean.of(true);

        net.sovrinhealth.fhir.model.type.Boolean actual = net.sovrinhealth.fhir.model.type.Boolean.of(false);

        Collection<Characteristic> characteristics = new ArrayList<>();
        characteristics.add(generateLowerBoundBirthdateCharacteristic());
        characteristics.add(generateUpperBoundBirthdateCharacteristic());

        // Values take from https://www.health.harvard.edu/heart-health/reading-the-new-blood-pressure-guidelines
        characteristics.add(generateUpperBloodPressureCharacteristic());
        characteristics.add(generateLowBloodPressureCharacteristic());

        Group group = Group.builder()
                .id(id)
                .meta(meta)
                .text(text)
                .active(active)
                .type(GroupType.PERSON)
                .actual(actual)
                .name(string(filename()))
                .characteristic(characteristics)
                .build();
        return group;
    }

    private Characteristic generateUpperBloodPressureCharacteristic() {
        CodeableConcept code = CodeableConcept.builder().coding(Coding.builder().code(Code.of("8480-6"))
            .system(Uri.of("http://loinc.org")).build())
            .text(string("Systolic blood pressure"))
            .build();

        Quantity upper = Quantity.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .code(Code.of("mm[Hg]"))
                .value(Decimal.of("120"))
                .comparator(QuantityComparator.GREATER_OR_EQUALS)
                .build();

        Characteristic characteristic = Characteristic.builder()
            .code(code)
            .value(upper)
            .exclude(net.sovrinhealth.fhir.model.type.Boolean.FALSE)
            .build();
        return characteristic;
    }

    private Characteristic generateLowBloodPressureCharacteristic() {
        CodeableConcept code = CodeableConcept.builder().coding(Coding.builder().code(Code.of("8462-4"))
            .system(Uri.of("http://loinc.org")).build())
            .text(string("Diastolic blood pressure"))
            .build();

        Quantity upper = Quantity.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .code(Code.of("mm[Hg]"))
                .value(Decimal.of("120"))
                .comparator(QuantityComparator.GREATER_OR_EQUALS)
                .build();

        Characteristic characteristic = Characteristic.builder()
            .code(code)
            .value(upper)
            .exclude(net.sovrinhealth.fhir.model.type.Boolean.FALSE)
            .build();
        return characteristic;
    }

    private Characteristic generateLowerBoundBirthdateCharacteristic() {
        CodeableConcept code = CodeableConcept.builder().coding(Coding.builder().code(Code.of("29553-5"))
            .system(Uri.of("http://loinc.org")).build())
            .text(string("Age calculated"))
            .build();

        Age value = Age.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .code(Code.of("a"))
                .unit(string("years"))
                .value(Decimal.of("35"))
                .comparator(QuantityComparator.GREATER_OR_EQUALS)
                .build();

        Characteristic characteristic = Characteristic.builder()
            .code(code)
            .value(value)
            .exclude(net.sovrinhealth.fhir.model.type.Boolean.FALSE)
            .build();
        return characteristic;
    }

    private Characteristic generateUpperBoundBirthdateCharacteristic() {
        CodeableConcept code = CodeableConcept.builder().coding(Coding.builder().code(Code.of("29553-5"))
            .system(Uri.of("http://loinc.org")).build())
            .text(string("Age calculated"))
            .build();

        Age value = Age.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .code(Code.of("a"))
                .unit(string("years"))
                .value(Decimal.of("70"))
                .comparator(QuantityComparator.LESS_OR_EQUALS)
                .build();

        Characteristic characteristic = Characteristic.builder()
            .code(code)
            .value(value)
            .exclude(net.sovrinhealth.fhir.model.type.Boolean.FALSE)
            .build();
        return characteristic;
    }

    @Override
    public Bundle sampleData() {
        Bundle.Entry entry = Bundle.Entry.builder()
                .resource(buildTestPatient())
                .request(Request.builder().method(HTTPVerb.POST)
                    .url(Uri.of("Patient")).build())
                .build();
        Bundle.Entry entryObservation = Bundle.Entry.builder()
                .resource(buildObservation("1-2-3-4"))
                .request(Request.builder().method(HTTPVerb.POST)
                    .url(Uri.of("Observation")).build())
                .build();
        return Bundle.builder()
                .type(BundleType.TRANSACTION)
                .entry(entry, entryObservation).build();
    }

    private Patient buildTestPatient() {
        String id = UUID.randomUUID().toString();

        Meta meta =
                Meta.builder()
                    .versionId(Id.of("1"))
                    .lastUpdated(Instant.now(ZoneOffset.UTC))
                    .build();

        net.sovrinhealth.fhir.model.type.String given =
                net.sovrinhealth.fhir.model.type.String.builder()
                .value("John")
                .build();

        HumanName name =
                HumanName.builder()
                    .id("someId")
                    .given(given)
                    .family(string("Doe")).build();

        java.lang.String uUID = UUID.randomUUID().toString();

        Reference providerRef =
                Reference.builder().reference(string("urn:uuid:" + uUID)).build();

        return Patient.builder().id(id)
                .active(net.sovrinhealth.fhir.model.type.Boolean.TRUE)
                .multipleBirth(net.sovrinhealth.fhir.model.type.Integer.of(2))
                .meta(meta).name(name).birthDate(Date.of(LocalDate.now()))
                .generalPractitioner(providerRef)
                .text(
                    Narrative.builder()
                        .div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">loaded from the datastore</div>"))
                        .status(NarrativeStatus.GENERATED).build())
                .build();
    }

    public Observation buildObservation(String patientId) {
        CodeableConcept code = CodeableConcept.builder().coding(Coding.builder().code(Code.of("55284-4"))
            .system(Uri.of("http://loinc.org")).build())
            .text(string("Blood pressure systolic & diastolic"))
            .build();

        Observation observation = Observation.builder().status(ObservationStatus.FINAL).bodySite(
                CodeableConcept.builder().coding(Coding.builder().code(Code.of("55284-4"))
                        .system(Uri.of("http://loinc.org")).build())
                        .text(string("Blood pressure systolic & diastolic")).build())
                .category(CodeableConcept.builder().coding(Coding.builder().code(Code.of("signs"))
                        .system(Uri.of("http://hl7.org/fhir/observation-category")).build())
                        .text(string("Vital Signs")).build())
                .code(code)
                .subject(Reference.builder().reference(string("Patient/" + patientId)).build())
                .component(Component.builder().code(CodeableConcept.builder().coding(Coding.builder().code(Code.of("8480-6"))
                        .system(Uri.of("http://loinc.org")).build())
                        .text(string("Systolic blood pressure")).build())
                        .value(Quantity.builder().value(Decimal.of(124.9)).unit(string("mmHg")).build()).build())
                .component(Component.builder().code(CodeableConcept.builder().coding(Coding.builder().code(Code.of("8462-4"))
                        .system(Uri.of("http://loinc.org")).build())
                        .text(string("Diastolic blood pressure")).build())
                        .value(Quantity.builder().value(Decimal.of(93.7)).unit(string("mmHg")).build()).build())
                .text(
                    Narrative.builder()
                        .div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">loaded from the datastore</div>"))
                        .status(NarrativeStatus.GENERATED).build())
                .build();
        return observation;
    }
}