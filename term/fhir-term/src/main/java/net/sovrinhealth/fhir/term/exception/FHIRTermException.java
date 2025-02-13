/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.term.exception;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;
import net.sovrinhealth.fhir.term.util.ConceptMapSupport;
import net.sovrinhealth.fhir.term.util.ValueSetSupport;

/**
 * A runtime exception class intended to be thrown by the {@link CodeSystemSupport}, {@link ConceptMapSupport},
 * and {@link ValueSetSupport} utility classes
 */
public class FHIRTermException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected final List<Issue> issues;

    public FHIRTermException(String message, List<Issue> issues) {
        super(message);
        this.issues = Collections.unmodifiableList(Objects.requireNonNull(issues, "issues"));
    }

    public List<Issue> getIssues() {
        return issues;
    }
}
