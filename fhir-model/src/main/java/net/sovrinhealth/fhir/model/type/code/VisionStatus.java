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

@System("http://hl7.org/fhir/fm-status")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class VisionStatus extends Code {
    /**
     * Active
     * 
     * <p>The instance is currently in-force.
     */
    public static final VisionStatus ACTIVE = VisionStatus.builder().value(Value.ACTIVE).build();

    /**
     * Cancelled
     * 
     * <p>The instance is withdrawn, rescinded or reversed.
     */
    public static final VisionStatus CANCELLED = VisionStatus.builder().value(Value.CANCELLED).build();

    /**
     * Draft
     * 
     * <p>A new instance the contents of which is not complete.
     */
    public static final VisionStatus DRAFT = VisionStatus.builder().value(Value.DRAFT).build();

    /**
     * Entered in Error
     * 
     * <p>The instance was entered in error.
     */
    public static final VisionStatus ENTERED_IN_ERROR = VisionStatus.builder().value(Value.ENTERED_IN_ERROR).build();

    private volatile int hashCode;

    private VisionStatus(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this VisionStatus as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating VisionStatus objects from a passed enum value.
     */
    public static VisionStatus of(Value value) {
        switch (value) {
        case ACTIVE:
            return ACTIVE;
        case CANCELLED:
            return CANCELLED;
        case DRAFT:
            return DRAFT;
        case ENTERED_IN_ERROR:
            return ENTERED_IN_ERROR;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating VisionStatus objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static VisionStatus of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating VisionStatus objects from a passed string value.
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
     * Inherited factory method for creating VisionStatus objects from a passed string value.
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
        VisionStatus other = (VisionStatus) obj;
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
         *     An enum constant for VisionStatus
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public VisionStatus build() {
            VisionStatus visionStatus = new VisionStatus(this);
            if (validating) {
                validate(visionStatus);
            }
            return visionStatus;
        }

        protected void validate(VisionStatus visionStatus) {
            super.validate(visionStatus);
        }

        protected Builder from(VisionStatus visionStatus) {
            super.from(visionStatus);
            return this;
        }
    }

    public enum Value {
        /**
         * Active
         * 
         * <p>The instance is currently in-force.
         */
        ACTIVE("active"),

        /**
         * Cancelled
         * 
         * <p>The instance is withdrawn, rescinded or reversed.
         */
        CANCELLED("cancelled"),

        /**
         * Draft
         * 
         * <p>A new instance the contents of which is not complete.
         */
        DRAFT("draft"),

        /**
         * Entered in Error
         * 
         * <p>The instance was entered in error.
         */
        ENTERED_IN_ERROR("entered-in-error");

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
         * Factory method for creating VisionStatus.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding VisionStatus.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "active":
                return ACTIVE;
            case "cancelled":
                return CANCELLED;
            case "draft":
                return DRAFT;
            case "entered-in-error":
                return ENTERED_IN_ERROR;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
