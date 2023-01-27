/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.flow.impl;

import net.sovrinhealth.fhir.flow.api.ResourceIdentifierVersion;
import net.sovrinhealth.fhir.model.resource.Resource;

/**
 * The result from reading the resource from the upstream system
 */
public class FlowFetchResult {
    private int status;
    // The location of the resource in the upstream server
    private ResourceIdentifierVersion location;

    // The resource can be carried in either its on-wire data form
    private String resourceData; // is faster

    // ...or as a parsed Resource value, depending on user preference
    private Resource resource; // will be slower

    /**
     * Public constructor
     * 
     * @param location
     */
    public FlowFetchResult(ResourceIdentifierVersion location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return location.toString();
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * @return the resource
     */
    public Resource getResource() {
        return resource;
    }
    
    /**
     * @param resource the resource to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * @return the resourceData
     */
    public String getResourceData() {
        return resourceData;
    }

    /**
     * @param resourceData the resourceData to set
     */
    public void setResourceData(String resourceData) {
        this.resourceData = resourceData;
    }
}
