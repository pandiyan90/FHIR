/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import static net.sovrinhealth.fhir.model.type.String.string;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countErrors;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countInformation;
import static net.sovrinhealth.fhir.validation.util.FHIRValidationUtil.countWarnings;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.Coverage;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Period;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.code.CoverageStatus;
import net.sovrinhealth.fhir.profile.ConstraintGenerator;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class ProfileConstraintTest {
    @Test
    public void testProfileConstraint() throws Exception {
        StructureDefinition structureDefinition = FHIRRegistry.getInstance().getResource("http://myurl.org/my-coverage", StructureDefinition.class);
        ConstraintGenerator generator = new ConstraintGenerator(structureDefinition);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);

        structureDefinition = FHIRRegistry.getInstance().getResource("http://myurl.org/my-extension", StructureDefinition.class);
        generator = new ConstraintGenerator(structureDefinition);
        constraints = generator.generate();
        assertEquals(constraints.size(), 2);

        structureDefinition = FHIRRegistry.getInstance().getResource("http://myurl.org/period-with-my-extension", StructureDefinition.class);
        generator = new ConstraintGenerator(structureDefinition);
        constraints = generator.generate();
        assertEquals(constraints.size(), 1);

        Coverage coverage = Coverage.builder()
                .status(CoverageStatus.ACTIVE)
                .beneficiary(Reference.builder()
                    .display(string("none"))
                    .build())
                .payor(Reference.builder()
                    .display(string("none"))
                    .build())
                .period(Period.builder()
                    .start(DateTime.of("2020-01-01"))
                    .end(DateTime.of("2020-01-31"))
                    .build())
                .meta(Meta.builder()
                    .profile(Canonical.of("http://myurl.org/my-coverage"))
                    .build())
                .build();

        List<Issue> issues = FHIRValidator.validator().validate(coverage);
        assertEquals(countErrors(issues), 1);
        assertEquals(countWarnings(issues), 1);
        assertEquals(countInformation(issues), 1);
    }
}