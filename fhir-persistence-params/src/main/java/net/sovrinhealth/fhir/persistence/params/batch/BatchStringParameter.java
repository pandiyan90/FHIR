/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.params.batch;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.index.StringParameter;
import net.sovrinhealth.fhir.persistence.params.api.IBatchParameterProcessor;
import net.sovrinhealth.fhir.persistence.params.api.BatchParameterValue;
import net.sovrinhealth.fhir.persistence.params.model.ParameterNameValue;

/**
 * A string parameter we are collecting to batch
 */
public class BatchStringParameter extends BatchParameterValue {
    private final StringParameter parameter;
    
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
    public BatchStringParameter(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, StringParameter parameter) {
        super(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue);
        this.parameter = parameter;
    }

    @Override
    public void apply(IBatchParameterProcessor processor) throws FHIRPersistenceException {
        processor.process(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue, parameter);
    }
}
