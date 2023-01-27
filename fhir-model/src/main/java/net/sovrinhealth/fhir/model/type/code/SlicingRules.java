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

@System("http://hl7.org/fhir/resource-slicing-rules")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class SlicingRules extends Code {
    /**
     * Closed
     * 
     * <p>No additional content is allowed other than that described by the slices in this profile.
     */
    public static final SlicingRules CLOSED = SlicingRules.builder().value(Value.CLOSED).build();

    /**
     * Open
     * 
     * <p>Additional content is allowed anywhere in the list.
     */
    public static final SlicingRules OPEN = SlicingRules.builder().value(Value.OPEN).build();

    /**
     * Open at End
     * 
     * <p>Additional content is allowed, but only at the end of the list. Note that using this requires that the slices be 
     * ordered, which makes it hard to share uses. This should only be done where absolutely required.
     */
    public static final SlicingRules OPEN_AT_END = SlicingRules.builder().value(Value.OPEN_AT_END).build();

    private volatile int hashCode;

    private SlicingRules(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this SlicingRules as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating SlicingRules objects from a passed enum value.
     */
    public static SlicingRules of(Value value) {
        switch (value) {
        case CLOSED:
            return CLOSED;
        case OPEN:
            return OPEN;
        case OPEN_AT_END:
            return OPEN_AT_END;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating SlicingRules objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static SlicingRules of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating SlicingRules objects from a passed string value.
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
     * Inherited factory method for creating SlicingRules objects from a passed string value.
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
        SlicingRules other = (SlicingRules) obj;
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
         *     An enum constant for SlicingRules
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public SlicingRules build() {
            SlicingRules slicingRules = new SlicingRules(this);
            if (validating) {
                validate(slicingRules);
            }
            return slicingRules;
        }

        protected void validate(SlicingRules slicingRules) {
            super.validate(slicingRules);
        }

        protected Builder from(SlicingRules slicingRules) {
            super.from(slicingRules);
            return this;
        }
    }

    public enum Value {
        /**
         * Closed
         * 
         * <p>No additional content is allowed other than that described by the slices in this profile.
         */
        CLOSED("closed"),

        /**
         * Open
         * 
         * <p>Additional content is allowed anywhere in the list.
         */
        OPEN("open"),

        /**
         * Open at End
         * 
         * <p>Additional content is allowed, but only at the end of the list. Note that using this requires that the slices be 
         * ordered, which makes it hard to share uses. This should only be done where absolutely required.
         */
        OPEN_AT_END("openAtEnd");

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
         * Factory method for creating SlicingRules.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding SlicingRules.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "closed":
                return CLOSED;
            case "open":
                return OPEN;
            case "openAtEnd":
                return OPEN_AT_END;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
