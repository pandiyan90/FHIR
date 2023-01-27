/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.model.util.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.stream.Collectors;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.test.TestUtil;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Identifier;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.code.IdentifierUse;
import net.sovrinhealth.fhir.model.util.SaltHash;
import net.sovrinhealth.fhir.model.visitor.ResourceFingerprintVisitor;

/**
 * Tests for the ResourceFingerprintVisitor
 */
public class ResourceFingerprintVisitorTest {
    @Test
    public void testEqualResources() throws Exception {
        Patient patient = TestUtil.getMinimalResource(Patient.class);

        ResourceFingerprintVisitor resourceFingerprintVisitor = new ResourceFingerprintVisitor();
        patient.accept(resourceFingerprintVisitor);
        SaltHash baseline = resourceFingerprintVisitor.getSaltAndHash();

        // same resource should match
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);
        assertEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);

        // shallowly rebuilt resource should match
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.toBuilder().build().accept(resourceFingerprintVisitor);
        assertEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);

        // deeply rebuilt resource should match
        patient = patient.toBuilder()
                .meta(patient.getMeta().toBuilder()
                    .tag(patient.getMeta().getTag().stream()
                        .map(t -> t.toBuilder()
                            .code(t.getCode().toBuilder().build())
                            .build())
                        .collect(Collectors.toList()))
                    .build())
                .build();
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);
        assertEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);
    }

    @Test
    public void testUnequalResources_intVals() throws Exception {
        Patient patient = TestUtil.getMinimalResource(Patient.class).toBuilder()
                .multipleBirth(1)
                .build();

        ResourceFingerprintVisitor resourceFingerprintVisitor = new ResourceFingerprintVisitor();
        patient.accept(resourceFingerprintVisitor);
        SaltHash baseline = resourceFingerprintVisitor.getSaltAndHash();

        patient = patient.toBuilder()
                .multipleBirth(2)
                .build();
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);
        assertNotEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);
    }

    @Test
    public void testUnequalResources_add() throws Exception {
        Patient patient = TestUtil.getMinimalResource(Patient.class);

        ResourceFingerprintVisitor resourceFingerprintVisitor = new ResourceFingerprintVisitor();
        patient.accept(resourceFingerprintVisitor);
        SaltHash baseline = resourceFingerprintVisitor.getSaltAndHash();

        patient = patient.toBuilder()
                .identifier(Identifier.builder()
                    .use(IdentifierUse.USUAL)
                    .build())
                .build();
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);
        assertNotEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);
    }

    @Test
    public void testUnequalResources_remove() throws Exception {
        Patient patient = TestUtil.getMinimalResource(Patient.class);

        ResourceFingerprintVisitor resourceFingerprintVisitor = new ResourceFingerprintVisitor();
        patient.accept(resourceFingerprintVisitor);
        SaltHash baseline = resourceFingerprintVisitor.getSaltAndHash();

        patient = patient.toBuilder()
                .meta(null)
                .build();
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);
        assertNotEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);
    }

    @Test
    public void testUnequalResources_reorder() throws Exception {
        Patient patient = TestUtil.getMinimalResource(Patient.class);
        patient = patient.toBuilder()
                .identifier(Identifier.builder()
                    .use(IdentifierUse.USUAL)
                    .build())
                .identifier(Identifier.builder()
                    .use(IdentifierUse.OFFICIAL)
                    .build())
                .build();

        ResourceFingerprintVisitor resourceFingerprintVisitor = new ResourceFingerprintVisitor();
        patient.accept(resourceFingerprintVisitor);
        SaltHash baseline = resourceFingerprintVisitor.getSaltAndHash();

        patient = patient.toBuilder()
                .identifier(Identifier.builder()
                    .use(IdentifierUse.OFFICIAL)
                    .build())
                .identifier(Identifier.builder()
                    .use(IdentifierUse.USUAL)
                    .build())
                .build();
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);

        assertNotEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);
    }

    @Test
    public void testIgnoredPaths() throws Exception {
        Patient patient = TestUtil.getMinimalResource(Patient.class);

        ResourceFingerprintVisitor resourceFingerprintVisitor = new ResourceFingerprintVisitor();
        patient.accept(resourceFingerprintVisitor);
        SaltHash baseline = resourceFingerprintVisitor.getSaltAndHash();

        patient = patient.toBuilder()
                .id("test")
                .meta(patient.getMeta().toBuilder()
                    .versionId(Id.of("ignoreMe"))
                    .lastUpdated(Instant.now())
                    .build())
                .build();
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);

        assertEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);
    }

    @Test
    public void testUnequalResources_extension() throws Exception {
        Patient patient = TestUtil.getMinimalResource(Patient.class);

        ResourceFingerprintVisitor resourceFingerprintVisitor = new ResourceFingerprintVisitor();
        patient.accept(resourceFingerprintVisitor);
        SaltHash baseline = resourceFingerprintVisitor.getSaltAndHash();

        patient = patient.toBuilder()
                .meta(patient.getMeta().toBuilder()
                    .tag(patient.getMeta().getTag().stream()
                        .map(t -> t.toBuilder()
                            .code(t.getCode().toBuilder()
                                .extension(Extension.builder()
                                    .url("http://example.com")
                                    .value(string("primitive extension"))
                                    .build())
                                .build())
                            .build())
                        .collect(Collectors.toList()))
                    .build())
                .build();
        resourceFingerprintVisitor = new ResourceFingerprintVisitor(baseline);
        patient.accept(resourceFingerprintVisitor);
        assertNotEquals(resourceFingerprintVisitor.getSaltAndHash(), baseline);
    }
}
