/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.constraint.spi;

import java.util.List;
import java.util.function.Predicate;

import net.sovrinhealth.fhir.model.annotation.Constraint;

/**
 * An abstract base class that extends {@link AbstractConstraintProvider} and implements {@link ModelConstraintProvider}
 */
public abstract class AbstractModelConstraintProvider extends AbstractConstraintProvider implements ModelConstraintProvider {
    @Override
    public abstract boolean appliesTo(Class<?> modelClass);

    @Override
    protected void addRemovalPredicates(List<Predicate<Constraint>> removalPredicates) {
        // do nothing
    }

    @Override
    protected void addConstraints(List<Constraint> constraints) {
        // do nothing
    }
}
