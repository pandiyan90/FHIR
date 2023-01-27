/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sovrinhealth.fhir.model.annotation.Binding;
import net.sovrinhealth.fhir.model.annotation.Choice;
import net.sovrinhealth.fhir.model.annotation.Constraint;
import net.sovrinhealth.fhir.model.annotation.ReferenceTarget;
import net.sovrinhealth.fhir.model.annotation.Required;
import net.sovrinhealth.fhir.model.annotation.Summary;
import net.sovrinhealth.fhir.model.constraint.spi.ConstraintProvider;
import net.sovrinhealth.fhir.model.constraint.spi.ModelConstraintProvider;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Address;
import net.sovrinhealth.fhir.model.type.Age;
import net.sovrinhealth.fhir.model.type.Annotation;
import net.sovrinhealth.fhir.model.type.Attachment;
import net.sovrinhealth.fhir.model.type.BackboneElement;
import net.sovrinhealth.fhir.model.type.Base64Binary;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.CodeableReference;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.ContactDetail;
import net.sovrinhealth.fhir.model.type.ContactPoint;
import net.sovrinhealth.fhir.model.type.Contributor;
import net.sovrinhealth.fhir.model.type.Count;
import net.sovrinhealth.fhir.model.type.DataRequirement;
import net.sovrinhealth.fhir.model.type.Date;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.Distance;
import net.sovrinhealth.fhir.model.type.Dosage;
import net.sovrinhealth.fhir.model.type.Duration;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.model.type.ElementDefinition;
import net.sovrinhealth.fhir.model.type.Expression;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.HumanName;
import net.sovrinhealth.fhir.model.type.Id;
import net.sovrinhealth.fhir.model.type.Identifier;
import net.sovrinhealth.fhir.model.type.Instant;
import net.sovrinhealth.fhir.model.type.Markdown;
import net.sovrinhealth.fhir.model.type.MarketingStatus;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Money;
import net.sovrinhealth.fhir.model.type.MoneyQuantity;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Oid;
import net.sovrinhealth.fhir.model.type.ParameterDefinition;
import net.sovrinhealth.fhir.model.type.Period;
import net.sovrinhealth.fhir.model.type.Population;
import net.sovrinhealth.fhir.model.type.PositiveInt;
import net.sovrinhealth.fhir.model.type.ProdCharacteristic;
import net.sovrinhealth.fhir.model.type.ProductShelfLife;
import net.sovrinhealth.fhir.model.type.Quantity;
import net.sovrinhealth.fhir.model.type.Range;
import net.sovrinhealth.fhir.model.type.Ratio;
import net.sovrinhealth.fhir.model.type.RatioRange;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.RelatedArtifact;
import net.sovrinhealth.fhir.model.type.SampledData;
import net.sovrinhealth.fhir.model.type.Signature;
import net.sovrinhealth.fhir.model.type.SimpleQuantity;
import net.sovrinhealth.fhir.model.type.Time;
import net.sovrinhealth.fhir.model.type.Timing;
import net.sovrinhealth.fhir.model.type.TriggerDefinition;
import net.sovrinhealth.fhir.model.type.UnsignedInt;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.Url;
import net.sovrinhealth.fhir.model.type.UsageContext;
import net.sovrinhealth.fhir.model.type.Uuid;
import net.sovrinhealth.fhir.model.type.Xhtml;

public final class ModelSupport {
    private static final Logger log = Logger.getLogger(ModelSupport.class.getName());

    public static final Class<net.sovrinhealth.fhir.model.type.Boolean> FHIR_BOOLEAN = net.sovrinhealth.fhir.model.type.Boolean.class;
    public static final Class<net.sovrinhealth.fhir.model.type.Integer> FHIR_INTEGER = net.sovrinhealth.fhir.model.type.Integer.class;
    public static final Class<net.sovrinhealth.fhir.model.type.String> FHIR_STRING = net.sovrinhealth.fhir.model.type.String.class;
    public static final Class<net.sovrinhealth.fhir.model.type.Date> FHIR_DATE = net.sovrinhealth.fhir.model.type.Date.class;
    public static final Class<net.sovrinhealth.fhir.model.type.Instant> FHIR_INSTANT = net.sovrinhealth.fhir.model.type.Instant.class;

