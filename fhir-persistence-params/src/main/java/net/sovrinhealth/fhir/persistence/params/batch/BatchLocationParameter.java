/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.params.batch;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.index.LocationParameter;
import net.sovrinhealth.fhir.persistence.params.api.IBatchParameterProcessor;
import net.sovrinhealth.fhir.persistence.params.api.BatchParameterValue;
import net.sovrinhealth.fhir.persistence.params.model.ParameterNameValue;

/**
 * A location parameter we are collecting to batch
 */
public class BatchLocationParameter extends BatchParameterValue {
    private final LocationParameter parameter;
    
    /**
     * Canonical constructor
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    public BatchLocationParameter(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, LocationParameter parameter) {
        super(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue);
        this.parameter = parameter;
    }

    @Override
    public void apply(IBatchParameterProcessor processor) throws FHIRPersistenceException {
        processor.process(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue, parameter);
    }
}
