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

@System("http://hl7.org/fhir/assert-direction-codes")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class AssertionDirectionType extends Code {
    /**
     * response
     * 
     * <p>The assertion is evaluated on the response. This is the default value.
     */
    public static final AssertionDirectionType RESPONSE = AssertionDirectionType.builder().value(Value.RESPONSE).build();

    /**
     * request
     * 
     * <p>The assertion is evaluated on the request.
     */
    public static final AssertionDirectionType REQUEST = AssertionDirectionType.builder().value(Value.REQUEST).build();

    private volatile int hashCode;

    private AssertionDirectionType(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this AssertionDirectionType as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating AssertionDirectionType objects from a passed enum value.
     */
    public static AssertionDirectionType of(Value value) {
        switch (value) {
        case RESPONSE:
            return RESPONSE;
        case REQUEST:
            return REQUEST;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating AssertionDirectionType objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static AssertionDirectionType of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating AssertionDirectionType objects from a passed string value.
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
     * Inherited factory method for creating AssertionDirectionType objects from a passed string value.
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
        AssertionDirectionType other = (AssertionDirectionType) obj;
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
         *     An enum constant for AssertionDirectionType
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public AssertionDirectionType build() {
            AssertionDirectionType assertionDirectionType = new AssertionDirectionType(this);
            if (validating) {
                validate(assertionDirectionType);
            }
            return assertionDirectionType;
        }

        protected void validate(AssertionDirectionType assertionDirectionType) {
            super.validate(assertionDirectionType);
        }

        protected Builder from(AssertionDirectionType assertionDirectionType) {
            super.from(assertionDirectionType);
            return this;
        }
    }

    public enum Value {
        /**
         * response
         * 
         * <p>The assertion is evaluated on the response. This is the default value.
         */
        RESPONSE("response"),

        /**
         * request
         * 
         * <p>The assertion is evaluated on the request.
         */
        REQUEST("request");

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
         * Factory method for creating AssertionDirectionType.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding AssertionDirectionType.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "response":
                return RESPONSE;
            case "request":
                return REQUEST;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
