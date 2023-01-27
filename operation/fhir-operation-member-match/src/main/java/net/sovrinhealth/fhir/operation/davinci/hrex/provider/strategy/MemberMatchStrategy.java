/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.davinci.hrex.provider.strategy;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * The Member Match Strategy
 */
public interface MemberMatchStrategy {

    /**
     * used to uniquely identify the strategy.
     * @implNote "default" is reserved.
     * @return the member match strategy identifier
     */
    String getMemberMatchIdentifier();

    /**
     * takes a set of input parameters
     * @param ctx
     * @param input
     * @param resourceHelper
     * @return
     * @throws FHIROperationException
     */
    Parameters execute(FHIROperationContext ctx, Parameters input, FHIRResourceHelpers resourceHelper) throws FHIROperationException;
}