    private static final Map<Class<?>, Class<?>> CONCRETE_TYPE_MAP = buildConcreteTypeMap();
    private static final List<Class<?>> MODEL_CLASSES = Arrays.asList(
        net.sovrinhealth.fhir.model.resource.Account.class,
        net.sovrinhealth.fhir.model.resource.Account.Coverage.class,
        net.sovrinhealth.fhir.model.resource.Account.Guarantor.class,
        net.sovrinhealth.fhir.model.resource.ActivityDefinition.class,
        net.sovrinhealth.fhir.model.resource.ActivityDefinition.DynamicValue.class,
        net.sovrinhealth.fhir.model.resource.ActivityDefinition.Participant.class,
        net.sovrinhealth.fhir.model.resource.AdministrableProductDefinition.class,
        net.sovrinhealth.fhir.model.resource.AdministrableProductDefinition.Property.class,
        net.sovrinhealth.fhir.model.resource.AdministrableProductDefinition.RouteOfAdministration.class,
        net.sovrinhealth.fhir.model.resource.AdministrableProductDefinition.RouteOfAdministration.TargetSpecies.class,
        net.sovrinhealth.fhir.model.resource.AdministrableProductDefinition.RouteOfAdministration.TargetSpecies.WithdrawalPeriod.class,
        net.sovrinhealth.fhir.model.resource.AdverseEvent.class,
        net.sovrinhealth.fhir.model.resource.AdverseEvent.SuspectEntity.class,
        net.sovrinhealth.fhir.model.resource.AdverseEvent.SuspectEntity.Causality.class,
        net.sovrinhealth.fhir.model.resource.AllergyIntolerance.class,
        net.sovrinhealth.fhir.model.resource.AllergyIntolerance.Reaction.class,
        net.sovrinhealth.fhir.model.resource.Appointment.class,
        net.sovrinhealth.fhir.model.resource.Appointment.Participant.class,
        net.sovrinhealth.fhir.model.resource.AppointmentResponse.class,
        net.sovrinhealth.fhir.model.resource.AuditEvent.class,
        net.sovrinhealth.fhir.model.resource.AuditEvent.Agent.class,
        net.sovrinhealth.fhir.model.resource.AuditEvent.Agent.Network.class,
        net.sovrinhealth.fhir.model.resource.AuditEvent.Entity.class,
        net.sovrinhealth.fhir.model.resource.AuditEvent.Entity.Detail.class,
        net.sovrinhealth.fhir.model.resource.AuditEvent.Source.class,
        net.sovrinhealth.fhir.model.resource.Basic.class,
        net.sovrinhealth.fhir.model.resource.Binary.class,
        net.sovrinhealth.fhir.model.resource.BiologicallyDerivedProduct.class,
        net.sovrinhealth.fhir.model.resource.BiologicallyDerivedProduct.Collection.class,
        net.sovrinhealth.fhir.model.resource.BiologicallyDerivedProduct.Manipulation.class,
        net.sovrinhealth.fhir.model.resource.BiologicallyDerivedProduct.Processing.class,
        net.sovrinhealth.fhir.model.resource.BiologicallyDerivedProduct.Storage.class,
        net.sovrinhealth.fhir.model.resource.BodyStructure.class,
        net.sovrinhealth.fhir.model.resource.Bundle.class,
        net.sovrinhealth.fhir.model.resource.Bundle.Entry.class,
        net.sovrinhealth.fhir.model.resource.Bundle.Entry.Request.class,
        net.sovrinhealth.fhir.model.resource.Bundle.Entry.Response.class,
        net.sovrinhealth.fhir.model.resource.Bundle.Entry.Search.class,
        net.sovrinhealth.fhir.model.resource.Bundle.Link.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Document.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Implementation.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Messaging.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Messaging.Endpoint.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Messaging.SupportedMessage.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.Interaction.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.Resource.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.Resource.Interaction.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.Resource.Operation.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.Resource.SearchParam.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Rest.Security.class,
        net.sovrinhealth.fhir.model.resource.CapabilityStatement.Software.class,
        net.sovrinhealth.fhir.model.resource.CarePlan.class,
        net.sovrinhealth.fhir.model.resource.CarePlan.Activity.class,
        net.sovrinhealth.fhir.model.resource.CarePlan.Activity.Detail.class,
        net.sovrinhealth.fhir.model.resource.CareTeam.class,
        net.sovrinhealth.fhir.model.resource.CareTeam.Participant.class,
        net.sovrinhealth.fhir.model.resource.CatalogEntry.class,
        net.sovrinhealth.fhir.model.resource.CatalogEntry.RelatedEntry.class,
        net.sovrinhealth.fhir.model.resource.ChargeItem.class,
        net.sovrinhealth.fhir.model.resource.ChargeItem.Performer.class,
        net.sovrinhealth.fhir.model.resource.ChargeItemDefinition.class,
        net.sovrinhealth.fhir.model.resource.ChargeItemDefinition.Applicability.class,
        net.sovrinhealth.fhir.model.resource.ChargeItemDefinition.PropertyGroup.class,
        net.sovrinhealth.fhir.model.resource.ChargeItemDefinition.PropertyGroup.PriceComponent.class,
        net.sovrinhealth.fhir.model.resource.Citation.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Abstract.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Classification.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Classification.WhoClassified.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Contributorship.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Contributorship.Entry.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Contributorship.Entry.AffiliationInfo.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Contributorship.Entry.ContributionInstance.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Contributorship.Summary.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Part.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.PublicationForm.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.PublicationForm.PeriodicRelease.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.PublicationForm.PeriodicRelease.DateOfPublication.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.PublicationForm.PublishedIn.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.RelatesTo.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.StatusDate.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Title.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.Version.class,
        net.sovrinhealth.fhir.model.resource.Citation.CitedArtifact.WebLocation.class,
        net.sovrinhealth.fhir.model.resource.Citation.Classification.class,
        net.sovrinhealth.fhir.model.resource.Citation.RelatesTo.class,
        net.sovrinhealth.fhir.model.resource.Citation.Summary.class,
        net.sovrinhealth.fhir.model.resource.Citation.StatusDate.class,
        net.sovrinhealth.fhir.model.resource.Claim.class,
        net.sovrinhealth.fhir.model.resource.Claim.Accident.class,
        net.sovrinhealth.fhir.model.resource.Claim.CareTeam.class,
        net.sovrinhealth.fhir.model.resource.Claim.Diagnosis.class,
        net.sovrinhealth.fhir.model.resource.Claim.Insurance.class,
        net.sovrinhealth.fhir.model.resource.Claim.Item.class,
        net.sovrinhealth.fhir.model.resource.Claim.Item.Detail.class,
        net.sovrinhealth.fhir.model.resource.Claim.Item.Detail.SubDetail.class,
        net.sovrinhealth.fhir.model.resource.Claim.Payee.class,
        net.sovrinhealth.fhir.model.resource.Claim.Procedure.class,
        net.sovrinhealth.fhir.model.resource.Claim.Related.class,
        net.sovrinhealth.fhir.model.resource.Claim.SupportingInfo.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.AddItem.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.AddItem.Detail.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.AddItem.Detail.SubDetail.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Error.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Insurance.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Item.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Item.Adjudication.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Item.Detail.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Item.Detail.SubDetail.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Payment.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.ProcessNote.class,
        net.sovrinhealth.fhir.model.resource.ClaimResponse.Total.class,
        net.sovrinhealth.fhir.model.resource.ClinicalImpression.class,
        net.sovrinhealth.fhir.model.resource.ClinicalImpression.Finding.class,
        net.sovrinhealth.fhir.model.resource.ClinicalImpression.Investigation.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.Contraindication.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.Contraindication.OtherTherapy.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.Indication.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.Interaction.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.Interaction.Interactant.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.UndesirableEffect.class,
        net.sovrinhealth.fhir.model.resource.ClinicalUseDefinition.Warning.class,
        net.sovrinhealth.fhir.model.resource.CodeSystem.class,
        net.sovrinhealth.fhir.model.resource.CodeSystem.Concept.class,
        net.sovrinhealth.fhir.model.resource.CodeSystem.Concept.Designation.class,
        net.sovrinhealth.fhir.model.resource.CodeSystem.Concept.Property.class,
        net.sovrinhealth.fhir.model.resource.CodeSystem.Filter.class,
        net.sovrinhealth.fhir.model.resource.CodeSystem.Property.class,
        net.sovrinhealth.fhir.model.resource.Communication.class,
        net.sovrinhealth.fhir.model.resource.Communication.Payload.class,
        net.sovrinhealth.fhir.model.resource.CommunicationRequest.class,
        net.sovrinhealth.fhir.model.resource.CommunicationRequest.Payload.class,
        net.sovrinhealth.fhir.model.resource.CompartmentDefinition.class,
        net.sovrinhealth.fhir.model.resource.CompartmentDefinition.Resource.class,
        net.sovrinhealth.fhir.model.resource.Composition.class,
        net.sovrinhealth.fhir.model.resource.Composition.Attester.class,
        net.sovrinhealth.fhir.model.resource.Composition.Event.class,
        net.sovrinhealth.fhir.model.resource.Composition.RelatesTo.class,
        net.sovrinhealth.fhir.model.resource.Composition.Section.class,
        net.sovrinhealth.fhir.model.resource.ConceptMap.class,
        net.sovrinhealth.fhir.model.resource.ConceptMap.Group.class,
        net.sovrinhealth.fhir.model.resource.ConceptMap.Group.Element.class,
        net.sovrinhealth.fhir.model.resource.ConceptMap.Group.Element.Target.class,
        net.sovrinhealth.fhir.model.resource.ConceptMap.Group.Element.Target.DependsOn.class,
        net.sovrinhealth.fhir.model.resource.ConceptMap.Group.Unmapped.class,
        net.sovrinhealth.fhir.model.resource.Condition.class,
        net.sovrinhealth.fhir.model.resource.Condition.Evidence.class,
        net.sovrinhealth.fhir.model.resource.Condition.Stage.class,
        net.sovrinhealth.fhir.model.resource.Consent.class,
        net.sovrinhealth.fhir.model.resource.Consent.Policy.class,
        net.sovrinhealth.fhir.model.resource.Consent.Provision.class,
        net.sovrinhealth.fhir.model.resource.Consent.Provision.Actor.class,
        net.sovrinhealth.fhir.model.resource.Consent.Provision.Data.class,
        net.sovrinhealth.fhir.model.resource.Consent.Verification.class,
        net.sovrinhealth.fhir.model.resource.Contract.class,
        net.sovrinhealth.fhir.model.resource.Contract.ContentDefinition.class,
        net.sovrinhealth.fhir.model.resource.Contract.Friendly.class,
        net.sovrinhealth.fhir.model.resource.Contract.Legal.class,
        net.sovrinhealth.fhir.model.resource.Contract.Rule.class,
        net.sovrinhealth.fhir.model.resource.Contract.Signer.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Action.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Action.Subject.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Asset.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Asset.Context.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Asset.ValuedItem.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Offer.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Offer.Answer.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.Offer.Party.class,
        net.sovrinhealth.fhir.model.resource.Contract.Term.SecurityLabel.class,
        net.sovrinhealth.fhir.model.resource.Coverage.class,
        net.sovrinhealth.fhir.model.resource.Coverage.Class.class,
        net.sovrinhealth.fhir.model.resource.Coverage.CostToBeneficiary.class,
        net.sovrinhealth.fhir.model.resource.Coverage.CostToBeneficiary.Exception.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityRequest.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityRequest.Insurance.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityRequest.Item.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityRequest.Item.Diagnosis.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityRequest.SupportingInfo.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityResponse.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityResponse.Error.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityResponse.Insurance.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityResponse.Insurance.Item.class,
        net.sovrinhealth.fhir.model.resource.CoverageEligibilityResponse.Insurance.Item.Benefit.class,
        net.sovrinhealth.fhir.model.resource.DetectedIssue.class,
        net.sovrinhealth.fhir.model.resource.DetectedIssue.Evidence.class,
        net.sovrinhealth.fhir.model.resource.DetectedIssue.Mitigation.class,
        net.sovrinhealth.fhir.model.resource.Device.class,
        net.sovrinhealth.fhir.model.resource.Device.DeviceName.class,
        net.sovrinhealth.fhir.model.resource.Device.Property.class,
        net.sovrinhealth.fhir.model.resource.Device.Specialization.class,
        net.sovrinhealth.fhir.model.resource.Device.UdiCarrier.class,
        net.sovrinhealth.fhir.model.resource.Device.Version.class,
        net.sovrinhealth.fhir.model.resource.DeviceDefinition.class,
        net.sovrinhealth.fhir.model.resource.DeviceDefinition.Capability.class,
        net.sovrinhealth.fhir.model.resource.DeviceDefinition.DeviceName.class,
        net.sovrinhealth.fhir.model.resource.DeviceDefinition.Material.class,
        net.sovrinhealth.fhir.model.resource.DeviceDefinition.Property.class,
        net.sovrinhealth.fhir.model.resource.DeviceDefinition.Specialization.class,
        net.sovrinhealth.fhir.model.resource.DeviceDefinition.UdiDeviceIdentifier.class,
        net.sovrinhealth.fhir.model.resource.DeviceMetric.class,
        net.sovrinhealth.fhir.model.resource.DeviceMetric.Calibration.class,
        net.sovrinhealth.fhir.model.resource.DeviceRequest.class,
        net.sovrinhealth.fhir.model.resource.DeviceRequest.Parameter.class,
        net.sovrinhealth.fhir.model.resource.DeviceUseStatement.class,
        net.sovrinhealth.fhir.model.resource.DiagnosticReport.class,
        net.sovrinhealth.fhir.model.resource.DiagnosticReport.Media.class,
        net.sovrinhealth.fhir.model.resource.DocumentManifest.class,
        net.sovrinhealth.fhir.model.resource.DocumentManifest.Related.class,
        net.sovrinhealth.fhir.model.resource.DocumentReference.class,
        net.sovrinhealth.fhir.model.resource.DocumentReference.Content.class,
        net.sovrinhealth.fhir.model.resource.DocumentReference.Context.class,
        net.sovrinhealth.fhir.model.resource.DocumentReference.RelatesTo.class,
        net.sovrinhealth.fhir.model.resource.DomainResource.class,
        net.sovrinhealth.fhir.model.resource.Encounter.class,
        net.sovrinhealth.fhir.model.resource.Encounter.ClassHistory.class,
        net.sovrinhealth.fhir.model.resource.Encounter.Diagnosis.class,
        net.sovrinhealth.fhir.model.resource.Encounter.Hospitalization.class,
        net.sovrinhealth.fhir.model.resource.Encounter.Location.class,
        net.sovrinhealth.fhir.model.resource.Encounter.Participant.class,
        net.sovrinhealth.fhir.model.resource.Encounter.StatusHistory.class,
        net.sovrinhealth.fhir.model.resource.Endpoint.class,
        net.sovrinhealth.fhir.model.resource.EnrollmentRequest.class,
        net.sovrinhealth.fhir.model.resource.EnrollmentResponse.class,
        net.sovrinhealth.fhir.model.resource.EpisodeOfCare.class,
        net.sovrinhealth.fhir.model.resource.EpisodeOfCare.Diagnosis.class,
        net.sovrinhealth.fhir.model.resource.EpisodeOfCare.StatusHistory.class,
        net.sovrinhealth.fhir.model.resource.EventDefinition.class,
        net.sovrinhealth.fhir.model.resource.Evidence.class,
        net.sovrinhealth.fhir.model.resource.Evidence.Certainty.class,
        net.sovrinhealth.fhir.model.resource.Evidence.Statistic.class,
        net.sovrinhealth.fhir.model.resource.Evidence.Statistic.AttributeEstimate.class,
        net.sovrinhealth.fhir.model.resource.Evidence.Statistic.ModelCharacteristic.class,
        net.sovrinhealth.fhir.model.resource.Evidence.Statistic.ModelCharacteristic.Variable.class,
        net.sovrinhealth.fhir.model.resource.Evidence.Statistic.SampleSize.class,
        net.sovrinhealth.fhir.model.resource.Evidence.VariableDefinition.class,
        net.sovrinhealth.fhir.model.resource.EvidenceReport.class,
        net.sovrinhealth.fhir.model.resource.EvidenceReport.RelatesTo.class,
        net.sovrinhealth.fhir.model.resource.EvidenceReport.Section.class,
        net.sovrinhealth.fhir.model.resource.EvidenceReport.Subject.class,
        net.sovrinhealth.fhir.model.resource.EvidenceReport.Subject.Characteristic.class,
        net.sovrinhealth.fhir.model.resource.EvidenceVariable.class,
        net.sovrinhealth.fhir.model.resource.EvidenceVariable.Category.class,
        net.sovrinhealth.fhir.model.resource.EvidenceVariable.Characteristic.class,
        net.sovrinhealth.fhir.model.resource.EvidenceVariable.Characteristic.TimeFromStart.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Actor.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Instance.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Instance.ContainedInstance.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Instance.Version.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Process.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Process.Step.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Process.Step.Alternative.class,
        net.sovrinhealth.fhir.model.resource.ExampleScenario.Process.Step.Operation.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Accident.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.AddItem.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.AddItem.Detail.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.AddItem.Detail.SubDetail.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.BenefitBalance.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.BenefitBalance.Financial.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.CareTeam.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Diagnosis.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Insurance.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Item.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Item.Adjudication.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Item.Detail.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Item.Detail.SubDetail.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Payee.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Payment.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Procedure.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.ProcessNote.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Related.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.SupportingInfo.class,
        net.sovrinhealth.fhir.model.resource.ExplanationOfBenefit.Total.class,
        net.sovrinhealth.fhir.model.resource.FamilyMemberHistory.class,
        net.sovrinhealth.fhir.model.resource.FamilyMemberHistory.Condition.class,
        net.sovrinhealth.fhir.model.resource.Flag.class,
        net.sovrinhealth.fhir.model.resource.Goal.class,
        net.sovrinhealth.fhir.model.resource.Goal.Target.class,
        net.sovrinhealth.fhir.model.resource.GraphDefinition.class,
        net.sovrinhealth.fhir.model.resource.GraphDefinition.Link.class,
        net.sovrinhealth.fhir.model.resource.GraphDefinition.Link.Target.class,
        net.sovrinhealth.fhir.model.resource.GraphDefinition.Link.Target.Compartment.class,
        net.sovrinhealth.fhir.model.resource.Group.class,
        net.sovrinhealth.fhir.model.resource.Group.Characteristic.class,
        net.sovrinhealth.fhir.model.resource.Group.Member.class,
        net.sovrinhealth.fhir.model.resource.GuidanceResponse.class,
        net.sovrinhealth.fhir.model.resource.HealthcareService.class,
        net.sovrinhealth.fhir.model.resource.HealthcareService.AvailableTime.class,
        net.sovrinhealth.fhir.model.resource.HealthcareService.Eligibility.class,
        net.sovrinhealth.fhir.model.resource.HealthcareService.NotAvailable.class,
        net.sovrinhealth.fhir.model.resource.ImagingStudy.class,
        net.sovrinhealth.fhir.model.resource.ImagingStudy.Series.class,
        net.sovrinhealth.fhir.model.resource.ImagingStudy.Series.Instance.class,
        net.sovrinhealth.fhir.model.resource.ImagingStudy.Series.Performer.class,
        net.sovrinhealth.fhir.model.resource.Immunization.class,
        net.sovrinhealth.fhir.model.resource.Immunization.Education.class,
        net.sovrinhealth.fhir.model.resource.Immunization.Performer.class,
        net.sovrinhealth.fhir.model.resource.Immunization.ProtocolApplied.class,
        net.sovrinhealth.fhir.model.resource.Immunization.Reaction.class,
        net.sovrinhealth.fhir.model.resource.ImmunizationEvaluation.class,
        net.sovrinhealth.fhir.model.resource.ImmunizationRecommendation.class,
        net.sovrinhealth.fhir.model.resource.ImmunizationRecommendation.Recommendation.class,
        net.sovrinhealth.fhir.model.resource.ImmunizationRecommendation.Recommendation.DateCriterion.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Definition.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Definition.Grouping.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Definition.Page.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Definition.Parameter.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Definition.Resource.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Definition.Template.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.DependsOn.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Global.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Manifest.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Manifest.Page.class,
        net.sovrinhealth.fhir.model.resource.ImplementationGuide.Manifest.Resource.class,
        net.sovrinhealth.fhir.model.resource.Ingredient.class,
        net.sovrinhealth.fhir.model.resource.Ingredient.Manufacturer.class,
        net.sovrinhealth.fhir.model.resource.Ingredient.Substance.class,
        net.sovrinhealth.fhir.model.resource.Ingredient.Substance.Strength.class,
        net.sovrinhealth.fhir.model.resource.Ingredient.Substance.Strength.ReferenceStrength.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Contact.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Coverage.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Coverage.Benefit.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Coverage.Benefit.Limit.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Plan.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Plan.GeneralCost.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Plan.SpecificCost.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Plan.SpecificCost.Benefit.class,
        net.sovrinhealth.fhir.model.resource.InsurancePlan.Plan.SpecificCost.Benefit.Cost.class,
        net.sovrinhealth.fhir.model.resource.Invoice.class,
        net.sovrinhealth.fhir.model.resource.Invoice.LineItem.class,
        net.sovrinhealth.fhir.model.resource.Invoice.LineItem.PriceComponent.class,
        net.sovrinhealth.fhir.model.resource.Invoice.Participant.class,
        net.sovrinhealth.fhir.model.resource.Library.class,
        net.sovrinhealth.fhir.model.resource.Linkage.class,
        net.sovrinhealth.fhir.model.resource.Linkage.Item.class,
        net.sovrinhealth.fhir.model.resource.List.class,
        net.sovrinhealth.fhir.model.resource.List.Entry.class,
        net.sovrinhealth.fhir.model.resource.Location.class,
        net.sovrinhealth.fhir.model.resource.Location.HoursOfOperation.class,
        net.sovrinhealth.fhir.model.resource.Location.Position.class,
        net.sovrinhealth.fhir.model.resource.ManufacturedItemDefinition.class,
        net.sovrinhealth.fhir.model.resource.ManufacturedItemDefinition.Property.class,
        net.sovrinhealth.fhir.model.resource.Measure.class,
        net.sovrinhealth.fhir.model.resource.Measure.Group.class,
        net.sovrinhealth.fhir.model.resource.Measure.Group.Population.class,
        net.sovrinhealth.fhir.model.resource.Measure.Group.Stratifier.class,
        net.sovrinhealth.fhir.model.resource.Measure.Group.Stratifier.Component.class,
        net.sovrinhealth.fhir.model.resource.Measure.SupplementalData.class,
        net.sovrinhealth.fhir.model.resource.MeasureReport.class,
        net.sovrinhealth.fhir.model.resource.MeasureReport.Group.class,
        net.sovrinhealth.fhir.model.resource.MeasureReport.Group.Population.class,
        net.sovrinhealth.fhir.model.resource.MeasureReport.Group.Stratifier.class,
        net.sovrinhealth.fhir.model.resource.MeasureReport.Group.Stratifier.Stratum.class,
        net.sovrinhealth.fhir.model.resource.MeasureReport.Group.Stratifier.Stratum.Component.class,
        net.sovrinhealth.fhir.model.resource.MeasureReport.Group.Stratifier.Stratum.Population.class,
        net.sovrinhealth.fhir.model.resource.Media.class,
        net.sovrinhealth.fhir.model.resource.Medication.class,
        net.sovrinhealth.fhir.model.resource.Medication.Batch.class,
        net.sovrinhealth.fhir.model.resource.Medication.Ingredient.class,
        net.sovrinhealth.fhir.model.resource.MedicationAdministration.class,
        net.sovrinhealth.fhir.model.resource.MedicationAdministration.Dosage.class,
        net.sovrinhealth.fhir.model.resource.MedicationAdministration.Performer.class,
        net.sovrinhealth.fhir.model.resource.MedicationDispense.class,
        net.sovrinhealth.fhir.model.resource.MedicationDispense.Performer.class,
        net.sovrinhealth.fhir.model.resource.MedicationDispense.Substitution.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.AdministrationGuidelines.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.AdministrationGuidelines.Dosage.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.AdministrationGuidelines.PatientCharacteristics.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Cost.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.DrugCharacteristic.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Ingredient.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Kinetics.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.MedicineClassification.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.MonitoringProgram.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Monograph.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Packaging.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Regulatory.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Regulatory.MaxDispense.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Regulatory.Schedule.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.Regulatory.Substitution.class,
        net.sovrinhealth.fhir.model.resource.MedicationKnowledge.RelatedMedicationKnowledge.class,
        net.sovrinhealth.fhir.model.resource.MedicationRequest.class,
        net.sovrinhealth.fhir.model.resource.MedicationRequest.DispenseRequest.class,
        net.sovrinhealth.fhir.model.resource.MedicationRequest.DispenseRequest.InitialFill.class,
        net.sovrinhealth.fhir.model.resource.MedicationRequest.Substitution.class,
        net.sovrinhealth.fhir.model.resource.MedicationStatement.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.Characteristic.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.Contact.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.CrossReference.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.Name.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.Name.CountryLanguage.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.Name.NamePart.class,
        net.sovrinhealth.fhir.model.resource.MedicinalProductDefinition.Operation.class,
        net.sovrinhealth.fhir.model.resource.MessageDefinition.class,
        net.sovrinhealth.fhir.model.resource.MessageDefinition.AllowedResponse.class,
        net.sovrinhealth.fhir.model.resource.MessageDefinition.Focus.class,
        net.sovrinhealth.fhir.model.resource.MessageHeader.class,
        net.sovrinhealth.fhir.model.resource.MessageHeader.Destination.class,
        net.sovrinhealth.fhir.model.resource.MessageHeader.Response.class,
        net.sovrinhealth.fhir.model.resource.MessageHeader.Source.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.Quality.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.Quality.Roc.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.ReferenceSeq.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.Repository.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.StructureVariant.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.StructureVariant.Inner.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.StructureVariant.Outer.class,
        net.sovrinhealth.fhir.model.resource.MolecularSequence.Variant.class,
        net.sovrinhealth.fhir.model.resource.NamingSystem.class,
        net.sovrinhealth.fhir.model.resource.NamingSystem.UniqueId.class,
        net.sovrinhealth.fhir.model.resource.NutritionOrder.class,
        net.sovrinhealth.fhir.model.resource.NutritionOrder.EnteralFormula.class,
        net.sovrinhealth.fhir.model.resource.NutritionOrder.EnteralFormula.Administration.class,
        net.sovrinhealth.fhir.model.resource.NutritionOrder.OralDiet.class,
        net.sovrinhealth.fhir.model.resource.NutritionOrder.OralDiet.Nutrient.class,
        net.sovrinhealth.fhir.model.resource.NutritionOrder.OralDiet.Texture.class,
        net.sovrinhealth.fhir.model.resource.NutritionOrder.Supplement.class,
        net.sovrinhealth.fhir.model.resource.NutritionProduct.class,
        net.sovrinhealth.fhir.model.resource.NutritionProduct.Ingredient.class,
        net.sovrinhealth.fhir.model.resource.NutritionProduct.Instance.class,
        net.sovrinhealth.fhir.model.resource.NutritionProduct.Nutrient.class,
        net.sovrinhealth.fhir.model.resource.NutritionProduct.ProductCharacteristic.class,
        net.sovrinhealth.fhir.model.resource.Observation.class,
        net.sovrinhealth.fhir.model.resource.Observation.Component.class,
        net.sovrinhealth.fhir.model.resource.Observation.ReferenceRange.class,
        net.sovrinhealth.fhir.model.resource.ObservationDefinition.class,
        net.sovrinhealth.fhir.model.resource.ObservationDefinition.QualifiedInterval.class,
        net.sovrinhealth.fhir.model.resource.ObservationDefinition.QuantitativeDetails.class,
        net.sovrinhealth.fhir.model.resource.OperationDefinition.class,
        net.sovrinhealth.fhir.model.resource.OperationDefinition.Overload.class,
        net.sovrinhealth.fhir.model.resource.OperationDefinition.Parameter.class,
        net.sovrinhealth.fhir.model.resource.OperationDefinition.Parameter.Binding.class,
        net.sovrinhealth.fhir.model.resource.OperationDefinition.Parameter.ReferencedFrom.class,
        net.sovrinhealth.fhir.model.resource.OperationOutcome.class,
        net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue.class,
        net.sovrinhealth.fhir.model.resource.Organization.class,
        net.sovrinhealth.fhir.model.resource.Organization.Contact.class,
        net.sovrinhealth.fhir.model.resource.OrganizationAffiliation.class,
        net.sovrinhealth.fhir.model.resource.PackagedProductDefinition.class,
        net.sovrinhealth.fhir.model.resource.PackagedProductDefinition.LegalStatusOfSupply.class,
        net.sovrinhealth.fhir.model.resource.PackagedProductDefinition.Package.class,
        net.sovrinhealth.fhir.model.resource.PackagedProductDefinition.Package.ContainedItem.class,
        net.sovrinhealth.fhir.model.resource.PackagedProductDefinition.Package.Property.class,
        net.sovrinhealth.fhir.model.resource.PackagedProductDefinition.Package.ShelfLifeStorage.class,
        net.sovrinhealth.fhir.model.resource.Parameters.class,
        net.sovrinhealth.fhir.model.resource.Parameters.Parameter.class,
        net.sovrinhealth.fhir.model.resource.Patient.class,
        net.sovrinhealth.fhir.model.resource.Patient.Communication.class,
        net.sovrinhealth.fhir.model.resource.Patient.Contact.class,
        net.sovrinhealth.fhir.model.resource.Patient.Link.class,
        net.sovrinhealth.fhir.model.resource.PaymentNotice.class,
        net.sovrinhealth.fhir.model.resource.PaymentReconciliation.class,
        net.sovrinhealth.fhir.model.resource.PaymentReconciliation.Detail.class,
        net.sovrinhealth.fhir.model.resource.PaymentReconciliation.ProcessNote.class,
        net.sovrinhealth.fhir.model.resource.Person.class,
        net.sovrinhealth.fhir.model.resource.Person.Link.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.Action.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.Action.Condition.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.Action.DynamicValue.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.Action.Participant.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.Action.RelatedAction.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.Goal.class,
        net.sovrinhealth.fhir.model.resource.PlanDefinition.Goal.Target.class,
        net.sovrinhealth.fhir.model.resource.Practitioner.class,
        net.sovrinhealth.fhir.model.resource.Practitioner.Qualification.class,
        net.sovrinhealth.fhir.model.resource.PractitionerRole.class,
        net.sovrinhealth.fhir.model.resource.PractitionerRole.AvailableTime.class,
        net.sovrinhealth.fhir.model.resource.PractitionerRole.NotAvailable.class,
        net.sovrinhealth.fhir.model.resource.Procedure.class,
        net.sovrinhealth.fhir.model.resource.Procedure.FocalDevice.class,
        net.sovrinhealth.fhir.model.resource.Procedure.Performer.class,
        net.sovrinhealth.fhir.model.resource.Provenance.class,
        net.sovrinhealth.fhir.model.resource.Provenance.Agent.class,
        net.sovrinhealth.fhir.model.resource.Provenance.Entity.class,
        net.sovrinhealth.fhir.model.resource.Questionnaire.class,
        net.sovrinhealth.fhir.model.resource.Questionnaire.Item.class,
        net.sovrinhealth.fhir.model.resource.Questionnaire.Item.AnswerOption.class,
        net.sovrinhealth.fhir.model.resource.Questionnaire.Item.EnableWhen.class,
        net.sovrinhealth.fhir.model.resource.Questionnaire.Item.Initial.class,
        net.sovrinhealth.fhir.model.resource.QuestionnaireResponse.class,
        net.sovrinhealth.fhir.model.resource.QuestionnaireResponse.Item.class,
        net.sovrinhealth.fhir.model.resource.QuestionnaireResponse.Item.Answer.class,
        net.sovrinhealth.fhir.model.resource.RegulatedAuthorization.class,
        net.sovrinhealth.fhir.model.resource.RegulatedAuthorization.Case.class,
        net.sovrinhealth.fhir.model.resource.RelatedPerson.class,
        net.sovrinhealth.fhir.model.resource.RelatedPerson.Communication.class,
        net.sovrinhealth.fhir.model.resource.RequestGroup.class,
        net.sovrinhealth.fhir.model.resource.RequestGroup.Action.class,
        net.sovrinhealth.fhir.model.resource.RequestGroup.Action.Condition.class,
        net.sovrinhealth.fhir.model.resource.RequestGroup.Action.RelatedAction.class,
        net.sovrinhealth.fhir.model.resource.ResearchDefinition.class,
        net.sovrinhealth.fhir.model.resource.ResearchElementDefinition.class,
        net.sovrinhealth.fhir.model.resource.ResearchElementDefinition.Characteristic.class,
        net.sovrinhealth.fhir.model.resource.ResearchStudy.class,
        net.sovrinhealth.fhir.model.resource.ResearchStudy.Arm.class,
        net.sovrinhealth.fhir.model.resource.ResearchStudy.Objective.class,
        net.sovrinhealth.fhir.model.resource.ResearchSubject.class,
        net.sovrinhealth.fhir.model.resource.Resource.class,
        net.sovrinhealth.fhir.model.resource.RiskAssessment.class,
        net.sovrinhealth.fhir.model.resource.RiskAssessment.Prediction.class,
        net.sovrinhealth.fhir.model.resource.Schedule.class,
        net.sovrinhealth.fhir.model.resource.SearchParameter.class,
        net.sovrinhealth.fhir.model.resource.SearchParameter.Component.class,
        net.sovrinhealth.fhir.model.resource.ServiceRequest.class,
        net.sovrinhealth.fhir.model.resource.Slot.class,
        net.sovrinhealth.fhir.model.resource.Specimen.class,
        net.sovrinhealth.fhir.model.resource.Specimen.Collection.class,
        net.sovrinhealth.fhir.model.resource.Specimen.Container.class,
        net.sovrinhealth.fhir.model.resource.Specimen.Processing.class,
        net.sovrinhealth.fhir.model.resource.SpecimenDefinition.class,
        net.sovrinhealth.fhir.model.resource.SpecimenDefinition.TypeTested.class,
        net.sovrinhealth.fhir.model.resource.SpecimenDefinition.TypeTested.Container.class,
        net.sovrinhealth.fhir.model.resource.SpecimenDefinition.TypeTested.Container.Additive.class,
        net.sovrinhealth.fhir.model.resource.SpecimenDefinition.TypeTested.Handling.class,
        net.sovrinhealth.fhir.model.resource.StructureDefinition.class,
        net.sovrinhealth.fhir.model.resource.StructureDefinition.Context.class,
        net.sovrinhealth.fhir.model.resource.StructureDefinition.Differential.class,
        net.sovrinhealth.fhir.model.resource.StructureDefinition.Mapping.class,
        net.sovrinhealth.fhir.model.resource.StructureDefinition.Snapshot.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Group.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Group.Input.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Group.Rule.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Group.Rule.Dependent.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Group.Rule.Source.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Group.Rule.Target.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Group.Rule.Target.Parameter.class,
        net.sovrinhealth.fhir.model.resource.StructureMap.Structure.class,
        net.sovrinhealth.fhir.model.resource.Subscription.class,
        net.sovrinhealth.fhir.model.resource.Subscription.Channel.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionStatus.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionStatus.NotificationEvent.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionTopic.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionTopic.CanFilterBy.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionTopic.EventTrigger.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionTopic.NotificationShape.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionTopic.ResourceTrigger.class,
        net.sovrinhealth.fhir.model.resource.SubscriptionTopic.ResourceTrigger.QueryCriteria.class,
        net.sovrinhealth.fhir.model.resource.Substance.class,
        net.sovrinhealth.fhir.model.resource.Substance.Ingredient.class,
        net.sovrinhealth.fhir.model.resource.Substance.Instance.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Code.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Moiety.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.MolecularWeight.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Name.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Name.Official.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Property.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Relationship.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.SourceMaterial.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Structure.class,
        net.sovrinhealth.fhir.model.resource.SubstanceDefinition.Structure.Representation.class,
        net.sovrinhealth.fhir.model.resource.SupplyDelivery.class,
        net.sovrinhealth.fhir.model.resource.SupplyDelivery.SuppliedItem.class,
        net.sovrinhealth.fhir.model.resource.SupplyRequest.class,
        net.sovrinhealth.fhir.model.resource.SupplyRequest.Parameter.class,
        net.sovrinhealth.fhir.model.resource.Task.class,
        net.sovrinhealth.fhir.model.resource.Task.Input.class,
        net.sovrinhealth.fhir.model.resource.Task.Output.class,
        net.sovrinhealth.fhir.model.resource.Task.Restriction.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.Closure.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.CodeSystem.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.CodeSystem.Version.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.CodeSystem.Version.Filter.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.Expansion.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.Expansion.Parameter.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.Implementation.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.Software.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.Translation.class,
        net.sovrinhealth.fhir.model.resource.TerminologyCapabilities.ValidateCode.class,
        net.sovrinhealth.fhir.model.resource.TestReport.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Participant.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Setup.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Setup.Action.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Setup.Action.Assert.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Setup.Action.Operation.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Teardown.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Teardown.Action.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Test.class,
        net.sovrinhealth.fhir.model.resource.TestReport.Test.Action.class,
        net.sovrinhealth.fhir.model.resource.TestScript.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Destination.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Fixture.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Metadata.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Metadata.Capability.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Metadata.Link.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Origin.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Setup.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Setup.Action.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Setup.Action.Assert.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Setup.Action.Operation.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Setup.Action.Operation.RequestHeader.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Teardown.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Teardown.Action.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Test.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Test.Action.class,
        net.sovrinhealth.fhir.model.resource.TestScript.Variable.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Compose.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Compose.Include.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Compose.Include.Concept.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Compose.Include.Concept.Designation.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Compose.Include.Filter.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Expansion.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Expansion.Contains.class,
        net.sovrinhealth.fhir.model.resource.ValueSet.Expansion.Parameter.class,
        net.sovrinhealth.fhir.model.resource.VerificationResult.class,
        net.sovrinhealth.fhir.model.resource.VerificationResult.Attestation.class,
        net.sovrinhealth.fhir.model.resource.VerificationResult.PrimarySource.class,
        net.sovrinhealth.fhir.model.resource.VerificationResult.Validator.class,
        net.sovrinhealth.fhir.model.resource.VisionPrescription.class,
        net.sovrinhealth.fhir.model.resource.VisionPrescription.LensSpecification.class,
        net.sovrinhealth.fhir.model.resource.VisionPrescription.LensSpecification.Prism.class,
        net.sovrinhealth.fhir.model.type.Address.class,
        net.sovrinhealth.fhir.model.type.Age.class,
        net.sovrinhealth.fhir.model.type.Annotation.class,
        net.sovrinhealth.fhir.model.type.Attachment.class,
        net.sovrinhealth.fhir.model.type.BackboneElement.class,
        net.sovrinhealth.fhir.model.type.Base64Binary.class,
        net.sovrinhealth.fhir.model.type.Boolean.class,
        net.sovrinhealth.fhir.model.type.Canonical.class,
        net.sovrinhealth.fhir.model.type.Code.class,
        net.sovrinhealth.fhir.model.type.CodeableConcept.class,
        net.sovrinhealth.fhir.model.type.CodeableReference.class,
        net.sovrinhealth.fhir.model.type.Coding.class,
        net.sovrinhealth.fhir.model.type.ContactDetail.class,
        net.sovrinhealth.fhir.model.type.ContactPoint.class,
        net.sovrinhealth.fhir.model.type.Contributor.class,
        net.sovrinhealth.fhir.model.type.Count.class,
        net.sovrinhealth.fhir.model.type.DataRequirement.class,
        net.sovrinhealth.fhir.model.type.DataRequirement.CodeFilter.class,
        net.sovrinhealth.fhir.model.type.DataRequirement.DateFilter.class,
        net.sovrinhealth.fhir.model.type.DataRequirement.Sort.class,
        net.sovrinhealth.fhir.model.type.Date.class,
        net.sovrinhealth.fhir.model.type.DateTime.class,
        net.sovrinhealth.fhir.model.type.Decimal.class,
        net.sovrinhealth.fhir.model.type.Distance.class,
        net.sovrinhealth.fhir.model.type.Dosage.class,
        net.sovrinhealth.fhir.model.type.Dosage.DoseAndRate.class,
        net.sovrinhealth.fhir.model.type.Duration.class,
        net.sovrinhealth.fhir.model.type.Element.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Base.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Binding.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Constraint.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Example.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Mapping.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Slicing.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Slicing.Discriminator.class,
        net.sovrinhealth.fhir.model.type.ElementDefinition.Type.class,
        net.sovrinhealth.fhir.model.type.Expression.class,
        net.sovrinhealth.fhir.model.type.Extension.class,
        net.sovrinhealth.fhir.model.type.HumanName.class,
        net.sovrinhealth.fhir.model.type.Id.class,
        net.sovrinhealth.fhir.model.type.Identifier.class,
        net.sovrinhealth.fhir.model.type.Instant.class,
        net.sovrinhealth.fhir.model.type.Integer.class,
        net.sovrinhealth.fhir.model.type.Markdown.class,
        net.sovrinhealth.fhir.model.type.MarketingStatus.class,
        net.sovrinhealth.fhir.model.type.Meta.class,
        net.sovrinhealth.fhir.model.type.Money.class,
        net.sovrinhealth.fhir.model.type.MoneyQuantity.class,
        net.sovrinhealth.fhir.model.type.Narrative.class,
        net.sovrinhealth.fhir.model.type.Oid.class,
        net.sovrinhealth.fhir.model.type.ParameterDefinition.class,
        net.sovrinhealth.fhir.model.type.Period.class,
        net.sovrinhealth.fhir.model.type.Population.class,
        net.sovrinhealth.fhir.model.type.PositiveInt.class,
        net.sovrinhealth.fhir.model.type.ProdCharacteristic.class,
        net.sovrinhealth.fhir.model.type.ProductShelfLife.class,
        net.sovrinhealth.fhir.model.type.Quantity.class,
        net.sovrinhealth.fhir.model.type.Range.class,
        net.sovrinhealth.fhir.model.type.Ratio.class,
        net.sovrinhealth.fhir.model.type.RatioRange.class,
        net.sovrinhealth.fhir.model.type.Reference.class,
        net.sovrinhealth.fhir.model.type.RelatedArtifact.class,
        net.sovrinhealth.fhir.model.type.SampledData.class,
        net.sovrinhealth.fhir.model.type.Signature.class,
        net.sovrinhealth.fhir.model.type.SimpleQuantity.class,
        net.sovrinhealth.fhir.model.type.String.class,
        net.sovrinhealth.fhir.model.type.Time.class,
        net.sovrinhealth.fhir.model.type.Timing.class,
        net.sovrinhealth.fhir.model.type.Timing.Repeat.class,
        net.sovrinhealth.fhir.model.type.TriggerDefinition.class,
        net.sovrinhealth.fhir.model.type.UnsignedInt.class,
        net.sovrinhealth.fhir.model.type.Uri.class,
        net.sovrinhealth.fhir.model.type.Url.class,
        net.sovrinhealth.fhir.model.type.UsageContext.class,
        net.sovrinhealth.fhir.model.type.Uuid.class,
        net.sovrinhealth.fhir.model.type.Xhtml.class
            );
    private static final Map<Class<?>, Map<String, ElementInfo>> MODEL_CLASS_ELEMENT_INFO_MAP = buildModelClassElementInfoMap();
    private static final Map<String, Class<? extends Resource>> RESOURCE_TYPE_MAP = buildResourceTypeMap();
    private static final Set<Class<? extends Resource>> CONCRETE_RESOURCE_TYPES = getResourceTypes().stream()
            .filter(rt -> !isAbstract(rt))
            .collect(Collectors.toSet());
    private static final Map<Class<?>, List<Constraint>> MODEL_CLASS_CONSTRAINT_MAP = buildModelClassConstraintMap();
    // LinkedHashSet is used just to preserve the order, for convenience only
    private static final Set<Class<? extends Element>> CHOICE_ELEMENT_TYPES = new LinkedHashSet<>(Arrays.asList(
            Base64Binary.class,
            net.sovrinhealth.fhir.model.type.Boolean.class,
            Canonical.class,
            Code.class,
            Date.class,
            DateTime.class,
            Decimal.class,
            Id.class,
            Instant.class,
            net.sovrinhealth.fhir.model.type.Integer.class,
            Markdown.class,
            Oid.class,
            PositiveInt.class,
            net.sovrinhealth.fhir.model.type.String.class,
            Time.class,
            UnsignedInt.class,
            Uri.class,
            Url.class,
            Uuid.class,
            Address.class,
            Age.class,
            Annotation.class,
            Attachment.class,
            CodeableConcept.class,
            CodeableReference.class,
            Coding.class,
            ContactPoint.class,
            Count.class,
            Distance.class,
            Duration.class,
            HumanName.class,
            Identifier.class,
            Money.class,
            MoneyQuantity.class, // profiled type
            Period.class,
            Quantity.class,
            Range.class,
            Ratio.class,
            RatioRange.class,
            Reference.class,
            SampledData.class,
            SimpleQuantity.class, // profiled type
            Signature.class,
            Timing.class,
            ContactDetail.class,
            Contributor.class,
            DataRequirement.class,
            Expression.class,
            ParameterDefinition.class,
            RelatedArtifact.class,
            TriggerDefinition.class,
            UsageContext.class,
            Dosage.class,
            Meta.class));
    private static final Set<Class<? extends Element>> DATA_TYPES;
    static {
        // LinkedHashSet is used just to preserve the order, for convenience only
        Set<Class<? extends Element>> dataTypes = new LinkedHashSet<>(CHOICE_ELEMENT_TYPES);
        dataTypes.add(Xhtml.class);
        dataTypes.add(Narrative.class);
        dataTypes.add(Extension.class);
        dataTypes.add(ElementDefinition.class);
        dataTypes.add(MarketingStatus.class);
        dataTypes.add(Population.class);
        dataTypes.add(ProductShelfLife.class);
        dataTypes.add(ProdCharacteristic.class);
        DATA_TYPES = Collections.unmodifiableSet(dataTypes);
    }
    private static final Map<String, Class<?>> DATA_TYPE_MAP = buildDataTypeMap();
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "$index",
        "$this",
        "$total",
        "and",
        "as",
        "contains",
        "day",
        "days",
        "div",
        "false",
        "hour",
        "hours",
        "implies",
        "in",
        "is",
        "millisecond",
        "milliseconds",
        "minute",
        "minutes",
        "mod",
        "month",
        "months",
        "or",
        "seconds",
        "true",
        "week",
        "weeks",
        "xor",
        "year",
        "years",
        "second"
    ));
    private static final Map<String, Class<?>> CODE_SUBTYPE_MAP = buildCodeSubtypeMap();

    private ModelSupport() { }

    /**
     * Calling this method allows us to load/initialize this class during startup.
     */
    public static void init() { }

    public static final class ElementInfo {
        private final String name;
        private final Class<?> type;
        private final Class<?> declaringType;
        private final boolean required;
        private final boolean repeating;
        private final boolean choice;
        private final Set<Class<?>> choiceTypes;
        private final boolean reference;
        private final Set<String> referenceTypes;
        private final Binding binding;
        private final boolean summary;

        private final Set<String> choiceElementNames;

        ElementInfo(String name,
                Class<?> type,
                Class<?> declaringType,
                boolean required,
                boolean repeating,
                boolean choice,
                Set<Class<?>> choiceTypes,
                boolean reference,
                Set<String> referenceTypes,
                Binding binding,
                boolean isSummary) {
            this.name = name;
            this.declaringType = declaringType;
            this.type = type;
            this.required = required;
            this.repeating = repeating;
            this.choice = choice;
            this.choiceTypes = choiceTypes;
            this.reference = reference;
            this.referenceTypes = referenceTypes;
            this.binding = binding;
            this.summary = isSummary;
            Set<String> choiceElementNames = new LinkedHashSet<>();
            if (this.choice) {
                for (Class<?> choiceType : this.choiceTypes) {
                    choiceElementNames.add(getChoiceElementName(this.name, choiceType));
                }
            }
            this.choiceElementNames = Collections.unmodifiableSet(choiceElementNames);
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public Class<?> getDeclaringType() {
            return declaringType;
        }

        public boolean isDeclaredBy(Class<?> type) {
            return declaringType.equals(type);
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isSummary() {
            return summary;
        }

        public boolean isRepeating() {
            return repeating;
        }

        public boolean isChoice() {
            return choice;
        }

        public Set<Class<?>> getChoiceTypes() {
            return choiceTypes;
        }

        public boolean isReference() {
            return reference;
        }

        public Set<String> getReferenceTypes() {
            return referenceTypes;
        }

        public Binding getBinding() {
            return binding;
        }

        public boolean hasBinding() {
            return (binding != null);
        }

        public Set<String> getChoiceElementNames() {
            return choiceElementNames;
        }
    }

    private static Map<String, Class<?>> buildCodeSubtypeMap() {
        Map<String, Class<?>> codeSubtypeMap = new LinkedHashMap<>();
        codeSubtypeMap.put("AccountStatus", net.sovrinhealth.fhir.model.type.code.AccountStatus.class);
        codeSubtypeMap.put("ActionCardinalityBehavior", net.sovrinhealth.fhir.model.type.code.ActionCardinalityBehavior.class);
        codeSubtypeMap.put("ActionConditionKind", net.sovrinhealth.fhir.model.type.code.ActionConditionKind.class);
        codeSubtypeMap.put("ActionGroupingBehavior", net.sovrinhealth.fhir.model.type.code.ActionGroupingBehavior.class);
        codeSubtypeMap.put("ActionParticipantType", net.sovrinhealth.fhir.model.type.code.ActionParticipantType.class);
        codeSubtypeMap.put("ActionPrecheckBehavior", net.sovrinhealth.fhir.model.type.code.ActionPrecheckBehavior.class);
        codeSubtypeMap.put("ActionRelationshipType", net.sovrinhealth.fhir.model.type.code.ActionRelationshipType.class);
        codeSubtypeMap.put("ActionRequiredBehavior", net.sovrinhealth.fhir.model.type.code.ActionRequiredBehavior.class);
        codeSubtypeMap.put("ActionSelectionBehavior", net.sovrinhealth.fhir.model.type.code.ActionSelectionBehavior.class);
        codeSubtypeMap.put("ActivityDefinitionKind", net.sovrinhealth.fhir.model.type.code.ActivityDefinitionKind.class);
        codeSubtypeMap.put("ActivityParticipantType", net.sovrinhealth.fhir.model.type.code.ActivityParticipantType.class);
        codeSubtypeMap.put("AddressType", net.sovrinhealth.fhir.model.type.code.AddressType.class);
        codeSubtypeMap.put("AddressUse", net.sovrinhealth.fhir.model.type.code.AddressUse.class);
        codeSubtypeMap.put("AdministrativeGender", net.sovrinhealth.fhir.model.type.code.AdministrativeGender.class);
        codeSubtypeMap.put("AdverseEventActuality", net.sovrinhealth.fhir.model.type.code.AdverseEventActuality.class);
        codeSubtypeMap.put("AggregationMode", net.sovrinhealth.fhir.model.type.code.AggregationMode.class);
        codeSubtypeMap.put("AllergyIntoleranceCategory", net.sovrinhealth.fhir.model.type.code.AllergyIntoleranceCategory.class);
        codeSubtypeMap.put("AllergyIntoleranceCriticality", net.sovrinhealth.fhir.model.type.code.AllergyIntoleranceCriticality.class);
        codeSubtypeMap.put("AllergyIntoleranceSeverity", net.sovrinhealth.fhir.model.type.code.AllergyIntoleranceSeverity.class);
        codeSubtypeMap.put("AllergyIntoleranceType", net.sovrinhealth.fhir.model.type.code.AllergyIntoleranceType.class);
        codeSubtypeMap.put("AppointmentStatus", net.sovrinhealth.fhir.model.type.code.AppointmentStatus.class);
        codeSubtypeMap.put("AssertionDirectionType", net.sovrinhealth.fhir.model.type.code.AssertionDirectionType.class);
        codeSubtypeMap.put("AssertionOperatorType", net.sovrinhealth.fhir.model.type.code.AssertionOperatorType.class);
        codeSubtypeMap.put("AssertionResponseTypes", net.sovrinhealth.fhir.model.type.code.AssertionResponseTypes.class);
        codeSubtypeMap.put("AuditEventAction", net.sovrinhealth.fhir.model.type.code.AuditEventAction.class);
        codeSubtypeMap.put("AuditEventAgentNetworkType", net.sovrinhealth.fhir.model.type.code.AuditEventAgentNetworkType.class);
        codeSubtypeMap.put("AuditEventOutcome", net.sovrinhealth.fhir.model.type.code.AuditEventOutcome.class);
        codeSubtypeMap.put("BindingStrength", net.sovrinhealth.fhir.model.type.code.BindingStrength.class);
        codeSubtypeMap.put("BiologicallyDerivedProductCategory", net.sovrinhealth.fhir.model.type.code.BiologicallyDerivedProductCategory.class);
        codeSubtypeMap.put("BiologicallyDerivedProductStatus", net.sovrinhealth.fhir.model.type.code.BiologicallyDerivedProductStatus.class);
        codeSubtypeMap.put("BiologicallyDerivedProductStorageScale", net.sovrinhealth.fhir.model.type.code.BiologicallyDerivedProductStorageScale.class);
        codeSubtypeMap.put("BundleType", net.sovrinhealth.fhir.model.type.code.BundleType.class);
        codeSubtypeMap.put("CapabilityStatementKind", net.sovrinhealth.fhir.model.type.code.CapabilityStatementKind.class);
        codeSubtypeMap.put("CarePlanActivityKind", net.sovrinhealth.fhir.model.type.code.CarePlanActivityKind.class);
        codeSubtypeMap.put("CarePlanActivityStatus", net.sovrinhealth.fhir.model.type.code.CarePlanActivityStatus.class);
        codeSubtypeMap.put("CarePlanIntent", net.sovrinhealth.fhir.model.type.code.CarePlanIntent.class);
        codeSubtypeMap.put("CarePlanStatus", net.sovrinhealth.fhir.model.type.code.CarePlanStatus.class);
        codeSubtypeMap.put("CareTeamStatus", net.sovrinhealth.fhir.model.type.code.CareTeamStatus.class);
        codeSubtypeMap.put("CatalogEntryRelationType", net.sovrinhealth.fhir.model.type.code.CatalogEntryRelationType.class);
        codeSubtypeMap.put("CharacteristicCombination", net.sovrinhealth.fhir.model.type.code.CharacteristicCombination.class);
        codeSubtypeMap.put("ChargeItemDefinitionPriceComponentType", net.sovrinhealth.fhir.model.type.code.ChargeItemDefinitionPriceComponentType.class);
        codeSubtypeMap.put("ChargeItemStatus", net.sovrinhealth.fhir.model.type.code.ChargeItemStatus.class);
        codeSubtypeMap.put("ClaimResponseStatus", net.sovrinhealth.fhir.model.type.code.ClaimResponseStatus.class);
        codeSubtypeMap.put("ClaimStatus", net.sovrinhealth.fhir.model.type.code.ClaimStatus.class);
        codeSubtypeMap.put("ClinicalImpressionStatus", net.sovrinhealth.fhir.model.type.code.ClinicalImpressionStatus.class);
        codeSubtypeMap.put("ClinicalUseDefinitionType", net.sovrinhealth.fhir.model.type.code.ClinicalUseDefinitionType.class);
        codeSubtypeMap.put("CodeSearchSupport", net.sovrinhealth.fhir.model.type.code.CodeSearchSupport.class);
        codeSubtypeMap.put("CodeSystemContentMode", net.sovrinhealth.fhir.model.type.code.CodeSystemContentMode.class);
        codeSubtypeMap.put("CodeSystemHierarchyMeaning", net.sovrinhealth.fhir.model.type.code.CodeSystemHierarchyMeaning.class);
        codeSubtypeMap.put("CommunicationPriority", net.sovrinhealth.fhir.model.type.code.CommunicationPriority.class);
        codeSubtypeMap.put("CommunicationRequestStatus", net.sovrinhealth.fhir.model.type.code.CommunicationRequestStatus.class);
        codeSubtypeMap.put("CommunicationStatus", net.sovrinhealth.fhir.model.type.code.CommunicationStatus.class);
        codeSubtypeMap.put("CompartmentCode", net.sovrinhealth.fhir.model.type.code.CompartmentCode.class);
        codeSubtypeMap.put("CompartmentType", net.sovrinhealth.fhir.model.type.code.CompartmentType.class);
        codeSubtypeMap.put("CompositionAttestationMode", net.sovrinhealth.fhir.model.type.code.CompositionAttestationMode.class);
        codeSubtypeMap.put("CompositionStatus", net.sovrinhealth.fhir.model.type.code.CompositionStatus.class);
        codeSubtypeMap.put("ConceptMapEquivalence", net.sovrinhealth.fhir.model.type.code.ConceptMapEquivalence.class);
        codeSubtypeMap.put("ConceptMapGroupUnmappedMode", net.sovrinhealth.fhir.model.type.code.ConceptMapGroupUnmappedMode.class);
        codeSubtypeMap.put("ConceptSubsumptionOutcome", net.sovrinhealth.fhir.model.type.code.ConceptSubsumptionOutcome.class);
        codeSubtypeMap.put("ConditionalDeleteStatus", net.sovrinhealth.fhir.model.type.code.ConditionalDeleteStatus.class);
        codeSubtypeMap.put("ConditionalReadStatus", net.sovrinhealth.fhir.model.type.code.ConditionalReadStatus.class);
        codeSubtypeMap.put("ConsentDataMeaning", net.sovrinhealth.fhir.model.type.code.ConsentDataMeaning.class);
        codeSubtypeMap.put("ConsentProvisionType", net.sovrinhealth.fhir.model.type.code.ConsentProvisionType.class);
        codeSubtypeMap.put("ConsentState", net.sovrinhealth.fhir.model.type.code.ConsentState.class);
        codeSubtypeMap.put("ConstraintSeverity", net.sovrinhealth.fhir.model.type.code.ConstraintSeverity.class);
        codeSubtypeMap.put("ContactPointSystem", net.sovrinhealth.fhir.model.type.code.ContactPointSystem.class);
        codeSubtypeMap.put("ContactPointUse", net.sovrinhealth.fhir.model.type.code.ContactPointUse.class);
        codeSubtypeMap.put("ContractPublicationStatus", net.sovrinhealth.fhir.model.type.code.ContractPublicationStatus.class);
        codeSubtypeMap.put("ContractStatus", net.sovrinhealth.fhir.model.type.code.ContractStatus.class);
        codeSubtypeMap.put("ContributorType", net.sovrinhealth.fhir.model.type.code.ContributorType.class);
        codeSubtypeMap.put("CoverageStatus", net.sovrinhealth.fhir.model.type.code.CoverageStatus.class);
        codeSubtypeMap.put("CriteriaNotExistsBehavior", net.sovrinhealth.fhir.model.type.code.CriteriaNotExistsBehavior.class);
        codeSubtypeMap.put("DataAbsentReason", net.sovrinhealth.fhir.model.type.code.DataAbsentReason.class);
        codeSubtypeMap.put("DayOfWeek", net.sovrinhealth.fhir.model.type.code.DayOfWeek.class);
        codeSubtypeMap.put("DaysOfWeek", net.sovrinhealth.fhir.model.type.code.DaysOfWeek.class);
        codeSubtypeMap.put("DetectedIssueSeverity", net.sovrinhealth.fhir.model.type.code.DetectedIssueSeverity.class);
        codeSubtypeMap.put("DetectedIssueStatus", net.sovrinhealth.fhir.model.type.code.DetectedIssueStatus.class);
        codeSubtypeMap.put("DeviceMetricCalibrationState", net.sovrinhealth.fhir.model.type.code.DeviceMetricCalibrationState.class);
        codeSubtypeMap.put("DeviceMetricCalibrationType", net.sovrinhealth.fhir.model.type.code.DeviceMetricCalibrationType.class);
        codeSubtypeMap.put("DeviceMetricCategory", net.sovrinhealth.fhir.model.type.code.DeviceMetricCategory.class);
        codeSubtypeMap.put("DeviceMetricColor", net.sovrinhealth.fhir.model.type.code.DeviceMetricColor.class);
        codeSubtypeMap.put("DeviceMetricOperationalStatus", net.sovrinhealth.fhir.model.type.code.DeviceMetricOperationalStatus.class);
        codeSubtypeMap.put("DeviceNameType", net.sovrinhealth.fhir.model.type.code.DeviceNameType.class);
        codeSubtypeMap.put("DeviceRequestStatus", net.sovrinhealth.fhir.model.type.code.DeviceRequestStatus.class);
        codeSubtypeMap.put("DeviceUseStatementStatus", net.sovrinhealth.fhir.model.type.code.DeviceUseStatementStatus.class);
        codeSubtypeMap.put("DiagnosticReportStatus", net.sovrinhealth.fhir.model.type.code.DiagnosticReportStatus.class);
        codeSubtypeMap.put("DiscriminatorType", net.sovrinhealth.fhir.model.type.code.DiscriminatorType.class);
        codeSubtypeMap.put("DocumentConfidentiality", net.sovrinhealth.fhir.model.type.code.DocumentConfidentiality.class);
        codeSubtypeMap.put("DocumentMode", net.sovrinhealth.fhir.model.type.code.DocumentMode.class);
        codeSubtypeMap.put("DocumentReferenceStatus", net.sovrinhealth.fhir.model.type.code.DocumentReferenceStatus.class);
        codeSubtypeMap.put("DocumentRelationshipType", net.sovrinhealth.fhir.model.type.code.DocumentRelationshipType.class);
        codeSubtypeMap.put("EligibilityRequestPurpose", net.sovrinhealth.fhir.model.type.code.EligibilityRequestPurpose.class);
        codeSubtypeMap.put("EligibilityRequestStatus", net.sovrinhealth.fhir.model.type.code.EligibilityRequestStatus.class);
        codeSubtypeMap.put("EligibilityResponsePurpose", net.sovrinhealth.fhir.model.type.code.EligibilityResponsePurpose.class);
        codeSubtypeMap.put("EligibilityResponseStatus", net.sovrinhealth.fhir.model.type.code.EligibilityResponseStatus.class);
        codeSubtypeMap.put("EnableWhenBehavior", net.sovrinhealth.fhir.model.type.code.EnableWhenBehavior.class);
        codeSubtypeMap.put("EncounterLocationStatus", net.sovrinhealth.fhir.model.type.code.EncounterLocationStatus.class);
        codeSubtypeMap.put("EncounterStatus", net.sovrinhealth.fhir.model.type.code.EncounterStatus.class);
        codeSubtypeMap.put("EndpointStatus", net.sovrinhealth.fhir.model.type.code.EndpointStatus.class);
        codeSubtypeMap.put("EnrollmentRequestStatus", net.sovrinhealth.fhir.model.type.code.EnrollmentRequestStatus.class);
        codeSubtypeMap.put("EnrollmentResponseStatus", net.sovrinhealth.fhir.model.type.code.EnrollmentResponseStatus.class);
        codeSubtypeMap.put("EpisodeOfCareStatus", net.sovrinhealth.fhir.model.type.code.EpisodeOfCareStatus.class);
        codeSubtypeMap.put("EventCapabilityMode", net.sovrinhealth.fhir.model.type.code.EventCapabilityMode.class);
        codeSubtypeMap.put("EventTiming", net.sovrinhealth.fhir.model.type.code.EventTiming.class);
        codeSubtypeMap.put("EvidenceVariableHandling", net.sovrinhealth.fhir.model.type.code.EvidenceVariableHandling.class);
        codeSubtypeMap.put("ExampleScenarioActorType", net.sovrinhealth.fhir.model.type.code.ExampleScenarioActorType.class);
        codeSubtypeMap.put("ExplanationOfBenefitStatus", net.sovrinhealth.fhir.model.type.code.ExplanationOfBenefitStatus.class);
        codeSubtypeMap.put("ExtensionContextType", net.sovrinhealth.fhir.model.type.code.ExtensionContextType.class);
        codeSubtypeMap.put("FamilyHistoryStatus", net.sovrinhealth.fhir.model.type.code.FamilyHistoryStatus.class);
        codeSubtypeMap.put("FHIRAllTypes", net.sovrinhealth.fhir.model.type.code.FHIRAllTypes.class);
        codeSubtypeMap.put("FHIRDefinedType", net.sovrinhealth.fhir.model.type.code.FHIRDefinedType.class);
        codeSubtypeMap.put("FHIRDeviceStatus", net.sovrinhealth.fhir.model.type.code.FHIRDeviceStatus.class);
        codeSubtypeMap.put("FHIRSubstanceStatus", net.sovrinhealth.fhir.model.type.code.FHIRSubstanceStatus.class);
        codeSubtypeMap.put("FHIRVersion", net.sovrinhealth.fhir.model.type.code.FHIRVersion.class);
        codeSubtypeMap.put("FilterOperator", net.sovrinhealth.fhir.model.type.code.FilterOperator.class);
        codeSubtypeMap.put("FlagStatus", net.sovrinhealth.fhir.model.type.code.FlagStatus.class);
        codeSubtypeMap.put("GoalLifecycleStatus", net.sovrinhealth.fhir.model.type.code.GoalLifecycleStatus.class);
        codeSubtypeMap.put("GraphCompartmentRule", net.sovrinhealth.fhir.model.type.code.GraphCompartmentRule.class);
        codeSubtypeMap.put("GraphCompartmentUse", net.sovrinhealth.fhir.model.type.code.GraphCompartmentUse.class);
        codeSubtypeMap.put("GroupMeasure", net.sovrinhealth.fhir.model.type.code.GroupMeasure.class);
        codeSubtypeMap.put("GroupType", net.sovrinhealth.fhir.model.type.code.GroupType.class);
        codeSubtypeMap.put("GuidanceResponseStatus", net.sovrinhealth.fhir.model.type.code.GuidanceResponseStatus.class);
        codeSubtypeMap.put("GuidePageGeneration", net.sovrinhealth.fhir.model.type.code.GuidePageGeneration.class);
        codeSubtypeMap.put("GuideParameterCode", net.sovrinhealth.fhir.model.type.code.GuideParameterCode.class);
        codeSubtypeMap.put("HTTPVerb", net.sovrinhealth.fhir.model.type.code.HTTPVerb.class);
        codeSubtypeMap.put("IdentifierUse", net.sovrinhealth.fhir.model.type.code.IdentifierUse.class);
        codeSubtypeMap.put("IdentityAssuranceLevel", net.sovrinhealth.fhir.model.type.code.IdentityAssuranceLevel.class);
        codeSubtypeMap.put("ImagingStudyStatus", net.sovrinhealth.fhir.model.type.code.ImagingStudyStatus.class);
        codeSubtypeMap.put("ImmunizationEvaluationStatus", net.sovrinhealth.fhir.model.type.code.ImmunizationEvaluationStatus.class);
        codeSubtypeMap.put("ImmunizationStatus", net.sovrinhealth.fhir.model.type.code.ImmunizationStatus.class);
        codeSubtypeMap.put("InvoicePriceComponentType", net.sovrinhealth.fhir.model.type.code.InvoicePriceComponentType.class);
        codeSubtypeMap.put("InvoiceStatus", net.sovrinhealth.fhir.model.type.code.InvoiceStatus.class);
        codeSubtypeMap.put("IssueSeverity", net.sovrinhealth.fhir.model.type.code.IssueSeverity.class);
        codeSubtypeMap.put("IssueType", net.sovrinhealth.fhir.model.type.code.IssueType.class);
        codeSubtypeMap.put("LinkageType", net.sovrinhealth.fhir.model.type.code.LinkageType.class);
        codeSubtypeMap.put("LinkType", net.sovrinhealth.fhir.model.type.code.LinkType.class);
        codeSubtypeMap.put("ListMode", net.sovrinhealth.fhir.model.type.code.ListMode.class);
        codeSubtypeMap.put("ListStatus", net.sovrinhealth.fhir.model.type.code.ListStatus.class);
        codeSubtypeMap.put("LocationMode", net.sovrinhealth.fhir.model.type.code.LocationMode.class);
        codeSubtypeMap.put("LocationStatus", net.sovrinhealth.fhir.model.type.code.LocationStatus.class);
        codeSubtypeMap.put("MeasureReportStatus", net.sovrinhealth.fhir.model.type.code.MeasureReportStatus.class);
        codeSubtypeMap.put("MeasureReportType", net.sovrinhealth.fhir.model.type.code.MeasureReportType.class);
        codeSubtypeMap.put("MediaStatus", net.sovrinhealth.fhir.model.type.code.MediaStatus.class);
        codeSubtypeMap.put("MedicationAdministrationStatus", net.sovrinhealth.fhir.model.type.code.MedicationAdministrationStatus.class);
        codeSubtypeMap.put("MedicationDispenseStatus", net.sovrinhealth.fhir.model.type.code.MedicationDispenseStatus.class);
        codeSubtypeMap.put("MedicationKnowledgeStatus", net.sovrinhealth.fhir.model.type.code.MedicationKnowledgeStatus.class);
        codeSubtypeMap.put("MedicationRequestIntent", net.sovrinhealth.fhir.model.type.code.MedicationRequestIntent.class);
        codeSubtypeMap.put("MedicationRequestPriority", net.sovrinhealth.fhir.model.type.code.MedicationRequestPriority.class);
        codeSubtypeMap.put("MedicationRequestStatus", net.sovrinhealth.fhir.model.type.code.MedicationRequestStatus.class);
        codeSubtypeMap.put("MedicationStatementStatus", net.sovrinhealth.fhir.model.type.code.MedicationStatementStatus.class);
        codeSubtypeMap.put("MedicationStatus", net.sovrinhealth.fhir.model.type.code.MedicationStatus.class);
        codeSubtypeMap.put("MessageHeaderResponseRequest", net.sovrinhealth.fhir.model.type.code.MessageHeaderResponseRequest.class);
        codeSubtypeMap.put("MessageSignificanceCategory", net.sovrinhealth.fhir.model.type.code.MessageSignificanceCategory.class);
        codeSubtypeMap.put("MethodCode", net.sovrinhealth.fhir.model.type.code.MethodCode.class);
        codeSubtypeMap.put("NameUse", net.sovrinhealth.fhir.model.type.code.NameUse.class);
        codeSubtypeMap.put("NamingSystemIdentifierType", net.sovrinhealth.fhir.model.type.code.NamingSystemIdentifierType.class);
        codeSubtypeMap.put("NamingSystemType", net.sovrinhealth.fhir.model.type.code.NamingSystemType.class);
        codeSubtypeMap.put("NarrativeStatus", net.sovrinhealth.fhir.model.type.code.NarrativeStatus.class);
        codeSubtypeMap.put("NoteType", net.sovrinhealth.fhir.model.type.code.NoteType.class);
        codeSubtypeMap.put("NutritionOrderIntent", net.sovrinhealth.fhir.model.type.code.NutritionOrderIntent.class);
        codeSubtypeMap.put("NutritionOrderStatus", net.sovrinhealth.fhir.model.type.code.NutritionOrderStatus.class);
        codeSubtypeMap.put("NutritionProductStatus", net.sovrinhealth.fhir.model.type.code.NutritionProductStatus.class);
        codeSubtypeMap.put("ObservationDataType", net.sovrinhealth.fhir.model.type.code.ObservationDataType.class);
        codeSubtypeMap.put("ObservationRangeCategory", net.sovrinhealth.fhir.model.type.code.ObservationRangeCategory.class);
        codeSubtypeMap.put("ObservationStatus", net.sovrinhealth.fhir.model.type.code.ObservationStatus.class);
        codeSubtypeMap.put("OperationKind", net.sovrinhealth.fhir.model.type.code.OperationKind.class);
        codeSubtypeMap.put("OperationParameterUse", net.sovrinhealth.fhir.model.type.code.OperationParameterUse.class);
        codeSubtypeMap.put("OrientationType", net.sovrinhealth.fhir.model.type.code.OrientationType.class);
        codeSubtypeMap.put("ParameterUse", net.sovrinhealth.fhir.model.type.code.ParameterUse.class);
        codeSubtypeMap.put("ParticipantRequired", net.sovrinhealth.fhir.model.type.code.ParticipantRequired.class);
        codeSubtypeMap.put("ParticipantStatus", net.sovrinhealth.fhir.model.type.code.ParticipantStatus.class);
        codeSubtypeMap.put("ParticipationStatus", net.sovrinhealth.fhir.model.type.code.ParticipationStatus.class);
        codeSubtypeMap.put("PaymentNoticeStatus", net.sovrinhealth.fhir.model.type.code.PaymentNoticeStatus.class);
        codeSubtypeMap.put("PaymentReconciliationStatus", net.sovrinhealth.fhir.model.type.code.PaymentReconciliationStatus.class);
        codeSubtypeMap.put("ProcedureStatus", net.sovrinhealth.fhir.model.type.code.ProcedureStatus.class);
        codeSubtypeMap.put("PropertyRepresentation", net.sovrinhealth.fhir.model.type.code.PropertyRepresentation.class);
        codeSubtypeMap.put("PropertyType", net.sovrinhealth.fhir.model.type.code.PropertyType.class);
        codeSubtypeMap.put("ProvenanceEntityRole", net.sovrinhealth.fhir.model.type.code.ProvenanceEntityRole.class);
        codeSubtypeMap.put("PublicationStatus", net.sovrinhealth.fhir.model.type.code.PublicationStatus.class);
        codeSubtypeMap.put("QualityType", net.sovrinhealth.fhir.model.type.code.QualityType.class);
        codeSubtypeMap.put("QuantityComparator", net.sovrinhealth.fhir.model.type.code.QuantityComparator.class);
        codeSubtypeMap.put("QuestionnaireItemOperator", net.sovrinhealth.fhir.model.type.code.QuestionnaireItemOperator.class);
        codeSubtypeMap.put("QuestionnaireItemType", net.sovrinhealth.fhir.model.type.code.QuestionnaireItemType.class);
        codeSubtypeMap.put("QuestionnaireResponseStatus", net.sovrinhealth.fhir.model.type.code.QuestionnaireResponseStatus.class);
        codeSubtypeMap.put("ReferenceHandlingPolicy", net.sovrinhealth.fhir.model.type.code.ReferenceHandlingPolicy.class);
        codeSubtypeMap.put("ReferenceVersionRules", net.sovrinhealth.fhir.model.type.code.ReferenceVersionRules.class);
        codeSubtypeMap.put("ReferredDocumentStatus", net.sovrinhealth.fhir.model.type.code.ReferredDocumentStatus.class);
        codeSubtypeMap.put("RelatedArtifactType", net.sovrinhealth.fhir.model.type.code.RelatedArtifactType.class);
        codeSubtypeMap.put("RemittanceOutcome", net.sovrinhealth.fhir.model.type.code.RemittanceOutcome.class);
        codeSubtypeMap.put("ReportRelationshipType", net.sovrinhealth.fhir.model.type.code.ReportRelationshipType.class);
        codeSubtypeMap.put("RepositoryType", net.sovrinhealth.fhir.model.type.code.RepositoryType.class);
        codeSubtypeMap.put("RequestIntent", net.sovrinhealth.fhir.model.type.code.RequestIntent.class);
        codeSubtypeMap.put("RequestPriority", net.sovrinhealth.fhir.model.type.code.RequestPriority.class);
        codeSubtypeMap.put("RequestStatus", net.sovrinhealth.fhir.model.type.code.RequestStatus.class);
        codeSubtypeMap.put("ResearchElementType", net.sovrinhealth.fhir.model.type.code.ResearchElementType.class);
        codeSubtypeMap.put("ResearchStudyStatus", net.sovrinhealth.fhir.model.type.code.ResearchStudyStatus.class);
        codeSubtypeMap.put("ResearchSubjectStatus", net.sovrinhealth.fhir.model.type.code.ResearchSubjectStatus.class);
        codeSubtypeMap.put("ResourceTypeCode", net.sovrinhealth.fhir.model.type.code.ResourceTypeCode.class);
        codeSubtypeMap.put("ResourceVersionPolicy", net.sovrinhealth.fhir.model.type.code.ResourceVersionPolicy.class);
        codeSubtypeMap.put("ResponseType", net.sovrinhealth.fhir.model.type.code.ResponseType.class);
        codeSubtypeMap.put("RestfulCapabilityMode", net.sovrinhealth.fhir.model.type.code.RestfulCapabilityMode.class);
        codeSubtypeMap.put("RiskAssessmentStatus", net.sovrinhealth.fhir.model.type.code.RiskAssessmentStatus.class);
        codeSubtypeMap.put("SearchComparator", net.sovrinhealth.fhir.model.type.code.SearchComparator.class);
        codeSubtypeMap.put("SearchEntryMode", net.sovrinhealth.fhir.model.type.code.SearchEntryMode.class);
        codeSubtypeMap.put("SearchModifierCode", net.sovrinhealth.fhir.model.type.code.SearchModifierCode.class);
        codeSubtypeMap.put("SearchParamType", net.sovrinhealth.fhir.model.type.code.SearchParamType.class);
        codeSubtypeMap.put("SectionMode", net.sovrinhealth.fhir.model.type.code.SectionMode.class);
        codeSubtypeMap.put("SequenceType", net.sovrinhealth.fhir.model.type.code.SequenceType.class);
        codeSubtypeMap.put("ServiceRequestIntent", net.sovrinhealth.fhir.model.type.code.ServiceRequestIntent.class);
        codeSubtypeMap.put("ServiceRequestPriority", net.sovrinhealth.fhir.model.type.code.ServiceRequestPriority.class);
        codeSubtypeMap.put("ServiceRequestStatus", net.sovrinhealth.fhir.model.type.code.ServiceRequestStatus.class);
        codeSubtypeMap.put("SlicingRules", net.sovrinhealth.fhir.model.type.code.SlicingRules.class);
        codeSubtypeMap.put("SlotStatus", net.sovrinhealth.fhir.model.type.code.SlotStatus.class);
        codeSubtypeMap.put("SortDirection", net.sovrinhealth.fhir.model.type.code.SortDirection.class);
        codeSubtypeMap.put("SPDXLicense", net.sovrinhealth.fhir.model.type.code.SPDXLicense.class);
        codeSubtypeMap.put("SpecimenContainedPreference", net.sovrinhealth.fhir.model.type.code.SpecimenContainedPreference.class);
        codeSubtypeMap.put("SpecimenStatus", net.sovrinhealth.fhir.model.type.code.SpecimenStatus.class);
        codeSubtypeMap.put("StandardsStatus", net.sovrinhealth.fhir.model.type.code.StandardsStatus.class);
        codeSubtypeMap.put("Status", net.sovrinhealth.fhir.model.type.code.Status.class);
        codeSubtypeMap.put("StrandType", net.sovrinhealth.fhir.model.type.code.StrandType.class);
        codeSubtypeMap.put("StructureDefinitionKind", net.sovrinhealth.fhir.model.type.code.StructureDefinitionKind.class);
        codeSubtypeMap.put("StructureMapContextType", net.sovrinhealth.fhir.model.type.code.StructureMapContextType.class);
        codeSubtypeMap.put("StructureMapGroupTypeMode", net.sovrinhealth.fhir.model.type.code.StructureMapGroupTypeMode.class);
        codeSubtypeMap.put("StructureMapInputMode", net.sovrinhealth.fhir.model.type.code.StructureMapInputMode.class);
        codeSubtypeMap.put("StructureMapModelMode", net.sovrinhealth.fhir.model.type.code.StructureMapModelMode.class);
        codeSubtypeMap.put("StructureMapSourceListMode", net.sovrinhealth.fhir.model.type.code.StructureMapSourceListMode.class);
        codeSubtypeMap.put("StructureMapTargetListMode", net.sovrinhealth.fhir.model.type.code.StructureMapTargetListMode.class);
        codeSubtypeMap.put("StructureMapTransform", net.sovrinhealth.fhir.model.type.code.StructureMapTransform.class);
        codeSubtypeMap.put("SubscriptionChannelType", net.sovrinhealth.fhir.model.type.code.SubscriptionChannelType.class);
        codeSubtypeMap.put("SubscriptionNotificationType", net.sovrinhealth.fhir.model.type.code.SubscriptionNotificationType.class);
        codeSubtypeMap.put("SubscriptionStatusCode", net.sovrinhealth.fhir.model.type.code.SubscriptionStatusCode.class);
        codeSubtypeMap.put("SubscriptionTopicFilterBySearchModifier", net.sovrinhealth.fhir.model.type.code.SubscriptionTopicFilterBySearchModifier.class);
        codeSubtypeMap.put("SupplyDeliveryStatus", net.sovrinhealth.fhir.model.type.code.SupplyDeliveryStatus.class);
        codeSubtypeMap.put("SupplyRequestStatus", net.sovrinhealth.fhir.model.type.code.SupplyRequestStatus.class);
        codeSubtypeMap.put("SystemRestfulInteraction", net.sovrinhealth.fhir.model.type.code.SystemRestfulInteraction.class);
        codeSubtypeMap.put("TaskIntent", net.sovrinhealth.fhir.model.type.code.TaskIntent.class);
        codeSubtypeMap.put("TaskPriority", net.sovrinhealth.fhir.model.type.code.TaskPriority.class);
        codeSubtypeMap.put("TaskStatus", net.sovrinhealth.fhir.model.type.code.TaskStatus.class);
        codeSubtypeMap.put("TestReportActionResult", net.sovrinhealth.fhir.model.type.code.TestReportActionResult.class);
        codeSubtypeMap.put("TestReportParticipantType", net.sovrinhealth.fhir.model.type.code.TestReportParticipantType.class);
        codeSubtypeMap.put("TestReportResult", net.sovrinhealth.fhir.model.type.code.TestReportResult.class);
        codeSubtypeMap.put("TestReportStatus", net.sovrinhealth.fhir.model.type.code.TestReportStatus.class);
        codeSubtypeMap.put("TestScriptRequestMethodCode", net.sovrinhealth.fhir.model.type.code.TestScriptRequestMethodCode.class);
        codeSubtypeMap.put("TriggerType", net.sovrinhealth.fhir.model.type.code.TriggerType.class);
        codeSubtypeMap.put("TypeDerivationRule", net.sovrinhealth.fhir.model.type.code.TypeDerivationRule.class);
        codeSubtypeMap.put("TypeRestfulInteraction", net.sovrinhealth.fhir.model.type.code.TypeRestfulInteraction.class);
        codeSubtypeMap.put("UDIEntryType", net.sovrinhealth.fhir.model.type.code.UDIEntryType.class);
        codeSubtypeMap.put("UnitsOfTime", net.sovrinhealth.fhir.model.type.code.UnitsOfTime.class);
        codeSubtypeMap.put("Use", net.sovrinhealth.fhir.model.type.code.Use.class);
        codeSubtypeMap.put("VariableType", net.sovrinhealth.fhir.model.type.code.VariableType.class);
        codeSubtypeMap.put("VisionBase", net.sovrinhealth.fhir.model.type.code.VisionBase.class);
        codeSubtypeMap.put("VisionEyes", net.sovrinhealth.fhir.model.type.code.VisionEyes.class);
        codeSubtypeMap.put("VisionStatus", net.sovrinhealth.fhir.model.type.code.VisionStatus.class);
        codeSubtypeMap.put("XPathUsageType", net.sovrinhealth.fhir.model.type.code.XPathUsageType.class);
        return Collections.unmodifiableMap(codeSubtypeMap);
    }

    private static Map<String, Class<?>> buildDataTypeMap() {
        Map<String, Class<?>> dataTypeMap = new LinkedHashMap<>();
        for (Class<?> dataType : DATA_TYPES) {
            dataTypeMap.put(getTypeName(dataType), dataType);
        }
        return Collections.unmodifiableMap(dataTypeMap);
    }

    private static Map<Class<?>, Class<?>> buildConcreteTypeMap() {
        Map<Class<?>, Class<?>> concreteTypeMap = new LinkedHashMap<>();
        concreteTypeMap.put(SimpleQuantity.class, Quantity.class);
        concreteTypeMap.put(MoneyQuantity.class, Quantity.class);
        return Collections.unmodifiableMap(concreteTypeMap);
    }

    private static Map<Class<?>, List<Constraint>> buildModelClassConstraintMap() {
        Map<Class<?>, List<Constraint>> modelClassConstraintMap = new LinkedHashMap<>(1024);
        List<ModelConstraintProvider> providers = ConstraintProvider.providers(ModelConstraintProvider.class);
        for (Class<?> modelClass : getModelClasses()) {
            List<Constraint> constraints = new ArrayList<>();
            for (Class<?> clazz : getClosure(modelClass)) {
                for (Constraint constraint : clazz.getDeclaredAnnotationsByType(Constraint.class)) {
                    constraints.add(Constraint.Factory.createConstraint(
                        constraint.id(),
                        constraint.level(),
                        constraint.location(),
                        constraint.description(),
                        constraint.expression(),
                        constraint.source(),
                        constraint.modelChecked(),
                        constraint.generated()));
                }
            }
            for (ModelConstraintProvider provider : providers) {
                if (provider.appliesTo(modelClass)) {
                    for (Predicate<Constraint> removalPredicate : provider.getRemovalPredicates()) {
                        constraints.removeIf(removalPredicate);
                    }
                    constraints.addAll(provider.getConstraints());
                }
            }
            modelClassConstraintMap.put(modelClass, Collections.unmodifiableList(constraints));
        }
        return Collections.unmodifiableMap(modelClassConstraintMap);
    }

    private static Map<Class<?>, Map<String, ElementInfo>> buildModelClassElementInfoMap() {
        Map<Class<?>, Map<String, ElementInfo>> modelClassElementInfoMap = new LinkedHashMap<>(1024);
        for(Class<?> modelClass : MODEL_CLASSES) {
            Map<String, ElementInfo> elementInfoMap = getElementInfoMap(modelClass, modelClassElementInfoMap);
            modelClassElementInfoMap.put(modelClass, Collections.unmodifiableMap(elementInfoMap));
        }
        return Collections.unmodifiableMap(modelClassElementInfoMap);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Class<? extends Resource>> buildResourceTypeMap() {
        Map<String, Class<? extends Resource>> resourceTypeMap = new LinkedHashMap<>(256);
        for (Class<?> modelClass : getModelClasses()) {
            if (isResourceType(modelClass)) {
                resourceTypeMap.put(modelClass.getSimpleName(), (Class<? extends Resource>) modelClass);
            }
        }
        return Collections.unmodifiableMap(resourceTypeMap);
    }

    private static Map<String, ElementInfo> getElementInfoMap(Class<?> modelClass,
            Map<Class<?>, Map<String,ElementInfo>> elementInfoMapCache) {
        Map<String, ElementInfo> elementInfoMap = new LinkedHashMap<>();

        // Loop through this class and its supertypes to collect ElementInfo for all the fields
        for (Class<?> clazz : getClosure(modelClass)) {
            // If we've already created ElementInfo for this class, then use that
            if (elementInfoMapCache.containsKey(clazz)) {
                elementInfoMap.putAll(elementInfoMapCache.get(clazz));
                continue;
            }

            // Else use reflection and model annotations to construct ElementInfo for all fields in this class
            for (Field field : clazz.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isVolatile(modifiers)) {
                    continue;
                }

                String elementName = getElementName(field);
                Class<?> type = getFieldType(field);
                Class<?> declaringType = field.getDeclaringClass();
                boolean required = isRequired(field);
                boolean summary = isSummary(field);
                boolean repeating = isRepeating(field);
                boolean choice = isChoice(field);
                boolean reference = isReference(field);
                Binding binding = field.getAnnotation(Binding.class);
                Set<Class<?>> choiceTypes = choice ? Collections.unmodifiableSet(getChoiceTypes(field)) : Collections.emptySet();
                Set<String> referenceTypes = reference ? Collections.unmodifiableSet(getReferenceTypes(field)) : Collections.emptySet();
                elementInfoMap.put(elementName, new ElementInfo(
                        elementName,
                        type,
                        declaringType,
                        required,
                        repeating,
                        choice,
                        choiceTypes,
                        reference,
                        referenceTypes,
                        binding,
                        summary
                    )
                );
            }
        }
        return elementInfoMap;
    }

    /**
     * @param name
     *            the name of the choice element without any type suffix
     * @param type
     *            the model class which represents the choice value for the choice element
     * @return the serialized name of the choice element {@code name} with choice type {@code type}
     */
    public static String getChoiceElementName(String name, Class<?> type) {
        return name + getConcreteType(type).getSimpleName();
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the choice element without any type suffix
     * @return the set of model classes for the allowed types of the specified choice element
     */
    public static Set<Class<?>> getChoiceElementTypes(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.getChoiceTypes();
        }
        return Collections.emptySet();
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the reference element
     * @return a set of Strings which represent the the allowed target types for the reference
     */
    public static Set<String> getReferenceTargetTypes(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.getReferenceTypes();
        }
        return Collections.emptySet();
    }

    private static Set<Class<?>> getChoiceTypes(Field field) {
        return new LinkedHashSet<>(Arrays.asList(field.getAnnotation(Choice.class).value()));
    }

    private static Set<String> getReferenceTypes(Field field) {
        return new LinkedHashSet<>(Arrays.asList(field.getAnnotation(ReferenceTarget.class).value()));
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @return A list of superclasses ordered from parent to child, including the modelClass itself
     */
    public static List<Class<?>> getClosure(Class<?> modelClass) {
        List<Class<?>> closure = new ArrayList<>();
        while (!Object.class.equals(modelClass)) {
            closure.add(modelClass);
            modelClass = modelClass.getSuperclass();
        }
        Collections.reverse(closure);
        return closure;
    }

    /**
     * @param type
     * @return the class for the concrete type of the passed type if it is a profiled type; otherwise the passed type
     *         itself
     */
    public static Class<?> getConcreteType(Class<?> type) {
        if (isProfiledType(type)) {
            return CONCRETE_TYPE_MAP.get(type);
        }
        return type;
    }

    /**
     * @return the list of constraints for the modelClass or empty if there are none
     */
    public static List<Constraint> getConstraints(Class<?> modelClass) {
        return MODEL_CLASS_CONSTRAINT_MAP.getOrDefault(modelClass, Collections.emptyList());
    }

    /**
     * @return ElementInfo for the element with the passed name on the passed modelClass or null if the modelClass does
     *         not contain an element with this name
     */
    public static ElementInfo getElementInfo(Class<?> modelClass, String elementName) {
        return MODEL_CLASS_ELEMENT_INFO_MAP.getOrDefault(modelClass, Collections.emptyMap()).get(elementName);
    }

    /**
     * @return a collection of ElementInfo for all elements of the passed modelClass or empty if the class is not a FHIR
     *         model class
     */
    public static Collection<ElementInfo> getElementInfo(Class<?> modelClass) {
        return MODEL_CLASS_ELEMENT_INFO_MAP.getOrDefault(modelClass, Collections.emptyMap()).values();
    }

    /**
     * @return ElementInfo for the choice element with the passed typeSpecificElementName of the passed modelClass or
     *         null if the modelClass does not contain a choice element that can have this typeSpecificElementName
     */
    public static ElementInfo getChoiceElementInfo(Class<?> modelClass, String typeSpecificElementName) {
        for (ElementInfo elementInfo : getElementInfo(modelClass)) {
            if (elementInfo.isChoice() && elementInfo.getChoiceElementNames().contains(typeSpecificElementName)) {
                return elementInfo;
            }
        }
        return null;
    }

    /**
     * Get the actual element name from a Java field.
     */
    public static String getElementName(Field field) {
        return getElementName(field.getName());
    }

    /**
     * Get the actual element name from a Java field name.
     * This method reverses any encoding that was required to represent the FHIR element name in Java,
     * such as converting class to clazz.
     */
    public static String getElementName(String fieldName) {
        if ("clazz".equals(fieldName)) {
            return "class";
        }
        if (fieldName.startsWith("_")) {
            return fieldName.substring(1);
        }
        return fieldName;
    }

    /**
     * @return the set of element names for the passed modelClass or empty if it is not a FHIR model class
     * @implSpec choice type element names are returned without a type suffix; see {@link #getChoiceElementName(String,
     *           Class<?>)} for building the serialized name
     */
    public static Set<String> getElementNames(Class<?> modelClass) {
        return MODEL_CLASS_ELEMENT_INFO_MAP.getOrDefault(modelClass, Collections.emptyMap()).keySet();
    }

    /**
     * @return the model class for the element with name elementName on the passed modelClass or
     *         null if the passed modelClass does not have an element {@code elementName}
     */
    public static Class<?> getElementType(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.getType();
        }
        return null;
    }

    /**
     * Get the model class which declares the elementName found on the passed modelClass.
     *
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the element; choice element names do not include a type suffix
     * @return modelClass or a superclass of modelClass, or null if the element is not found on the passed modelClass
     */
    public static Class<?> getElementDeclaringType(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.getDeclaringType();
        }
        return null;
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the element; choice element names do not include a type suffix
     * @param type
     *            the model class to check
     * @return true if the passed modelClass contains an element with name elementName and the passed type is the one
     *         that declares it; otherwise false
     */
    public static boolean isElementDeclaredBy(Class<?> modelClass, String elementName, Class<?> type) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.isDeclaredBy(type);
        }
        return false;
    }

    private static Class<?> getFieldType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        return field.getType();
    }

    /**
     * @return all model classes, including both resources and elements, concrete and abstract
     */
    public static Set<Class<?>> getModelClasses() {
        return MODEL_CLASS_ELEMENT_INFO_MAP.keySet();
    }

    /**
     * @param name
     *            the resource type name in titlecase to match the corresponding model class name
     * @return the model class that corresponds to the passed resource type name
     */
    public static Class<? extends Resource> getResourceType(String name) {
        return RESOURCE_TYPE_MAP.get(name);
    }

    /**
     * @return a collection of FHIR resource type model classes, including abstract supertypes
     */
    public static Collection<Class<? extends Resource>> getResourceTypes() {
        return RESOURCE_TYPE_MAP.values();
    }

    /**
     * @return a collection of FHIR resource type model classes
     */
    public static Collection<Class<? extends Resource>> getResourceTypes(boolean includeAbstractTypes) {
        if (includeAbstractTypes) {
            return RESOURCE_TYPE_MAP.values();
        } else {
            return CONCRETE_RESOURCE_TYPES;
        }
    }

    /**
     * @return the set of classes for the FHIR elements
     */
    public static Set<Class<? extends Element>> getDataTypes() {
        return DATA_TYPES;
    }

    /**
     * @return the name of the FHIR data type which corresponds to the passed type
     * @implNote primitive types will start with a lowercase letter,
     *           complex types and resources with an uppercaseletter
     */
    public static String getTypeName(Class<?> type) {
        String typeName = type.getSimpleName();
        if (Code.class.isAssignableFrom(type)) {
            typeName = "code";
        } else if (isPrimitiveType(type)) {
            typeName = typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
        }
        return typeName;
    }

    /**
     * @return the set of FHIR data type names for the passed modelClass and its supertypes
     * @implNote primitive types will start with a lowercase letter,
     *           complex types and resources with an uppercaseletter
     */
    public static Set<String> getTypeNames(Class<?> modelClass) {
        Set<String> typeNames = new HashSet<>();
        while (!Object.class.equals(modelClass)) {
            typeNames.add(getTypeName(modelClass));
            modelClass = modelClass.getSuperclass();
        }
        return typeNames;
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @return true if and only if {@code modelClass} is a BackboneElement
     */
    public static boolean isBackboneElementType(Class<?> modelClass) {
        return BackboneElement.class.isAssignableFrom(modelClass);
    }

    private static boolean isChoice(Field field) {
        return field.isAnnotationPresent(Choice.class);
    }

    private static boolean isReference(Field field) {
        return field.isAnnotationPresent(net.sovrinhealth.fhir.model.annotation.ReferenceTarget.class);
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the element; choice element names do not include a type suffix
     * @return true if {@code modelClass} contains a choice element with name @{code elementName}; otherwise false
     */
    public static boolean isChoiceElement(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.isChoice();
        }
        return false;
    }

    /**
     * @param type
     *            a model class which represents a FHIR element
     * @return true if {@code type} is an allowed choice element type; otherwise false
     */
    public static boolean isChoiceElementType(Class<?> type) {
        return CHOICE_ELEMENT_TYPES.contains(type);
    }

    /**
     * @param type
     *            a model class which represents a FHIR element
     * @return true if {@code type} is subclass of net.sovrinhealth.fhir.model.type.Code; otherwise false
     */
    public static boolean isCodeSubtype(Class<?> type) {
        return Code.class.isAssignableFrom(type) && !Code.class.equals(type);
    }

    /**
     * @param modelObject
     *            a model object which represents a FHIR resource or element
     * @return true if {@code modelObject} is an element; otherwise false
     */
    public static boolean isElement(Object modelObject) {
        return (modelObject instanceof Element);
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @return true if {@code modelClass} is an element; otherwise false
     */
    public static boolean isElementType(Class<?> modelClass) {
        return Element.class.isAssignableFrom(modelClass);
    }

    /**
     * @param type
     *            a model class which represents a FHIR element
     * @return true if {@code type} is a metadata type; otherwise false
     * @see <a href="https://www.hl7.org/fhir/R4/metadatatypes.html">https://www.hl7.org/fhir/R4/metadatatypes.html</a>
     */
    public static boolean isMetadataType(Class<?> type) {
        return ContactDetail.class.equals(type) ||
                Contributor.class.equals(type) ||
                DataRequirement.class.isAssignableFrom(type) ||
                RelatedArtifact.class.isAssignableFrom(type) ||
                UsageContext.class.equals(type) ||
                ParameterDefinition.class.equals(type) ||
                Expression.class.equals(type) ||
                TriggerDefinition.class.equals(type);
    }

    /**
     * @return true if {@code type} is a model class that represents a FHIR resource or element; otherwise false
     */
    public static boolean isModelClass(Class<?> type) {
        return isResourceType(type) || isElementType(type);
    }

    /**
     * @param type
     *            a model class which represents a FHIR element
     * @return true if {@code type} is a model class that represents a FHIR primitive type; otherwise false
     * @implNote xhtml is considered a primitive type
     * @see <a href="https://www.hl7.org/fhir/R4/datatypes.html#primitive">https://www.hl7.org/fhir/R4/datatypes.html#primitive</a>
     */
    public static boolean isPrimitiveType(Class<?> type) {
        return Base64Binary.class.equals(type) ||
            net.sovrinhealth.fhir.model.type.Boolean.class.equals(type) ||
            net.sovrinhealth.fhir.model.type.String.class.isAssignableFrom(type) ||
            Uri.class.isAssignableFrom(type) ||
            DateTime.class.equals(type) ||
            Date.class.equals(type) ||
            Time.class.equals(type) ||
            Instant.class.equals(type) ||
            net.sovrinhealth.fhir.model.type.Integer.class.isAssignableFrom(type) ||
            Decimal.class.equals(type) ||
            Xhtml.class.equals(type);
    }

    /**
     * @param type
     *            a model class which represents a FHIR element
     * @return true if {@code type} is a profiled data type; otherwise false
     */
    public static boolean isProfiledType(Class<?> type) {
        return CONCRETE_TYPE_MAP.containsKey(type);
    }

    private static boolean isRepeating(Field field) {
        return List.class.equals(field.getType());
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the element; choice element names do not include a type suffix
     * @return true if {@code modelClass} has an element {@code elementName} and it has max cardinality > 1;
     *         otherwise false
     */
    public static boolean isRepeatingElement(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.isRepeating();
        }
        return false;
    }

    private static boolean isRequired(Field field) {
        return field.isAnnotationPresent(Required.class);
    }

    private static boolean isSummary(Field field) {
        return field.isAnnotationPresent(Summary.class);
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the element; choice element names do not include a type suffix
     * @return true if {@code modelClass} has an element {@code elementName} and it has min cardinality > 0;
     *         otherwise false
     */
    public static boolean isRequiredElement(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.isRequired();
        }
        return false;
    }

    /**
     * @param modelObject
     *            a model object which represents a FHIR resource or element
     * @return true if {@code modelObject} represents a FHIR resource; otherwise false
     */
    public static boolean isResource(Object modelObject) {
        return (modelObject instanceof Resource);
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @return true if {@code modelClass} represents a FHIR resource; otherwise false
     */
    public static boolean isResourceType(Class<?> modelClass) {
        return Resource.class.isAssignableFrom(modelClass);
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @return true if {@code modelClass} is an abstract FHIR model class; otherwise false
     */
    public static boolean isAbstract(Class<?> modelClass) {
        return Modifier.isAbstract(modelClass.getModifiers());
    }

    /**
     * @param name
     *            the resource type name in titlecase to match the corresponding model class name
     * @return true if {@code name} is a valid FHIR resource name; otherwise false
     * @implSpec this method returns true for abstract types like {@code Resource} and {@code DomainResource}
     */
    public static boolean isResourceType(String name) {
        return RESOURCE_TYPE_MAP.containsKey(name);
    }

    /**
     * @param name
     *            the resource type name in titlecase to match the corresponding model class name
     * @return true if {@code name} is a valid FHIR resource name; otherwise false
     * @implSpec this method returns false for abstract types like {@code Resource} and {@code DomainResource}
     */
    public static boolean isConcreteResourceType(String name) {
        Class<?> modelClass = RESOURCE_TYPE_MAP.get(name);
        return modelClass != null && !isAbstract(modelClass);
    }

    /**
     * @param modelClass
     *            a model class which represents a FHIR resource or element
     * @param elementName
     *            the name of the element; choice element names do not include a type suffix
     * @return true if {@code modelClass} has an element {@code elementName} and its marked as a summary element;
     *         otherwise false
     */
    public static boolean isSummaryElement(Class<?> modelClass, String elementName) {
        ElementInfo elementInfo = getElementInfo(modelClass, elementName);
        if (elementInfo != null) {
            return elementInfo.isSummary();
        }
        return false;
    }

    /**
     * @return a copy of the passed ZonedDateTime with the time truncated to {@code unit}
     */
    public static ZonedDateTime truncateTime(ZonedDateTime dateTime, ChronoUnit unit) {
        return dateTime == null ? null : dateTime.truncatedTo(unit);
    }

    /**
     * @return a copy of the passed LocalTime with the time truncated to {@code unit}
     */
    public static LocalTime truncateTime(LocalTime time, ChronoUnit unit) {
        return time == null ? null : time.truncatedTo(unit);
    }

    /**
     * @return a copy of the passed TemporalAccessor with the time truncated to {@code unit}
     */
    public static TemporalAccessor truncateTime(TemporalAccessor ta, ChronoUnit unit) {
        if (ta instanceof java.time.Instant) {
            ta = ((java.time.Instant) ta).truncatedTo(unit);
        } else if (ta instanceof ZonedDateTime) {
            ta = ((ZonedDateTime) ta).truncatedTo(unit);
        } else if (ta instanceof LocalDateTime) {
            ta = ((LocalDateTime) ta).truncatedTo(unit);
        } else if (ta instanceof LocalTime) {
            ta = ((LocalTime) ta).truncatedTo(unit);
        } else if (ta instanceof OffsetTime) {
            ta = ((OffsetTime) ta).truncatedTo(unit);
        } else if (ta instanceof OffsetDateTime) {
            ta = ((OffsetDateTime) ta).truncatedTo(unit);
        }

        return ta;
    }

    /**
     * @return true if {@code identifier} is a reserved keyword in FHIRPath version N1
     * @see <a href="http://hl7.org/fhirpath/2018Sep/index.html#keywords">http://hl7.org/fhirpath/2018Sep/index.html#keywords</a>
     */
    public static boolean isKeyword(String identifier) {
        return KEYWORDS.contains(identifier);
    }

    /**
     * Wraps the passed string identifier for use in FHIRPath
     * @see <a href="http://hl7.org/fhirpath/2018Sep/index.html#keywords">http://hl7.org/fhirpath/2018Sep/index.html#keywords</a>
     */
    public static String delimit(String identifier) {
        return String.format("`%s`", identifier);
    }

    /**
     * @return the implicit system for {@code code} if present, otherwise null
     */
    public static String getSystem(Code code) {
        if (code != null && code.getClass().isAnnotationPresent(net.sovrinhealth.fhir.model.annotation.System.class)) {
            return code.getClass().getAnnotation(net.sovrinhealth.fhir.model.annotation.System.class).value();
        }
        return null;
    }

    /**
     * @return the data type class associated with {@code typeName} parameter if exists, otherwise null
     */
    public static Class<?> getDataType(String typeName) {
        return DATA_TYPE_MAP.get(typeName);
    }

    public static boolean isCodeSubtype(String name) {
        return CODE_SUBTYPE_MAP.containsKey(name);
    }

    public static Collection<Class<?>> getCodeSubtypes() {
        return CODE_SUBTYPE_MAP.values();
    }
}
