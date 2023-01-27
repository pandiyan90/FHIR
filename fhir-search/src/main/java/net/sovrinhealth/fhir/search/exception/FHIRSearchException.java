/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.exception;

import java.util.Collection;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;

/**
 * 
 * @author pbastide 
 *
 */
public class FHIRSearchException extends FHIROperationException {
    private static final long serialVersionUID = 1L;

    public FHIRSearchException(String message) {
        super(message);
    }

    public FHIRSearchException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public FHIRSearchException withIssue(OperationOutcome.Issue... issues) {
        super.withIssue(issues);
        return this;
    }

    @Override
    public FHIRSearchException withIssue(Collection<OperationOutcome.Issue> issues) {
        super.withIssue(issues);
        return this;
    }
}
