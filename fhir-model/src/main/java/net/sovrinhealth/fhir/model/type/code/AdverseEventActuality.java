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

@System("http://hl7.org/fhir/adverse-event-actuality")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class AdverseEventActuality extends Code {
    /**
     * Adverse Event
     * 
     * <p>The adverse event actually happened regardless of whether anyone was affected or harmed.
     */
    public static final AdverseEventActuality ACTUAL = AdverseEventActuality.builder().value(Value.ACTUAL).build();

    /**
     * Potential Adverse Event
     * 
     * <p>A potential adverse event.
     */
    public static final AdverseEventActuality POTENTIAL = AdverseEventActuality.builder().value(Value.POTENTIAL).build();

    private volatile int hashCode;

    private AdverseEventActuality(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this AdverseEventActuality as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating AdverseEventActuality objects from a passed enum value.
     */
    public static AdverseEventActuality of(Value value) {
        switch (value) {
        case ACTUAL:
            return ACTUAL;
        case POTENTIAL:
            return POTENTIAL;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating AdverseEventActuality objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static AdverseEventActuality of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating AdverseEventActuality objects from a passed string value.
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
     * Inherited factory method for creating AdverseEventActuality objects from a passed string value.
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
        AdverseEventActuality other = (AdverseEventActuality) obj;
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
         *     An enum constant for AdverseEventActuality
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public AdverseEventActuality build() {
            AdverseEventActuality adverseEventActuality = new AdverseEventActuality(this);
            if (validating) {
                validate(adverseEventActuality);
            }
            return adverseEventActuality;
        }

        protected void validate(AdverseEventActuality adverseEventActuality) {
            super.validate(adverseEventActuality);
        }

        protected Builder from(AdverseEventActuality adverseEventActuality) {
            super.from(adverseEventActuality);
            return this;
        }
    }

    public enum Value {
        /**
         * Adverse Event
         * 
         * <p>The adverse event actually happened regardless of whether anyone was affected or harmed.
         */
        ACTUAL("actual"),

        /**
         * Potential Adverse Event
         * 
         * <p>A potential adverse event.
         */
        POTENTIAL("potential");

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
         * Factory method for creating AdverseEventActuality.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding AdverseEventActuality.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "actual":
                return ACTUAL;
            case "potential":
                return POTENTIAL;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
