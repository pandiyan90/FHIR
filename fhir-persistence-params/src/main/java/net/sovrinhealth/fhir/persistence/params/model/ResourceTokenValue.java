/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.params.model;

import net.sovrinhealth.fhir.persistence.index.TokenParameter;

/**
 * Record representing a new row in _resource_token_refs
 */
public class ResourceTokenValue {
    private final String resourceType;
    private final String logicalId;
    private final long logicalResourceId;
    private final TokenParameter tokenParameter;
    private final CommonTokenValue commonTokenValue;

    /**
     * Public constructor
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param tokenParameter
     * @param commonTokenValue
     */
    public ResourceTokenValue(String resourceType, String logicalId, long logicalResourceId, TokenParameter tokenParameter, CommonTokenValue commonTokenValue) {
        this.resourceType = resourceType;
        this.logicalId = logicalId;
        this.logicalResourceId = logicalResourceId;
        this.tokenParameter = tokenParameter;
        this.commonTokenValue = commonTokenValue;
    }

    
    /**
     * @return the resourceType
     */
    public String getResourceType() {
        return resourceType;
    }

    
    /**
     * @return the logicalId
     */
    public String getLogicalId() {
        return logicalId;
    }

    
    /**
     * @return the logicalResourceId
     */
    public long getLogicalResourceId() {
        return logicalResourceId;
    }

    
    /**
     * @return the tokenParameter
     */
    public TokenParameter getTokenParameter() {
        return tokenParameter;
    }

    
    /**
     * @return the commonTokenValue
     */
    public CommonTokenValue getCommonTokenValue() {
        return commonTokenValue;
    }
}
