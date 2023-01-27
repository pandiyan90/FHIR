/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.index;

import java.util.Objects;

/**
 * The base class for our search parameter values. These index model classes
 * are designed to reflect the raw values we want the remote indexing
 * service to store
 */
public class SearchParameterValue {
    // The name of the parameter
    private String name;

    // The composite id used to tie together values belonging to the same composite parameter. Null for ordinary params.
    private Integer compositeId;

    // True if this parameter should also be stored at the whole-system level
    private Boolean wholeSystem;

    /**
     * Add the base description of this parameter to the given {@link StringBuilder}
     * @param sb
     */
    protected void addDescription(StringBuilder sb) {
        sb.append(name);
        sb.append(",");
        sb.append(compositeId);
        sb.append(",");
        sb.append(wholeSystem);
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

    /**
     * @return the compositeId
     */
    public Integer getCompositeId() {
        return compositeId;
    }

    /**
     * @param compositeId the compositeId to set
     */
    public void setCompositeId(Integer compositeId) {
        this.compositeId = compositeId;
    }

    /**
     * @return the wholeSystem
     */
    public Boolean getWholeSystem() {
        return wholeSystem;
    }

    /**
     * Returns true iff the wholeSystem property is not null and true
     * @return
     */
    public boolean isSystemParam() {
        return this.wholeSystem != null && this.wholeSystem.booleanValue();
    }

    /**
     * @param wholeSystem the wholeSystem to set
     */
    public void setWholeSystem(Boolean wholeSystem) {
        this.wholeSystem = wholeSystem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, compositeId, wholeSystem);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SearchParameterValue) {
            SearchParameterValue that = (SearchParameterValue)obj;
            return Objects.equals(this.name, that.name)
                    && Objects.equals(this.compositeId, that.compositeId)
                    && Objects.equals(this.wholeSystem, that.wholeSystem);
        }
        return false;
    }
}
