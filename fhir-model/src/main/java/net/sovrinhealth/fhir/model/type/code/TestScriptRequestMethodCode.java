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

@System("http://hl7.org/fhir/http-operations")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class TestScriptRequestMethodCode extends Code {
    /**
     * DELETE
     * 
     * <p>HTTP DELETE operation.
     */
    public static final TestScriptRequestMethodCode DELETE = TestScriptRequestMethodCode.builder().value(Value.DELETE).build();

    /**
     * GET
     * 
     * <p>HTTP GET operation.
     */
    public static final TestScriptRequestMethodCode GET = TestScriptRequestMethodCode.builder().value(Value.GET).build();

    /**
     * OPTIONS
     * 
     * <p>HTTP OPTIONS operation.
     */
    public static final TestScriptRequestMethodCode OPTIONS = TestScriptRequestMethodCode.builder().value(Value.OPTIONS).build();

    /**
     * PATCH
     * 
     * <p>HTTP PATCH operation.
     */
    public static final TestScriptRequestMethodCode PATCH = TestScriptRequestMethodCode.builder().value(Value.PATCH).build();

    /**
     * POST
     * 
     * <p>HTTP POST operation.
     */
    public static final TestScriptRequestMethodCode POST = TestScriptRequestMethodCode.builder().value(Value.POST).build();

    /**
     * PUT
     * 
     * <p>HTTP PUT operation.
     */
    public static final TestScriptRequestMethodCode PUT = TestScriptRequestMethodCode.builder().value(Value.PUT).build();

    /**
     * HEAD
     * 
     * <p>HTTP HEAD operation.
     */
    public static final TestScriptRequestMethodCode HEAD = TestScriptRequestMethodCode.builder().value(Value.HEAD).build();

    private volatile int hashCode;

    private TestScriptRequestMethodCode(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this TestScriptRequestMethodCode as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating TestScriptRequestMethodCode objects from a passed enum value.
     */
    public static TestScriptRequestMethodCode of(Value value) {
        switch (value) {
        case DELETE:
            return DELETE;
        case GET:
            return GET;
        case OPTIONS:
            return OPTIONS;
        case PATCH:
            return PATCH;
        case POST:
            return POST;
        case PUT:
            return PUT;
        case HEAD:
            return HEAD;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating TestScriptRequestMethodCode objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static TestScriptRequestMethodCode of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating TestScriptRequestMethodCode objects from a passed string value.
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
     * Inherited factory method for creating TestScriptRequestMethodCode objects from a passed string value.
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
        TestScriptRequestMethodCode other = (TestScriptRequestMethodCode) obj;
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
         *     An enum constant for TestScriptRequestMethodCode
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public TestScriptRequestMethodCode build() {
            TestScriptRequestMethodCode testScriptRequestMethodCode = new TestScriptRequestMethodCode(this);
            if (validating) {
                validate(testScriptRequestMethodCode);
            }
            return testScriptRequestMethodCode;
        }

        protected void validate(TestScriptRequestMethodCode testScriptRequestMethodCode) {
            super.validate(testScriptRequestMethodCode);
        }

        protected Builder from(TestScriptRequestMethodCode testScriptRequestMethodCode) {
            super.from(testScriptRequestMethodCode);
            return this;
        }
    }

    public enum Value {
        /**
         * DELETE
         * 
         * <p>HTTP DELETE operation.
         */
        DELETE("delete"),

        /**
         * GET
         * 
         * <p>HTTP GET operation.
         */
        GET("get"),

        /**
         * OPTIONS
         * 
         * <p>HTTP OPTIONS operation.
         */
        OPTIONS("options"),

        /**
         * PATCH
         * 
         * <p>HTTP PATCH operation.
         */
        PATCH("patch"),

        /**
         * POST
         * 
         * <p>HTTP POST operation.
         */
        POST("post"),

        /**
         * PUT
         * 
         * <p>HTTP PUT operation.
         */
        PUT("put"),

        /**
         * HEAD
         * 
         * <p>HTTP HEAD operation.
         */
        HEAD("head");

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
         * Factory method for creating TestScriptRequestMethodCode.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding TestScriptRequestMethodCode.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "delete":
                return DELETE;
            case "get":
                return GET;
            case "options":
                return OPTIONS;
            case "patch":
                return PATCH;
            case "post":
                return POST;
            case "put":
                return PUT;
            case "head":
                return HEAD;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
