/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.spi.operation;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.search.util.SearchHelper;

public interface FHIROperation {
    String getName();

    /**
     * Invoke the operation.
     *
     * @throws FHIROperationException
     *     if input or output parameters fail validation or an exception occurs
     */
    Parameters invoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId, Parameters parameters,
        FHIRResourceHelpers resourceHelpers, SearchHelper searchHelper) throws FHIROperationException;

    OperationDefinition getDefinition();
}
