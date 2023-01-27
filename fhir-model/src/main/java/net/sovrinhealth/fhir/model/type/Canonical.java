/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.type;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Generated;

import net.sovrinhealth.fhir.model.visitor.Visitor;

/**
 * A URI that is a reference to a canonical URL on a FHIR resource
 */
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class Canonical extends Uri {
    private Canonical(Builder builder) {
        super(builder);
    }

    @Override
    public boolean hasValue() {
        return (value != null);
    }

    /**
     * Factory method for creating Canonical objects from a java.lang.String
     * 
     * @param value
     *     A java.lang.String, not null
     */
    public static Canonical of(java.lang.String value) {
        Objects.requireNonNull(value, "value");
        return Canonical.builder().value(value).build();
    }

    /**
     * Factory method for creating Canonical objects from a java.lang.String uri and version
     * 
     * @param value
     *     A java.lang.String for the uri portion of the canonical reference, not null
     * @param value
     *     A java.lang.String for the version portion of the canonical reference
     */
    public static Canonical of(java.lang.String uri, java.lang.String version) {
        Objects.requireNonNull(uri, "uri");
        StringBuilder value = new StringBuilder(uri);
        if (version != null && !version.isEmpty()) {
            value.append('|');
            value.append(version);
        }
        return Canonical.builder().value(value.toString()).build();
    }

    /**
     * Factory method for creating Canonical objects from a java.lang.String uri, version, and fragment
     * 
     * @param value
     *     A java.lang.String for the uri portion of the canonical reference, not null
     * @param value
     *     A java.lang.String for the version portion of the canonical reference
     * @param value
     *     A java.lang.String for the fragment portion of the canonical reference
     */
    public static Canonical of(java.lang.String uri, java.lang.String version, java.lang.String fragment) {
        Objects.requireNonNull(uri, "uri");
        StringBuilder value = new StringBuilder(uri);
        if (version != null && !version.isEmpty()) {
            value.append('|');
            value.append(version);
        }
        if (fragment != null && !fragment.isEmpty()) {
            value.append('#');
            value.append(fragment);
        }
        return Canonical.builder().value(value.toString()).build();
    }

    /**
     * Factory method for creating Canonical objects from a java.lang.String
     * 
     * @param value
     *     A java.lang.String that can be parsed into a valid FHIR uri value, not null
     */
    public static Uri uri(java.lang.String value) {
        Objects.requireNonNull(value, "value");
        return Canonical.builder().value(value).build();
    }

    @Override
    public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
        if (visitor.preVisit(this)) {
            visitor.visitStart(elementName, elementIndex, this);
            if (visitor.visit(elementName, elementIndex, this)) {
                // visit children
                accept(id, "id", visitor);
                accept(extension, "extension", visitor, Extension.class);
                accept(value, "value", visitor);
            }
            visitor.visitEnd(elementName, elementIndex, this);
            visitor.postVisit(this);
        }
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
        Canonical other = (Canonical) obj;
        return Objects.equals(id, other.id) && 
            Objects.equals(extension, other.extension) && 
            Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, 
                extension, 
                value);
            hashCode = result;
        }
        return result;
    }

    @Override
    public Builder toBuilder() {
        return new Builder().from(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Uri.Builder {
        private Builder() {
            super();
        }

        /**
         * unique id for the element within a resource (for internal references)
         * 
         * @param id
         *     xml:id (or equivalent in JSON)
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder extension(Extension... extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        @Override
        public Builder extension(Collection<Extension> extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * Primitive value for canonical
         * 
         * @param value
         *     Primitive value for canonical
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder value(java.lang.String value) {
            return (Builder) super.value(value);
        }

        /**
         * Build the {@link Canonical}
         * 
         * @return
         *     An immutable object of type {@link Canonical}
         * @throws IllegalStateException
         *     if the current state cannot be built into a valid Canonical per the base specification
         */
        @Override
        public Canonical build() {
            Canonical canonical = new Canonical(this);
            if (validating) {
                validate(canonical);
            }
            return canonical;
        }

        protected void validate(Canonical canonical) {
            super.validate(canonical);
        }

        protected Builder from(Canonical canonical) {
            super.from(canonical);
            return this;
        }
    }
}
