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

@System("http://hl7.org/fhir/report-participant-type")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class TestReportParticipantType extends Code {
    /**
     * Test Engine
     * 
     * <p>The test execution engine.
     */
    public static final TestReportParticipantType TEST_ENGINE = TestReportParticipantType.builder().value(Value.TEST_ENGINE).build();

    /**
     * Client
     * 
     * <p>A FHIR Client.
     */
    public static final TestReportParticipantType CLIENT = TestReportParticipantType.builder().value(Value.CLIENT).build();

    /**
     * Server
     * 
     * <p>A FHIR Server.
     */
    public static final TestReportParticipantType SERVER = TestReportParticipantType.builder().value(Value.SERVER).build();

    private volatile int hashCode;

    private TestReportParticipantType(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this TestReportParticipantType as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating TestReportParticipantType objects from a passed enum value.
     */
    public static TestReportParticipantType of(Value value) {
        switch (value) {
        case TEST_ENGINE:
            return TEST_ENGINE;
        case CLIENT:
            return CLIENT;
        case SERVER:
            return SERVER;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating TestReportParticipantType objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static TestReportParticipantType of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating TestReportParticipantType objects from a passed string value.
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
     * Inherited factory method for creating TestReportParticipantType objects from a passed string value.
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
        TestReportParticipantType other = (TestReportParticipantType) obj;
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
         *     An enum constant for TestReportParticipantType
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public TestReportParticipantType build() {
            TestReportParticipantType testReportParticipantType = new TestReportParticipantType(this);
            if (validating) {
                validate(testReportParticipantType);
            }
            return testReportParticipantType;
        }

        protected void validate(TestReportParticipantType testReportParticipantType) {
            super.validate(testReportParticipantType);
        }

        protected Builder from(TestReportParticipantType testReportParticipantType) {
            super.from(testReportParticipantType);
            return this;
        }
    }

    public enum Value {
        /**
         * Test Engine
         * 
         * <p>The test execution engine.
         */
        TEST_ENGINE("test-engine"),

        /**
         * Client
         * 
         * <p>A FHIR Client.
         */
        CLIENT("client"),

        /**
         * Server
         * 
         * <p>A FHIR Server.
         */
        SERVER("server");

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
         * Factory method for creating TestReportParticipantType.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding TestReportParticipantType.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "test-engine":
                return TEST_ENGINE;
            case "client":
                return CLIENT;
            case "server":
                return SERVER;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
