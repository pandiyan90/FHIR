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

@System("http://hl7.org/fhir/response-code")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class ResponseType extends Code {
    /**
     * OK
     * 
     * <p>The message was accepted and processed without error.
     */
    public static final ResponseType OK = ResponseType.builder().value(Value.OK).build();

    /**
     * Transient Error
     * 
     * <p>Some internal unexpected error occurred - wait and try again. Note - this is usually used for things like database 
     * unavailable, which may be expected to resolve, though human intervention may be required.
     */
    public static final ResponseType TRANSIENT_ERROR = ResponseType.builder().value(Value.TRANSIENT_ERROR).build();

    /**
     * Fatal Error
     * 
     * <p>The message was rejected because of a problem with the content. There is no point in re-sending without change. The 
     * response narrative SHALL describe the issue.
     */
    public static final ResponseType FATAL_ERROR = ResponseType.builder().value(Value.FATAL_ERROR).build();

    private volatile int hashCode;

    private ResponseType(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this ResponseType as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating ResponseType objects from a passed enum value.
     */
    public static ResponseType of(Value value) {
        switch (value) {
        case OK:
            return OK;
        case TRANSIENT_ERROR:
            return TRANSIENT_ERROR;
        case FATAL_ERROR:
            return FATAL_ERROR;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating ResponseType objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static ResponseType of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating ResponseType objects from a passed string value.
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
     * Inherited factory method for creating ResponseType objects from a passed string value.
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
        ResponseType other = (ResponseType) obj;
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
         *     An enum constant for ResponseType
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public ResponseType build() {
            ResponseType responseType = new ResponseType(this);
            if (validating) {
                validate(responseType);
            }
            return responseType;
        }

        protected void validate(ResponseType responseType) {
            super.validate(responseType);
        }

        protected Builder from(ResponseType responseType) {
            super.from(responseType);
            return this;
        }
    }

    public enum Value {
        /**
         * OK
         * 
         * <p>The message was accepted and processed without error.
         */
        OK("ok"),

        /**
         * Transient Error
         * 
         * <p>Some internal unexpected error occurred - wait and try again. Note - this is usually used for things like database 
         * unavailable, which may be expected to resolve, though human intervention may be required.
         */
        TRANSIENT_ERROR("transient-error"),

        /**
         * Fatal Error
         * 
         * <p>The message was rejected because of a problem with the content. There is no point in re-sending without change. The 
         * response narrative SHALL describe the issue.
         */
        FATAL_ERROR("fatal-error");

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
         * Factory method for creating ResponseType.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding ResponseType.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "ok":
                return OK;
            case "transient-error":
                return TRANSIENT_ERROR;
            case "fatal-error":
                return FATAL_ERROR;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
