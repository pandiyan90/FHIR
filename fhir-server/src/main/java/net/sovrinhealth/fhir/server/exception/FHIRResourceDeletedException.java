/*
 * (C) Copyright IBM Corp. 2017, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.exception;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.model.util.FHIRUtil;

public class FHIRResourceDeletedException extends FHIROperationException {
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception with a single OperationOutcome.Issue of type DELETED,
     * both with the passed message
     */
    public FHIRResourceDeletedException(String message) {
        super(message);
        withIssue(FHIRUtil.buildOperationOutcomeIssue(message, IssueType.DELETED));
    }

    /**
     * Create an exception with the passed message and cause and
     * a single OperationOutcome.Issue with the passed message and an IssueType of NOT_FOUND
     */
    public FHIRResourceDeletedException(String message, Throwable cause) {
        super(message, cause);
        withIssue(FHIRUtil.buildOperationOutcomeIssue(message, IssueType.DELETED));
    }
}