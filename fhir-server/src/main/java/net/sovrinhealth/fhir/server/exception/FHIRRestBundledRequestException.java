/*
 * (C) Copyright IBM Corp. 2016, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.exception;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.Bundle;

public class FHIRRestBundledRequestException extends FHIROperationException {
    private static final long serialVersionUID = 1L;
    private Bundle responseBundle = null;

    public FHIRRestBundledRequestException(String message) {
        super(message);
    }

    public FHIRRestBundledRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public FHIRRestBundledRequestException(String message, Bundle responseBundle) {
        this(message, responseBundle, null);
    }

    public FHIRRestBundledRequestException(String message, Bundle responseBundle, Throwable t) {
        super(message, t);
        this.responseBundle = responseBundle;
    }

    public Bundle getResponseBundle() {
        return responseBundle;
    }
}
