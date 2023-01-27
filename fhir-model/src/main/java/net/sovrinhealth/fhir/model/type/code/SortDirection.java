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

@System("http://hl7.org/fhir/sort-direction")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class SortDirection extends Code {
    /**
     * Ascending
     * 
     * <p>Sort by the value ascending, so that lower values appear first.
     */
    public static final SortDirection ASCENDING = SortDirection.builder().value(Value.ASCENDING).build();

    /**
     * Descending
     * 
     * <p>Sort by the value descending, so that lower values appear last.
     */
    public static final SortDirection DESCENDING = SortDirection.builder().value(Value.DESCENDING).build();

    private volatile int hashCode;

    private SortDirection(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this SortDirection as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating SortDirection objects from a passed enum value.
     */
    public static SortDirection of(Value value) {
        switch (value) {
        case ASCENDING:
            return ASCENDING;
        case DESCENDING:
            return DESCENDING;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating SortDirection objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static SortDirection of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating SortDirection objects from a passed string value.
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
     * Inherited factory method for creating SortDirection objects from a passed string value.
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
        SortDirection other = (SortDirection) obj;
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
         *     An enum constant for SortDirection
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public SortDirection build() {
            SortDirection sortDirection = new SortDirection(this);
            if (validating) {
                validate(sortDirection);
            }
            return sortDirection;
        }

        protected void validate(SortDirection sortDirection) {
            super.validate(sortDirection);
        }

        protected Builder from(SortDirection sortDirection) {
            super.from(sortDirection);
            return this;
        }
    }

    public enum Value {
        /**
         * Ascending
         * 
         * <p>Sort by the value ascending, so that lower values appear first.
         */
        ASCENDING("ascending"),

        /**
         * Descending
         * 
         * <p>Sort by the value descending, so that lower values appear last.
         */
        DESCENDING("descending");

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
         * Factory method for creating SortDirection.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding SortDirection.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "ascending":
                return ASCENDING;
            case "descending":
                return DESCENDING;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
