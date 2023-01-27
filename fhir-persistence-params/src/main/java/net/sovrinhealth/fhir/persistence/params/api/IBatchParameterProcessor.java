/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.params.api;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.index.DateParameter;
import net.sovrinhealth.fhir.persistence.index.LocationParameter;
import net.sovrinhealth.fhir.persistence.index.NumberParameter;
import net.sovrinhealth.fhir.persistence.index.ProfileParameter;
import net.sovrinhealth.fhir.persistence.index.QuantityParameter;
import net.sovrinhealth.fhir.persistence.index.ReferenceParameter;
import net.sovrinhealth.fhir.persistence.index.SecurityParameter;
import net.sovrinhealth.fhir.persistence.index.StringParameter;
import net.sovrinhealth.fhir.persistence.index.TagParameter;
import net.sovrinhealth.fhir.persistence.index.TokenParameter;
import net.sovrinhealth.fhir.persistence.params.model.CodeSystemValue;
import net.sovrinhealth.fhir.persistence.params.model.CommonCanonicalValue;
import net.sovrinhealth.fhir.persistence.params.model.CommonTokenValue;
import net.sovrinhealth.fhir.persistence.params.model.LogicalResourceIdentValue;
import net.sovrinhealth.fhir.persistence.params.model.ParameterNameValue;

/**
 * Processes batched parameters
 */
public interface IBatchParameterProcessor {

    /**
     * Compute the shard key value use to distribute resources among nodes
     * of the database
     * @param requestShard
     * @return
     */
    Short encodeShardKey(String requestShard);

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, StringParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, NumberParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, QuantityParameter parameter, CodeSystemValue codeSystemValue) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, LocationParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, DateParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, TokenParameter parameter, CommonTokenValue commonTokenValue) throws FHIRPersistenceException;

    /**
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param commonTokenValue
     * @throws FHIRPersistenceException
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, TagParameter parameter, CommonTokenValue commonTokenValue) throws FHIRPersistenceException;

    /**
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param commonCanonicalValue
     * @throws FHIRPersistenceException
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, ProfileParameter parameter, CommonCanonicalValue commonCanonicalValue) throws FHIRPersistenceException;

    /**
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param commonCanonicalValue
     * @throws FHIRPersistenceException
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, SecurityParameter parameter, CommonTokenValue commonTokenValue) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param refLogicalResourceId
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue,
        ReferenceParameter parameter, LogicalResourceIdentValue refLogicalResourceId) throws FHIRPersistenceException;
}
