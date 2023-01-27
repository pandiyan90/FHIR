/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.schema.model;

/**
 * DTO to describe a resource type stored in the resource_types tables
 */
public class ResourceType {
    // the database-assigned id
    private int id;

    // the name of the resource type
    private String name;

    /**
     * Default constructor
     */
    public ResourceType() {
    }

    /**
     * Public constructor
     * @param id
     * @param name
     */
    public ResourceType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name + "[" + this.id + "]";
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}