/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.bulkdata.jbatch.load.exception;

import net.sovrinhealth.fhir.exception.FHIRException;


public class FHIRLoadException extends FHIRException {

    private static final long serialVersionUID = 12434123435239981L;

    public FHIRLoadException(String msg) {
        super(msg);
    }
}