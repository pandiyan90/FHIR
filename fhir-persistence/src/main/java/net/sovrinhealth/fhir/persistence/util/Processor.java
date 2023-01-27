/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;

import net.sovrinhealth.fhir.model.resource.Location;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.model.type.Address;
import net.sovrinhealth.fhir.model.type.Annotation;
import net.sovrinhealth.fhir.model.type.Attachment;
import net.sovrinhealth.fhir.model.type.BackboneElement;
import net.sovrinhealth.fhir.model.type.Base64Binary;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.ContactDetail;
import net.sovrinhealth.fhir.model.type.ContactPoint;
import net.sovrinhealth.fhir.model.type.Contributor;
import net.sovrinhealth.fhir.model.type.DataRequirement;
import net.sovrinhealth.fhir.model.type.Date;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.Expression;
import net.sovrinhealth.fhir.model.type.HumanName;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Identifier;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.Markdown;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Money;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Oid;
import net.sovrinhealth.fhir.model.type.ParameterDefinition;
import net.sovrinhealth.fhir.model.type.Period;
import net.sovrinhealth.fhir.model.type.PositiveInt;
import net.sovrinhealth.fhir.model.type.Quantity;
import net.sovrinhealth.fhir.model.type.Range;
import net.sovrinhealth.fhir.model.type.Ratio;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.RelatedArtifact;
import net.sovrinhealth.fhir.model.type.SampledData;
import net.sovrinhealth.fhir.model.type.Signature;
import net.sovrinhealth.fhir.model.type.Time;
import net.sovrinhealth.fhir.model.type.Timing;
import net.sovrinhealth.fhir.model.type.TriggerDefinition;
import net.sovrinhealth.fhir.model.type.UnsignedInt;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.Url;
import net.sovrinhealth.fhir.model.type.UsageContext;
import net.sovrinhealth.fhir.model.type.Uuid;
import net.sovrinhealth.fhir.path.FHIRPathAbstractNode;
import net.sovrinhealth.fhir.path.FHIRPathBooleanValue;
import net.sovrinhealth.fhir.path.FHIRPathDateTimeValue;
import net.sovrinhealth.fhir.path.FHIRPathDecimalValue;
import net.sovrinhealth.fhir.path.FHIRPathElementNode;
import net.sovrinhealth.fhir.path.FHIRPathIntegerValue;
import net.sovrinhealth.fhir.path.FHIRPathQuantityValue;
import net.sovrinhealth.fhir.path.FHIRPathResourceNode;
import net.sovrinhealth.fhir.path.FHIRPathStringValue;
import net.sovrinhealth.fhir.path.FHIRPathTimeValue;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceProcessorException;

@Deprecated
public interface Processor<T> {
    T process(SearchParameter parameter, Object value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, java.lang.String value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, net.sovrinhealth.fhir.model.type.String value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Address value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Annotation value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Attachment value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, BackboneElement value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Base64Binary value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, java.lang.Boolean value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, net.sovrinhealth.fhir.model.type.Boolean value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Canonical value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Code value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, CodeableConcept value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Coding value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ContactDetail value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ContactPoint value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Contributor value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Date value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, DateTime value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, DataRequirement value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Decimal value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Expression value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, HumanName value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Id value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Identifier value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Instant value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, java.lang.Integer value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, net.sovrinhealth.fhir.model.type.Integer value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Markdown value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Meta value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Money value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Narrative value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Oid value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ParameterDefinition value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Period value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, PositiveInt value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Quantity value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Range value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Ratio value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Reference value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, RelatedArtifact value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, SampledData value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Signature value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Time value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Timing value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, TriggerDefinition value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, UnsignedInt value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Uri value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Url value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, UsageContext value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Uuid value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Location.Position value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathAbstractNode value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathElementNode value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathDateTimeValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathStringValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathTimeValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathResourceNode value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathIntegerValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathDecimalValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathBooleanValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathQuantityValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ZonedDateTime value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, LocalDate value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, YearMonth value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Year value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, BigDecimal value) throws FHIRPersistenceProcessorException;
}
