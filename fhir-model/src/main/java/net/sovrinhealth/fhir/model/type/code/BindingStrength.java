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

@System("http://hl7.org/fhir/binding-strength")
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class BindingStrength extends Code {
    /**
     * Required
     * 
     * <p>To be conformant, the concept in this element SHALL be from the specified value set.
     */
    public static final BindingStrength REQUIRED = BindingStrength.builder().value(Value.REQUIRED).build();

    /**
     * Extensible
     * 
     * <p>To be conformant, the concept in this element SHALL be from the specified value set if any of the codes within the 
     * value set can apply to the concept being communicated. If the value set does not cover the concept (based on human 
     * review), alternate codings (or, data type allowing, text) may be included instead.
     */
    public static final BindingStrength EXTENSIBLE = BindingStrength.builder().value(Value.EXTENSIBLE).build();

    /**
     * Preferred
     * 
     * <p>Instances are encouraged to draw from the specified codes for interoperability purposes but are not required to do 
     * so to be considered conformant.
     */
    public static final BindingStrength PREFERRED = BindingStrength.builder().value(Value.PREFERRED).build();

    /**
     * Example
     * 
     * <p>Instances are not expected or even encouraged to draw from the specified value set. The value set merely provides 
     * examples of the types of concepts intended to be included.
     */
    public static final BindingStrength EXAMPLE = BindingStrength.builder().value(Value.EXAMPLE).build();

    private volatile int hashCode;

    private BindingStrength(Builder builder) {
        super(builder);
    }

    /**
     * Get the value of this BindingStrength as an enum constant.
     */
    public Value getValueAsEnum() {
        return (value != null) ? Value.from(value) : null;
    }

    /**
     * Factory method for creating BindingStrength objects from a passed enum value.
     */
    public static BindingStrength of(Value value) {
        switch (value) {
        case REQUIRED:
            return REQUIRED;
        case EXTENSIBLE:
            return EXTENSIBLE;
        case PREFERRED:
            return PREFERRED;
        case EXAMPLE:
            return EXAMPLE;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating BindingStrength objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static BindingStrength of(java.lang.String value) {
        return of(Value.from(value));
    }

    /**
     * Inherited factory method for creating BindingStrength objects from a passed string value.
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
     * Inherited factory method for creating BindingStrength objects from a passed string value.
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
        BindingStrength other = (BindingStrength) obj;
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
         *     An enum constant for BindingStrength
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(Value value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public BindingStrength build() {
            BindingStrength bindingStrength = new BindingStrength(this);
            if (validating) {
                validate(bindingStrength);
            }
            return bindingStrength;
        }

        protected void validate(BindingStrength bindingStrength) {
            super.validate(bindingStrength);
        }

        protected Builder from(BindingStrength bindingStrength) {
            super.from(bindingStrength);
            return this;
        }
    }

    public enum Value {
        /**
         * Required
         * 
         * <p>To be conformant, the concept in this element SHALL be from the specified value set.
         */
        REQUIRED("required"),

        /**
         * Extensible
         * 
         * <p>To be conformant, the concept in this element SHALL be from the specified value set if any of the codes within the 
         * value set can apply to the concept being communicated. If the value set does not cover the concept (based on human 
         * review), alternate codings (or, data type allowing, text) may be included instead.
         */
        EXTENSIBLE("extensible"),

        /**
         * Preferred
         * 
         * <p>Instances are encouraged to draw from the specified codes for interoperability purposes but are not required to do 
         * so to be considered conformant.
         */
        PREFERRED("preferred"),

        /**
         * Example
         * 
         * <p>Instances are not expected or even encouraged to draw from the specified value set. The value set merely provides 
         * examples of the types of concepts intended to be included.
         */
        EXAMPLE("example");

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
         * Factory method for creating BindingStrength.Value values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @return
         *     The corresponding BindingStrength.Value or null if a null value was passed
         * @throws IllegalArgumentException
         *     If the passed string is not null and cannot be parsed into an allowed code value
         */
        public static Value from(java.lang.String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
            case "required":
                return REQUIRED;
            case "extensible":
                return EXTENSIBLE;
            case "preferred":
                return PREFERRED;
            case "example":
                return EXAMPLE;
            default:
                throw new IllegalArgumentException(value);
            }
        }
    }
}
