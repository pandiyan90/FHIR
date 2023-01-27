/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.type.code;

import net.sovrinhealth.fhir.model.annotation.System;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.String;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Generated;

@System("http://hl7.org/fhir/map-source-list-mode")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class StructureMapSourceListMode extends Code {
    /**
     * First
     * 
     * <p>Only process this rule for the first in the list.
     */
    public static final StructureMapSourceListMode FIRST = StructureMapSourceListMode.builder().value(Value.FIRST).build();

    /**
     * All but the first
     * 
     * <p>Process this rule for all but the first.
     */
    public static final StructureMapSourceListMode NOT_FIRST = StructureMapSourceListMode.builder().value(Value.NOT_FIRST).build();

    /**
     * Last
     * 
     * <p>Only process this rule for the last in the list.
     */
    public static final StructureMapSourceListMode LAST = StructureMapSourceListMode.builder().value(Value.LAST).build();

    /**
     * All but the last
     * 
     * <p>Process this rule for all but the last.
     */
    public static final StructureMapSourceListMode NOT_LAST = StructureMapSourceListMode.builder().value(Value.NOT_LAST).build();

    /**
     * Enforce only one
     * 
     * <p>Only process this rule is there is only item.
     */
    public static final StructureMapSourceListMode ONLY_ONE = StructureMapSourceListMode.builder().value(Value.ONLY_ONE).build();

    private volatile int hashCode;

    private StructureMapSourceListMode(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this StructureMapSourceListMode as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating StructureMapSourceListMode objects from a passed enum value.
     */
    public static StructureMapSourceListMode of(Value value) {
        switch (value) {
        case FIRST:
            return FIRST;
        case NOT_FIRST:
            return NOT_FIRST;
        case LAST:
            return LAST;
        case NOT_LAST:
            return NOT_LAST;
        case ONLY_ONE:
            return ONLY_ONE;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating StructureMapSourceListMode objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static StructureMapSourceListMode of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating StructureMapSourceListMode objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static String string(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating StructureMapSourceListMode objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static Code code(java.lang.String value) {
        return of(Value.from(value));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StructureMapSourceListMode other = (StructureMapSourceListMode) obj;
        return Objects.equals(id, other.id) && Objects.equals(extension, other.extension) && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, extension, value);
            hashCode = result;
        }
        return result;
    }

    public Builder toBuilder() {
        return new Builder().from(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Code.Builder {
        private Builder() {
            super();
        }

        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        @Override
        public Builder extension(Extension... extension) {
            return (Builder) super.extension(extension);
        }

        @Override
        public Builder extension(Collection<Extension> extension) {
            return (Builder) super.extension(extension);
        }

        @Override
        public Builder value(java.lang.String value) {
            return (value != null) ? (Builder) super.value(Value.from(value).value()) : this;
        }

        /**
         * Primitive value for code
         * 
         * @param value
         *     An enum constant for StructureMapSourceListMode
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public StructureMapSourceListMode build() {
            StructureMapSourceListMode structureMapSourceListMode = new StructureMapSourceListMode(this);
            if (validating) {
                validate(structureMapSourceListMode);
            }
            return structureMapSourceListMode;
        }

        protected void validate(StructureMapSourceListMode structureMapSourceListMode) {
            super.validate(structureMapSourceListMode);
        }

        protected Builder from(StructureMapSourceListMode structureMapSourceListMode) {
            super.from(structureMapSourceListMode);
            return this;
        }
    }

    public enum Value {
        /**
         * First
         * 
         * <p>Only process this rule for the first in the list.
         */
        FIRST("first"),

        /**
         * All but the first
         * 
         * <p>Process this rule for all but the first.
         */
        NOT_FIRST("not_first"),

        /**
         * Last
         * 
         * <p>Only process this rule for the last in the list.
         */
        LAST("last"),

        /**
         * All but the last
         * 
         * <p>Process this rule for all but the last.
         */
        NOT_LAST("not_last"),

        /**
         * Enforce only one
         * 
         * <p>Only process this rule is there is only item.
         */
        ONLY_ONE("only_one");

        private final java.lang.String value;

        Value(java.lang.String value) {
            this.value = value;
        }

        /**
         * @return
         *     The java.lang.String value of the code represented by this enum
         */
        public java.lang.String value() {
            return value;
        }

        /**
         * Factory method for creating StructureMapSourceListMode.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding StructureMapSourceListMode.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "first":
                return FIRST;
            case "not_first":
                return NOT_FIRST;
            case "last":
                return LAST;
            case "not_last":
                return NOT_LAST;
            case "only_one":
                return ONLY_ONE;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
