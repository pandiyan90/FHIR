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

@System("http://hl7.org/fhir/composition-attestation-mode")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class CompositionAttestationMode extends Code {
    /**
     * Personal
     * 
     * <p>The person authenticated the content in their personal capacity.
     */
    public static final CompositionAttestationMode PERSONAL = CompositionAttestationMode.builder().value(Value.PERSONAL).build();

    /**
     * Professional
     * 
     * <p>The person authenticated the content in their professional capacity.
     */
    public static final CompositionAttestationMode PROFESSIONAL = CompositionAttestationMode.builder().value(Value.PROFESSIONAL).build();

    /**
     * Legal
     * 
     * <p>The person authenticated the content and accepted legal responsibility for its content.
     */
    public static final CompositionAttestationMode LEGAL = CompositionAttestationMode.builder().value(Value.LEGAL).build();

    /**
     * Official
     * 
     * <p>The organization authenticated the content as consistent with their policies and procedures.
     */
    public static final CompositionAttestationMode OFFICIAL = CompositionAttestationMode.builder().value(Value.OFFICIAL).build();

    private volatile int hashCode;

    private CompositionAttestationMode(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this CompositionAttestationMode as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating CompositionAttestationMode objects from a passed enum value.
     */
    public static CompositionAttestationMode of(Value value) {
        switch (value) {
        case PERSONAL:
            return PERSONAL;
        case PROFESSIONAL:
            return PROFESSIONAL;
        case LEGAL:
            return LEGAL;
        case OFFICIAL:
            return OFFICIAL;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating CompositionAttestationMode objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static CompositionAttestationMode of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating CompositionAttestationMode objects from a passed string value.
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
     * Inherited factory method for creating CompositionAttestationMode objects from a passed string value.
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
        CompositionAttestationMode other = (CompositionAttestationMode) obj;
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
         *     An enum constant for CompositionAttestationMode
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public CompositionAttestationMode build() {
            CompositionAttestationMode compositionAttestationMode = new CompositionAttestationMode(this);
            if (validating) {
                validate(compositionAttestationMode);
            }
            return compositionAttestationMode;
        }

        protected void validate(CompositionAttestationMode compositionAttestationMode) {
            super.validate(compositionAttestationMode);
        }

        protected Builder from(CompositionAttestationMode compositionAttestationMode) {
            super.from(compositionAttestationMode);
            return this;
        }
    }

    public enum Value {
        /**
         * Personal
         * 
         * <p>The person authenticated the content in their personal capacity.
         */
        PERSONAL("personal"),

        /**
         * Professional
         * 
         * <p>The person authenticated the content in their professional capacity.
         */
        PROFESSIONAL("professional"),

        /**
         * Legal
         * 
         * <p>The person authenticated the content and accepted legal responsibility for its content.
         */
        LEGAL("legal"),

        /**
         * Official
         * 
         * <p>The organization authenticated the content as consistent with their policies and procedures.
         */
        OFFICIAL("official");

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
         * Factory method for creating CompositionAttestationMode.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding CompositionAttestationMode.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "personal":
                return PERSONAL;
            case "professional":
                return PROFESSIONAL;
            case "legal":
                return LEGAL;
            case "official":
                return OFFICIAL;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
