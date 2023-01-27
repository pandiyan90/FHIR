/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.jdbc.dto;


/**
 * A DTO representing an erased resource from ERASED_RESOURCES
 */
public class ErasedResourceRec {
    private final long erasedResourceId;
    private final int resourceTypeId;
    private final String logicalId;
    private final Integer versionId;
    
    /**
     * Public constructor
     * @param erasedResourceId
     * @param resourceTypeId
     * @param logicalId
     * @param versionId
     */
    public ErasedResourceRec(long erasedResourceId, int resourceTypeId, String logicalId, Integer versionId) {
        this.erasedResourceId = erasedResourceId;
        this.resourceTypeId = resourceTypeId;
        this.logicalId = logicalId;
        this.versionId = versionId;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("ErasedResourceRec[")
            .append(erasedResourceId)
            .append(", ").append(resourceTypeId)
            .append(", ").append(logicalId)
            .append(", ").append(versionId)
            .append("]");
            ;
        return result.toString();
    }
    
    /**
     * @return the erasedResourceId
     */
    public long getErasedResourceId() {
        return erasedResourceId;
    }

    
    /**
     * @return the resourceTypeId
     */
    public int getResourceTypeId() {
        return resourceTypeId;
    }

    
    /**
     * @return the logicalId
     */
    public String getLogicalId() {
        return logicalId;
    }

    
    /**
     * @return the versionId
     */
    public Integer getVersionId() {
        return versionId;
    }
}