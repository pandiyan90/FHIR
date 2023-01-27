/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.profile.test;

import static net.sovrinhealth.fhir.profile.ProfileBuilder.binding;
import static net.sovrinhealth.fhir.profile.ProfileBuilder.constraint;
import static net.sovrinhealth.fhir.profile.ProfileBuilder.discriminator;
import static net.sovrinhealth.fhir.profile.ProfileBuilder.profile;
import static net.sovrinhealth.fhir.profile.ProfileBuilder.slicing;
import static net.sovrinhealth.fhir.profile.ProfileBuilder.targetProfile;
import static net.sovrinhealth.fhir.profile.ProfileBuilder.type;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.MessageHeader;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.Organization;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.resource.StructureDefinition.Context;
import net.sovrinhealth.fhir.model.type.Identifier;
import net.sovrinhealth.fhir.model.type.Quantity;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.BindingStrength;
import net.sovrinhealth.fhir.model.type.code.ConstraintSeverity;
import net.sovrinhealth.fhir.model.type.code.DiscriminatorType;
import net.sovrinhealth.fhir.model.type.code.ExtensionContextType;
import net.sovrinhealth.fhir.model.type.code.SlicingRules;
import net.sovrinhealth.fhir.profile.ConstraintGenerator;
import net.sovrinhealth.fhir.profile.ExtensionBuilder;
import net.sovrinhealth.fhir.profile.ProfileBuilder;
import net.sovrinhealth.fhir.profile.ProfileSupport;
import net.sovrinhealth.fhir.registry.FHIRRegistry;

public class ConstraintGeneratorTest {
    // maintain a strong reference to the logger configured for these unit tests
    private static Logger logger = Logger.getLogger(ConstraintGenerator.class.getName());

    @BeforeClass
    public void beforeClass() {
        configureLogging();
    }

    @BeforeClass
    public void before() {
        FHIRRegistry.getInstance();
        FHIRRegistry.init();
    }

