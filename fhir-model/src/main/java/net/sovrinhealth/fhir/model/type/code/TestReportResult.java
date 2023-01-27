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

@System("http://hl7.org/fhir/report-result-codes")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class TestReportResult extends Code {
    /**
     * Pass
     * 
     * <p>All test operations successfully passed all asserts.
     */
    public static final TestReportResult PASS = TestReportResult.builder().value(Value.PASS).build();

    /**
     * Fail
     * 
     * <p>One or more test operations failed one or more asserts.
     */
    public static final TestReportResult FAIL = TestReportResult.builder().value(Value.FAIL).build();

    /**
     * Pending
     * 
     * <p>One or more test operations is pending execution completion.
     */
    public static final TestReportResult PENDING = TestReportResult.builder().value(Value.PENDING).build();

    private volatile int hashCode;

    private TestReportResult(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this TestReportResult as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating TestReportResult objects from a passed enum value.
     */
    public static TestReportResult of(Value value) {
        switch (value) {
        case PASS:
            return PASS;
        case FAIL:
            return FAIL;
        case PENDING:
            return PENDING;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating TestReportResult objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static TestReportResult of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating TestReportResult objects from a passed string value.
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
     * Inherited factory method for creating TestReportResult objects from a passed string value.
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
        TestReportResult other = (TestReportResult) obj;
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
         *     An enum constant for TestReportResult
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public TestReportResult build() {
            TestReportResult testReportResult = new TestReportResult(this);
            if (validating) {
                validate(testReportResult);
            }
            return testReportResult;
        }

        protected void validate(TestReportResult testReportResult) {
            super.validate(testReportResult);
        }

        protected Builder from(TestReportResult testReportResult) {
            super.from(testReportResult);
            return this;
        }
    }

    public enum Value {
        /**
         * Pass
         * 
         * <p>All test operations successfully passed all asserts.
         */
        PASS("pass"),

        /**
         * Fail
         * 
         * <p>One or more test operations failed one or more asserts.
         */
        FAIL("fail"),

        /**
         * Pending
         * 
         * <p>One or more test operations is pending execution completion.
         */
        PENDING("pending");

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
         * Factory method for creating TestReportResult.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding TestReportResult.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "pass":
                return PASS;
            case "fail":
                return FAIL;
            case "pending":
                return PENDING;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
