/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.constraint.test;

import java.util.List;
import java.util.function.Predicate;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.constraint.spi.AbstractModelConstraintProvider;
import net.sovrinhealth.fhir.model.resource.Patient;

public class TestModelConstraintProvider extends AbstractModelConstraintProvider {
    @Override
    public boolean appliesTo(Class<?> modelClass) {
        return Patient.class.equals(modelClass);
    }

    @Override
    protected void addRemovalPredicates(List<Predicate<Constraint>> removalPredicates) {
        removalPredicates.add(idEquals("pat-1"));
    }

    @Override
    protected void addConstraints(List<Constraint> constraints) {
        constraints.add(constraint(
            "added-pat-1",
            Constraint.LEVEL_RULE,
            "Patient.contact",
            "SHALL at least contain a contact's details or a reference to an organization",
            "name.exists() or telecom.exists() or address.exists() or organization.exists()",
            "http://hl7.org/fhir/StructureDefinition/Patient"));
    }
}