    @Test
    public static void testConstraintGeneratorOnExtension() throws Exception {
        StructureDefinition builtExtDef = new ExtensionBuilder("http://example.com/fhir/StructureDefinition/genderExt", "1.0.0", "code")
                .context(Context.builder()
                    .type(ExtensionContextType.ELEMENT)
                    .expression("Patient")
                    .build())
                .cardinality("Extension.value[x]", 1, "1")
                .binding("Extension.value[x]", ExtensionBuilder.binding(BindingStrength.REQUIRED, "http://hl7.org/fhir/ValueSet/administrative-gender"))
                .build();
        ConstraintGenerator generator = new ConstraintGenerator(builtExtDef);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "value.asTypeEqual(code).exists() and value.asTypeEqual(code).all(memberOf('http://hl7.org/fhir/ValueSet/administrative-gender', 'required'))");
    }

    @Test
    public static void testConstraintGenerator1() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Organization.class, "http://example.com/fhir/StructureDefinition/TestOrganization", "1.0.0")
            .slicing("Organization.contained", slicing(discriminator(DiscriminatorType.PROFILE, "$this"), SlicingRules.OPEN))
            .slice("Organization.contained", "ProfileA", Resource.class, 1, "1")
            .type("Organization.contained:ProfileA", type("Resource", profile("http://example.com/fhir/StructureDefinition/ProfileA")))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "contained.where(conformsTo('http://example.com/fhir/StructureDefinition/ProfileA')).count() = 1");
    }

    @Test
    public static void testConstraintGenerator2() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Organization.class, "http://example.com/fhir/StructureDefinition/TestOrganization", "1.0.0")
            .slicing("Organization.contained", slicing(discriminator(DiscriminatorType.PROFILE, "$this"), SlicingRules.OPEN))
            .slice("Organization.contained", "ProfileA", Resource.class, 0, "1")
            .type("Organization.contained:ProfileA", type("Resource", profile("http://example.com/fhir/StructureDefinition/ProfileA")))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "contained.where(conformsTo('http://example.com/fhir/StructureDefinition/ProfileA')).exists() implies (contained.where(conformsTo('http://example.com/fhir/StructureDefinition/ProfileA')).count() = 1)");
    }

    @Test
    public static void testConstraintGenerator3() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Organization.class, "http://example.com/fhir/StructureDefinition/TestOrganization", "1.0.0")
            .slicing("Organization.identifier", slicing(discriminator(DiscriminatorType.VALUE, "system"), SlicingRules.OPEN))
            .slice("Organization.identifier", "SliceA", Identifier.class, 1, "1")
            .cardinality("Organization.identifier:SliceA.system", 1, "1")
            .pattern("Organization.identifier:SliceA.system", Uri.of("http://example.com/fhir/system/system-1"))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "identifier.where(system = 'http://example.com/fhir/system/system-1').count() = 1");
    }

    @Test
    public static void testConstraintGenerator4() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Organization.class, "http://example.com/fhir/StructureDefinition/TestOrganization", "1.0.0")
            .slicing("Organization.identifier", slicing(discriminator(DiscriminatorType.VALUE, "system"), SlicingRules.OPEN))
            .slice("Organization.identifier", "SliceA", Identifier.class, 0, "1")
            .cardinality("Organization.identifier:SliceA.system", 1, "1")
            .pattern("Organization.identifier:SliceA.system", Uri.of("http://example.com/fhir/system/system-1"))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "identifier.where(system = 'http://example.com/fhir/system/system-1').exists() implies (identifier.where(system = 'http://example.com/fhir/system/system-1').count() = 1)");
    }

    @Test
    public static void testConstraintGenerator5() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Organization.class, "http://example.com/fhir/StructureDefinition/TestOrganization", "1.0.0")
            .slicing("Organization.identifier", slicing(discriminator(DiscriminatorType.VALUE, "system"), SlicingRules.OPEN))
            .slice("Organization.identifier", "SliceA", Identifier.class, 0, "1")
            .cardinality("Organization.identifier:SliceA.system", 1, "1")
            .pattern("Organization.identifier:SliceA.system", Uri.of("http://example.com/fhir/system/system-1"))
            .constraint("Organization.identifier:SliceA", constraint("test-1", ConstraintSeverity.ERROR, "The organization SliceB identifier value length SHALL be greater than 9 characters", "value.length() > 9"))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "identifier.where(system = 'http://example.com/fhir/system/system-1').exists() implies (identifier.where(system = 'http://example.com/fhir/system/system-1').count() = 1 and identifier.where(system = 'http://example.com/fhir/system/system-1').all(system = 'http://example.com/fhir/system/system-1' and (value.length() > 9)))");
    }

    @Test
    public static void testConstraintGenerator6() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Observation.class, "http://example.com/fhir/StructureDefinition/TestObservation", "1.0.0")
            .slicing("Observation.value[x]", slicing(discriminator(DiscriminatorType.TYPE, "$this"), SlicingRules.OPEN))
            .slice("Observation.value[x]", "SliceA", Quantity.class, 1, "1")
            .type("Observation.value[x]:SliceA", type("Quantity"))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "value.where(isTypeEqual(Quantity)).count() = 1");
    }

    @Test
    public static void testConstraintGenerator7() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Observation.class, "http://example.com/fhir/StructureDefinition/TestObservation", "1.0.0")
            .type("Observation.value[x]", type("Quantity"))
            .cardinality("Observation.value[x]", 1, "1")
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "value.asTypeEqual(Quantity).exists()");
    }

    @Test
    public static void testConstraintGenerator8() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Patient.class, "http://example.com/fhir/StructureDefinition/TestPatient", "1.0.0")
            .type("Patient.generalPractitioner", type("Reference", profile(), targetProfile(ProfileSupport.HL7_STRUCTURE_DEFINITION_URL_PREFIX + "Organization")))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "generalPractitioner.exists() implies (generalPractitioner.count() >= 1 and generalPractitioner.all(resolve().is(Organization)))");
    }

    @Test
    public static void testConstraintGenerator9() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Patient.class, "http://example.com/fhir/StructureDefinition/TestPatient", "1.0.0")
            .type("Patient.generalPractitioner", type("Reference", profile(), targetProfile(ProfileSupport.HL7_STRUCTURE_DEFINITION_URL_PREFIX + "Organization")))
            .cardinality("Patient.generalPractitioner", 1, "1")
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "generalPractitioner.count() = 1 and generalPractitioner.all(resolve().is(Organization))");
    }

    @Test
    public static void testConstraintGenerator10() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Patient.class, "http://example.com/fhir/StructureDefinition/TestPatient", "1.0.0")
            .cardinality("Patient.name", 1, "1")
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "name.count() = 1");
    }

    @Test
    public static void testConstraintGenerator11() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Observation.class, "http://example.com/fhir/StructureDefinition/TestObservation", "1.0.0")
            .binding("Observation.code", binding(BindingStrength.REQUIRED, "http://example.com/fhir/ValueSet/vs-1"))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "code.exists() and code.all(memberOf('http://example.com/fhir/ValueSet/vs-1', 'required'))");
    }

    @Test
    public static void testConstraintGenerator12() throws Exception {
        StructureDefinition profile = new ProfileBuilder(Observation.class, "http://example.com/fhir/StructureDefinition/TestObservation", "1.0.0")
            .binding("Observation.code", binding(BindingStrength.REQUIRED, "http://example.com/fhir/ValueSet/vs-1", "http://example.com/fhir/ValueSet/max-vs-1"))
            .build();
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "code.exists() and code.all(memberOf('http://example.com/fhir/ValueSet/vs-1', 'required') and memberOf('http://example.com/fhir/ValueSet/max-vs-1', 'required'))");
    }

    @Test
    public static void testConstraintGenerator13() throws Exception {
        StructureDefinition profile = new ProfileBuilder(MessageHeader.class, "http://example.com/fhir/StructureDefinition/TestMessageHeader", "1.0.0")
            .slicing("MessageHeader.focus", slicing(discriminator(DiscriminatorType.PROFILE, "$this.resolve()"), SlicingRules.OPEN))
            .slice("MessageHeader.focus", "SliceA", Reference.class, 0, "1")
            .type("MessageHeader.focus:SliceA", type("Reference", profile(), targetProfile("http://example.com/fhir/StructureDefinition/ProfileA")))
            .build();
        System.out.println(profile);
        ConstraintGenerator generator = new ConstraintGenerator(profile);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);
        assertEquals(constraints.get(0).expression(), "focus.where(resolve().conformsTo('http://example.com/fhir/StructureDefinition/ProfileA')).exists() implies (focus.where(resolve().conformsTo('http://example.com/fhir/StructureDefinition/ProfileA')).count() = 1)");
    }

    private void configureLogging() {
        logger.setLevel(Level.FINEST);
        Handler h = new Handler() {
            @Override
            public void publish(LogRecord record) {
                System.out.println(record.getMessage());
            }

            @Override
            public void flush() { }

            @Override
            public void close() throws SecurityException { }
        };
        h.setLevel(Level.FINEST);
        logger.addHandler(h);
    }
}
