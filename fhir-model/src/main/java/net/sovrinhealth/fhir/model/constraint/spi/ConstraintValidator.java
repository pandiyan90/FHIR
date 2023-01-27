/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.constraint.spi;

import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.model.visitor.Visitable;

/**
 * An interface for programmatically evaluating constraints against a validation target {@link Element} or {@link Resource}
 */
public interface ConstraintValidator<T extends Visitable> {
    /**
     * Indicates whether an element or resource is valid with respect to the given constraint
     *
     * @param elementOrResource
     *     the element or resource
     * @param constraint
     *     the constraint
     * @return
     *     true if the element or resource is valid with respect to the given constraint, false otherwise
     */
    boolean isValid(T elementOrResource, Constraint constraint);
}
