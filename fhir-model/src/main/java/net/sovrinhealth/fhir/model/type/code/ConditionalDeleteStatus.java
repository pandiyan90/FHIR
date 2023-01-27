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

@System("http://hl7.org/fhir/conditional-delete-status")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class ConditionalDeleteStatus extends Code {
    /**
     * Not Supported
     * 
     * <p>No support for conditional deletes.
     */
    public static final ConditionalDeleteStatus NOT_SUPPORTED = ConditionalDeleteStatus.builder().value(Value.NOT_SUPPORTED).build();

    /**
     * Single Deletes Supported
     * 
     * <p>Conditional deletes are supported, but only single resources at a time.
     */
    public static final ConditionalDeleteStatus SINGLE = ConditionalDeleteStatus.builder().value(Value.SINGLE).build();

    /**
     * Multiple Deletes Supported
     * 
     * <p>Conditional deletes are supported, and multiple resources can be deleted in a single interaction.
     */
    public static final ConditionalDeleteStatus MULTIPLE = ConditionalDeleteStatus.builder().value(Value.MULTIPLE).build();

    private volatile int hashCode;

    private ConditionalDeleteStatus(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this ConditionalDeleteStatus as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating ConditionalDeleteStatus objects from a passed enum value.
     */
    public static ConditionalDeleteStatus of(Value value) {
        switch (value) {
        case NOT_SUPPORTED:
            return NOT_SUPPORTED;
        case SINGLE:
            return SINGLE;
        case MULTIPLE:
            return MULTIPLE;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating ConditionalDeleteStatus objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static ConditionalDeleteStatus of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating ConditionalDeleteStatus objects from a passed string value.
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
     * Inherited factory method for creating ConditionalDeleteStatus objects from a passed string value.
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
        ConditionalDeleteStatus other = (ConditionalDeleteStatus) obj;
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
         *     An enum constant for ConditionalDeleteStatus
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public ConditionalDeleteStatus build() {
            ConditionalDeleteStatus conditionalDeleteStatus = new ConditionalDeleteStatus(this);
            if (validating) {
                validate(conditionalDeleteStatus);
            }
            return conditionalDeleteStatus;
        }

        protected void validate(ConditionalDeleteStatus conditionalDeleteStatus) {
            super.validate(conditionalDeleteStatus);
        }

        protected Builder from(ConditionalDeleteStatus conditionalDeleteStatus) {
            super.from(conditionalDeleteStatus);
            return this;
        }
    }

    public enum Value {
        /**
         * Not Supported
         * 
         * <p>No support for conditional deletes.
         */
        NOT_SUPPORTED("not-supported"),

        /**
         * Single Deletes Supported
         * 
         * <p>Conditional deletes are supported, but only single resources at a time.
         */
        SINGLE("single"),

        /**
         * Multiple Deletes Supported
         * 
         * <p>Conditional deletes are supported, and multiple resources can be deleted in a single interaction.
         */
        MULTIPLE("multiple");

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
         * Factory method for creating ConditionalDeleteStatus.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding ConditionalDeleteStatus.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "not-supported":
                return NOT_SUPPORTED;
            case "single":
                return SINGLE;
            case "multiple":
                return MULTIPLE;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
