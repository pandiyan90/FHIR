/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.hasErrors;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Organization;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.ElementDefinition;
import net.sovrinhealth.fhir.model.type.ElementDefinition.Constraint;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.ConstraintSeverity;
import net.sovrinhealth.fhir.model.type.code.TypeDerivationRule;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.registry.resource.FHIRRegistryResource;
import net.sovrinhealth.fhir.registry.util.FHIRRegistryResourceProviderAdapter;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class ConformsToLoopTest {
    @BeforeClass
    public void beforeClass() throws Exception {
        StructureDefinition structureDefinition = FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/StructureDefinition/Organization", StructureDefinition.class);

        List<ElementDefinition> element = new ArrayList<>(structureDefinition.getSnapshot().getElement());
        element.set(indexOf(structureDefinition, "Organization"), addConstraints(getElementDefinition(structureDefinition, "Organization"),
            Constraint.builder()
                .key(Id.of("test-1"))
                .severity(ConstraintSeverity.ERROR)
                .human(string("The organization SHALL conform to the TestOrganization profile"))
                .expression(string("conformsTo('http://example.com/fhir/StructureDefinition/TestOrganization')"))
                .source(Canonical.of("http://example.com/fhir/StructureDefinition/TestOrganization"))
                .build(),
            Constraint.builder()
                .key(Id.of("test-2"))
                .severity(ConstraintSeverity.ERROR)
                .human(string("The organization name length SHALL be greater than 9 characters"))
                .expression(string("name.length() > 9"))
                .source(Canonical.of("http://example.com/fhir/StructureDefinition/TestOrganization"))
                .build()));
        element.set(indexOf(structureDefinition, "Organization.partOf"), addConstraints(getElementDefinition(structureDefinition, "Organization.partOf"), Constraint.builder()
            .key(Id.of("test-3"))
            .human(string("The partOf reference SHALL resolve to an organization that conforms the TestOrganization profile"))
            .expression(string("resolve().conformsTo('http://example.com/fhir/StructureDefinition/TestOrganization')"))
            .source(Canonical.of("http://example.com/fhir/StructureDefinition/TestOrganization"))
            .severity(ConstraintSeverity.ERROR)
            .build()));

        StructureDefinition profile = structureDefinition.toBuilder()
            .url(Uri.of("http://example.com/fhir/StructureDefinition/TestOrganization"))
            .baseDefinition(Canonical.of("http://hl7.org/fhir/StructureDefinition/Organization"))
            .derivation(TypeDerivationRule.CONSTRAINT)
            .snapshot(structureDefinition.getSnapshot().toBuilder()
                .element(element)
                .build())
            .build();

        FHIRRegistry.getInstance().addProvider(new FHIRRegistryResourceProviderAdapter() {
            @Override
            public FHIRRegistryResource getRegistryResource(Class<? extends Resource> resourceType, String url, String version) {
                if ("http://example.com/fhir/StructureDefinition/TestOrganization".equals(url)) {
                    return FHIRRegistryResource.from(profile);
                }
                return null;
            }
        });
    }

    @Test
    public void testConformsToLoop1() throws Exception {
        Organization organization = Organization.builder()
            .name(string("Test Organization"))
            .contained(Organization.builder()
                .meta(Meta.builder()
                    .profile(Canonical.of("http://example.com/fhir/StructureDefinition/TestOrganization"))
                    .build())
                .name(string("Child Test Organization"))
                .partOf(Reference.builder()
                    .reference(string("#"))
                    .build())
                .build())
            .build();
        List<Issue> issues = FHIRValidator.validator().validate(organization, true, "http://example.com/fhir/StructureDefinition/TestOrganization");
        assertFalse(hasErrors(issues));
    }

    @Test
    public void testConformsToLoop2() throws Exception {
        Organization organization = Organization.builder()
            .name(string("Test Organization"))
            .contained(Organization.builder()
                .meta(Meta.builder()
                    .profile(Canonical.of("http://example.com/fhir/StructureDefinition/TestOrganization"))
                    .build())
                .name(string("invalid"))
                .partOf(Reference.builder()
                    .reference(string("#"))
                    .build())
                .build())
            .build();
        List<Issue> issues = FHIRValidator.validator().validate(organization, true, "http://example.com/fhir/StructureDefinition/TestOrganization");
        assertTrue(hasErrors(issues));
        assertEquals(countErrors(issues), 1);
    }

    private ElementDefinition addConstraints(ElementDefinition elementDefinition, Constraint... constraints) {
        return elementDefinition.toBuilder()
            .constraint(elementDefinition.getConstraint())
            .constraint(constraints)
            .build();
    }

    private ElementDefinition getElementDefinition(StructureDefinition structureDefinition, String path) {
        for (ElementDefinition elementDefinition : structureDefinition.getSnapshot().getElement()) {
            if (elementDefinition.getPath().getValue().equals(path)) {
                return elementDefinition;
            }
        }
        return null;
    }

    private int indexOf(StructureDefinition structureDefinition, String path) {
        for (int i = 0; i < structureDefinition.getSnapshot().getElement().size(); i++) {
            ElementDefinition elementDefinition = structureDefinition.getSnapshot().getElement().get(i);
            if (elementDefinition.getPath().getValue().equals(path)) {
                return i;
            }
        }
        return -1;
    }
}
