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

@System("http://hl7.org/fhir/specimen-contained-preference")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class SpecimenContainedPreference extends Code {
    /**
     * Preferred
     * 
     * <p>This type of contained specimen is preferred to collect this kind of specimen.
     */
    public static final SpecimenContainedPreference PREFERRED = SpecimenContainedPreference.builder().value(Value.PREFERRED).build();

    /**
     * Alternate
     * 
     * <p>This type of conditioned specimen is an alternate.
     */
    public static final SpecimenContainedPreference ALTERNATE = SpecimenContainedPreference.builder().value(Value.ALTERNATE).build();

    private volatile int hashCode;

    private SpecimenContainedPreference(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this SpecimenContainedPreference as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating SpecimenContainedPreference objects from a passed enum value.
     */
    public static SpecimenContainedPreference of(Value value) {
        switch (value) {
        case PREFERRED:
            return PREFERRED;
        case ALTERNATE:
            return ALTERNATE;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating SpecimenContainedPreference objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static SpecimenContainedPreference of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating SpecimenContainedPreference objects from a passed string value.
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
     * Inherited factory method for creating SpecimenContainedPreference objects from a passed string value.
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
        SpecimenContainedPreference other = (SpecimenContainedPreference) obj;
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
         *     An enum constant for SpecimenContainedPreference
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public SpecimenContainedPreference build() {
            SpecimenContainedPreference specimenContainedPreference = new SpecimenContainedPreference(this);
            if (validating) {
                validate(specimenContainedPreference);
            }
            return specimenContainedPreference;
        }

        protected void validate(SpecimenContainedPreference specimenContainedPreference) {
            super.validate(specimenContainedPreference);
        }

        protected Builder from(SpecimenContainedPreference specimenContainedPreference) {
            super.from(specimenContainedPreference);
            return this;
        }
    }

    public enum Value {
        /**
         * Preferred
         * 
         * <p>This type of contained specimen is preferred to collect this kind of specimen.
         */
        PREFERRED("preferred"),

        /**
         * Alternate
         * 
         * <p>This type of conditioned specimen is an alternate.
         */
        ALTERNATE("alternate");

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
         * Factory method for creating SpecimenContainedPreference.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding SpecimenContainedPreference.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "preferred":
                return PREFERRED;
            case "alternate":
                return ALTERNATE;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
