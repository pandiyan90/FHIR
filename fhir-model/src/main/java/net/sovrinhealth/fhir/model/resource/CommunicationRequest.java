/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Generated;

import net.sovrinhealth.fhir.model.annotation.Binding;
import net.sovrinhealth.fhir.model.annotation.Choice;
import net.sovrinhealth.fhir.model.annotation.Maturity;
import net.sovrinhealth.fhir.model.annotation.ReferenceTarget;
import net.sovrinhealth.fhir.model.annotation.Required;
import net.sovrinhealth.fhir.model.annotation.Summary;
import net.sovrinhealth.fhir.model.type.Annotation;
import net.sovrinhealth.fhir.model.type.Attachment;
import net.sovrinhealth.fhir.model.type.BackboneElement;
import net.sovrinhealth.fhir.model.type.Boolean;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.Identifier;
import net.sovrinhealth.fhir.model.type.Meta;
import net.sovrinhealth.fhir.model.type.Narrative;
import net.sovrinhealth.fhir.model.type.Period;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.String;
import net.sovrinhealth.fhir.model.type.Uri;
import net.sovrinhealth.fhir.model.type.code.BindingStrength;
import net.sovrinhealth.fhir.model.type.code.CommunicationPriority;
import net.sovrinhealth.fhir.model.type.code.CommunicationRequestStatus;
import net.sovrinhealth.fhir.model.type.code.StandardsStatus;
import net.sovrinhealth.fhir.model.util.ValidationSupport;
import net.sovrinhealth.fhir.model.visitor.Visitor;

/**
 * A request to convey information; e.g. the CDS system proposes that an alert be sent to a responsible provider, the CDS 
 * system proposes that the public health agency be notified about a reportable condition.
 * 
 * <p>Maturity level: FMM2 (Trial Use)
 */
@Maturity(
    level = 2,
    status = StandardsStatus.Value.TRIAL_USE
)
@Generated("net.sovrinhealth.fhir.tools.CodeGenerator")
public class CommunicationRequest extends DomainResource {
    @Summary
    private final List<Identifier> identifier;
    @Summary
    private final List<Reference> basedOn;
    @Summary
    @ReferenceTarget({ "CommunicationRequest" })
    private final List<Reference> replaces;
    @Summary
    private final Identifier groupIdentifier;
    @Summary
    @Binding(
        bindingName = "CommunicationRequestStatus",
        strength = BindingStrength.Value.REQUIRED,
        description = "The status of the communication request.",
        valueSet = "http://hl7.org/fhir/ValueSet/request-status|4.3.0"
    )
    @Required
    private final CommunicationRequestStatus status;
    @Binding(
        bindingName = "CommunicationRequestStatusReason",
        strength = BindingStrength.Value.EXAMPLE,
        description = "Codes identifying the reason for the current state of a request."
    )
    private final CodeableConcept statusReason;
    @Binding(
        bindingName = "CommunicationCategory",
        strength = BindingStrength.Value.EXAMPLE,
        description = "Codes for general categories of communications such as alerts, instruction, etc.",
        valueSet = "http://hl7.org/fhir/ValueSet/communication-category"
    )
    private final List<CodeableConcept> category;
    @Summary
    @Binding(
        bindingName = "CommunicationPriority",
        strength = BindingStrength.Value.REQUIRED,
        description = "Codes indicating the relative importance of a communication request.",
        valueSet = "http://hl7.org/fhir/ValueSet/request-priority|4.3.0"
    )
    private final CommunicationPriority priority;
    @Summary
    private final Boolean doNotPerform;
    @Binding(
        bindingName = "CommunicationMedium",
        strength = BindingStrength.Value.EXAMPLE,
        description = "Codes for communication mediums such as phone, fax, email, in person, etc.",
        valueSet = "http://terminology.hl7.org/ValueSet/v3-ParticipationMode"
    )
    private final List<CodeableConcept> medium;
    @ReferenceTarget({ "Patient", "Group" })
    private final Reference subject;
    private final List<Reference> about;
    @Summary
    @ReferenceTarget({ "Encounter" })
    private final Reference encounter;
    private final List<Payload> payload;
    @Summary
    @Choice({ DateTime.class, Period.class })
    private final Element occurrence;
    @Summary
    private final DateTime authoredOn;
    @Summary
    @ReferenceTarget({ "Practitioner", "PractitionerRole", "Organization", "Patient", "RelatedPerson", "Device" })
    private final Reference requester;
    @ReferenceTarget({ "Device", "Organization", "Patient", "Practitioner", "PractitionerRole", "RelatedPerson", "Group", "CareTeam", "HealthcareService" })
    private final List<Reference> recipient;
    @Summary
    @ReferenceTarget({ "Device", "Organization", "Patient", "Practitioner", "PractitionerRole", "RelatedPerson", "HealthcareService" })
    private final Reference sender;
    @Summary
    @Binding(
        bindingName = "CommunicationReason",
        strength = BindingStrength.Value.EXAMPLE,
        description = "Codes for describing reasons for the occurrence of a communication.",
        valueSet = "http://terminology.hl7.org/ValueSet/v3-ActReason"
    )
    private final List<CodeableConcept> reasonCode;
    @Summary
    @ReferenceTarget({ "Condition", "Observation", "DiagnosticReport", "DocumentReference" })
    private final List<Reference> reasonReference;
    private final List<Annotation> note;

    private CommunicationRequest(Builder builder) {
        super(builder);
        identifier = Collections.unmodifiableList(builder.identifier);
        basedOn = Collections.unmodifiableList(builder.basedOn);
        replaces = Collections.unmodifiableList(builder.replaces);
        groupIdentifier = builder.groupIdentifier;
        status = builder.status;
        statusReason = builder.statusReason;
        category = Collections.unmodifiableList(builder.category);
        priority = builder.priority;
        doNotPerform = builder.doNotPerform;
        medium = Collections.unmodifiableList(builder.medium);
        subject = builder.subject;
        about = Collections.unmodifiableList(builder.about);
        encounter = builder.encounter;
        payload = Collections.unmodifiableList(builder.payload);
        occurrence = builder.occurrence;
        authoredOn = builder.authoredOn;
        requester = builder.requester;
        recipient = Collections.unmodifiableList(builder.recipient);
        sender = builder.sender;
        reasonCode = Collections.unmodifiableList(builder.reasonCode);
        reasonReference = Collections.unmodifiableList(builder.reasonReference);
        note = Collections.unmodifiableList(builder.note);
    }

    /**
     * Business identifiers assigned to this communication request by the performer or other systems which remain constant as 
     * the resource is updated and propagates from server to server.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Identifier} that may be empty.
     */
    public List<Identifier> getIdentifier() {
        return identifier;
    }

    /**
     * A plan or proposal that is fulfilled in whole or in part by this request.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Reference} that may be empty.
     */
    public List<Reference> getBasedOn() {
        return basedOn;
    }

    /**
     * Completed or terminated request(s) whose function is taken by this new request.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Reference} that may be empty.
     */
    public List<Reference> getReplaces() {
        return replaces;
    }

    /**
     * A shared identifier common to all requests that were authorized more or less simultaneously by a single author, 
     * representing the identifier of the requisition, prescription or similar form.
     * 
     * @return
     *     An immutable object of type {@link Identifier} that may be null.
     */
    public Identifier getGroupIdentifier() {
        return groupIdentifier;
    }

    /**
     * The status of the proposal or order.
     * 
     * @return
     *     An immutable object of type {@link CommunicationRequestStatus} that is non-null.
     */
    public CommunicationRequestStatus getStatus() {
        return status;
    }

    /**
     * Captures the reason for the current state of the CommunicationRequest.
     * 
     * @return
     *     An immutable object of type {@link CodeableConcept} that may be null.
     */
    public CodeableConcept getStatusReason() {
        return statusReason;
    }

    /**
     * The type of message to be sent such as alert, notification, reminder, instruction, etc.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link CodeableConcept} that may be empty.
     */
    public List<CodeableConcept> getCategory() {
        return category;
    }

    /**
     * Characterizes how quickly the proposed act must be initiated. Includes concepts such as stat, urgent, routine.
     * 
     * @return
     *     An immutable object of type {@link CommunicationPriority} that may be null.
     */
    public CommunicationPriority getPriority() {
        return priority;
    }

    /**
     * If true indicates that the CommunicationRequest is asking for the specified action to *not* occur.
     * 
     * @return
     *     An immutable object of type {@link Boolean} that may be null.
     */
    public Boolean getDoNotPerform() {
        return doNotPerform;
    }

    /**
     * A channel that was used for this communication (e.g. email, fax).
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link CodeableConcept} that may be empty.
     */
    public List<CodeableConcept> getMedium() {
        return medium;
    }

    /**
     * The patient or group that is the focus of this communication request.
     * 
     * @return
     *     An immutable object of type {@link Reference} that may be null.
     */
    public Reference getSubject() {
        return subject;
    }

    /**
     * Other resources that pertain to this communication request and to which this communication request should be 
     * associated.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Reference} that may be empty.
     */
    public List<Reference> getAbout() {
        return about;
    }

    /**
     * The Encounter during which this CommunicationRequest was created or to which the creation of this record is tightly 
     * associated.
     * 
     * @return
     *     An immutable object of type {@link Reference} that may be null.
     */
    public Reference getEncounter() {
        return encounter;
    }

    /**
     * Text, attachment(s), or resource(s) to be communicated to the recipient.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Payload} that may be empty.
     */
    public List<Payload> getPayload() {
        return payload;
    }

    /**
     * The time when this communication is to occur.
     * 
     * @return
     *     An immutable object of type {@link DateTime} or {@link Period} that may be null.
     */
    public Element getOccurrence() {
        return occurrence;
    }

    /**
     * For draft requests, indicates the date of initial creation. For requests with other statuses, indicates the date of 
     * activation.
     * 
     * @return
     *     An immutable object of type {@link DateTime} that may be null.
     */
    public DateTime getAuthoredOn() {
        return authoredOn;
    }

    /**
     * The device, individual, or organization who initiated the request and has responsibility for its activation.
     * 
     * @return
     *     An immutable object of type {@link Reference} that may be null.
     */
    public Reference getRequester() {
        return requester;
    }

    /**
     * The entity (e.g. person, organization, clinical information system, device, group, or care team) which is the intended 
     * target of the communication.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Reference} that may be empty.
     */
    public List<Reference> getRecipient() {
        return recipient;
    }

    /**
     * The entity (e.g. person, organization, clinical information system, or device) which is to be the source of the 
     * communication.
     * 
     * @return
     *     An immutable object of type {@link Reference} that may be null.
     */
    public Reference getSender() {
        return sender;
    }

    /**
     * Describes why the request is being made in coded or textual form.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link CodeableConcept} that may be empty.
     */
    public List<CodeableConcept> getReasonCode() {
        return reasonCode;
    }

    /**
     * Indicates another resource whose existence justifies this request.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Reference} that may be empty.
     */
    public List<Reference> getReasonReference() {
        return reasonReference;
    }

    /**
     * Comments made about the request by the requester, sender, recipient, subject or other participants.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Annotation} that may be empty.
     */
    public List<Annotation> getNote() {
        return note;
    }

    @Override
    public boolean hasChildren() {
        return super.hasChildren() || 
            !identifier.isEmpty() || 
            !basedOn.isEmpty() || 
            !replaces.isEmpty() || 
            (groupIdentifier != null) || 
            (status != null) || 
            (statusReason != null) || 
            !category.isEmpty() || 
            (priority != null) || 
            (doNotPerform != null) || 
            !medium.isEmpty() || 
            (subject != null) || 
            !about.isEmpty() || 
            (encounter != null) || 
            !payload.isEmpty() || 
            (occurrence != null) || 
            (authoredOn != null) || 
            (requester != null) || 
            !recipient.isEmpty() || 
            (sender != null) || 
            !reasonCode.isEmpty() || 
            !reasonReference.isEmpty() || 
            !note.isEmpty();
    }

    @Override
    public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
        if (visitor.preVisit(this)) {
            visitor.visitStart(elementName, elementIndex, this);
            if (visitor.visit(elementName, elementIndex, this)) {
                // visit children
                accept(id, "id", visitor);
                accept(meta, "meta", visitor);
                accept(implicitRules, "implicitRules", visitor);
                accept(language, "language", visitor);
                accept(text, "text", visitor);
                accept(contained, "contained", visitor, Resource.class);
                accept(extension, "extension", visitor, Extension.class);
                accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                accept(identifier, "identifier", visitor, Identifier.class);
                accept(basedOn, "basedOn", visitor, Reference.class);
                accept(replaces, "replaces", visitor, Reference.class);
                accept(groupIdentifier, "groupIdentifier", visitor);
                accept(status, "status", visitor);
                accept(statusReason, "statusReason", visitor);
                accept(category, "category", visitor, CodeableConcept.class);
                accept(priority, "priority", visitor);
                accept(doNotPerform, "doNotPerform", visitor);
                accept(medium, "medium", visitor, CodeableConcept.class);
                accept(subject, "subject", visitor);
                accept(about, "about", visitor, Reference.class);
                accept(encounter, "encounter", visitor);
                accept(payload, "payload", visitor, Payload.class);
                accept(occurrence, "occurrence", visitor);
                accept(authoredOn, "authoredOn", visitor);
                accept(requester, "requester", visitor);
                accept(recipient, "recipient", visitor, Reference.class);
                accept(sender, "sender", visitor);
                accept(reasonCode, "reasonCode", visitor, CodeableConcept.class);
                accept(reasonReference, "reasonReference", visitor, Reference.class);
                accept(note, "note", visitor, Annotation.class);
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
        CommunicationRequest other = (CommunicationRequest) obj;
        return Objects.equals(id, other.id) && 
            Objects.equals(meta, other.meta) && 
            Objects.equals(implicitRules, other.implicitRules) && 
            Objects.equals(language, other.language) && 
            Objects.equals(text, other.text) && 
            Objects.equals(contained, other.contained) && 
            Objects.equals(extension, other.extension) && 
            Objects.equals(modifierExtension, other.modifierExtension) && 
            Objects.equals(identifier, other.identifier) && 
            Objects.equals(basedOn, other.basedOn) && 
            Objects.equals(replaces, other.replaces) && 
            Objects.equals(groupIdentifier, other.groupIdentifier) && 
            Objects.equals(status, other.status) && 
            Objects.equals(statusReason, other.statusReason) && 
            Objects.equals(category, other.category) && 
            Objects.equals(priority, other.priority) && 
            Objects.equals(doNotPerform, other.doNotPerform) && 
            Objects.equals(medium, other.medium) && 
            Objects.equals(subject, other.subject) && 
            Objects.equals(about, other.about) && 
            Objects.equals(encounter, other.encounter) && 
            Objects.equals(payload, other.payload) && 
            Objects.equals(occurrence, other.occurrence) && 
            Objects.equals(authoredOn, other.authoredOn) && 
            Objects.equals(requester, other.requester) && 
            Objects.equals(recipient, other.recipient) && 
            Objects.equals(sender, other.sender) && 
            Objects.equals(reasonCode, other.reasonCode) && 
            Objects.equals(reasonReference, other.reasonReference) && 
            Objects.equals(note, other.note);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, 
                meta, 
                implicitRules, 
                language, 
                text, 
                contained, 
                extension, 
                modifierExtension, 
                identifier, 
                basedOn, 
                replaces, 
                groupIdentifier, 
                status, 
                statusReason, 
                category, 
                priority, 
                doNotPerform, 
                medium, 
                subject, 
                about, 
                encounter, 
                payload, 
                occurrence, 
                authoredOn, 
                requester, 
                recipient, 
                sender, 
                reasonCode, 
                reasonReference, 
                note);
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

    public static class Builder extends DomainResource.Builder {
        private List<Identifier> identifier = new ArrayList<>();
        private List<Reference> basedOn = new ArrayList<>();
        private List<Reference> replaces = new ArrayList<>();
        private Identifier groupIdentifier;
        private CommunicationRequestStatus status;
        private CodeableConcept statusReason;
        private List<CodeableConcept> category = new ArrayList<>();
        private CommunicationPriority priority;
        private Boolean doNotPerform;
        private List<CodeableConcept> medium = new ArrayList<>();
        private Reference subject;
        private List<Reference> about = new ArrayList<>();
        private Reference encounter;
        private List<Payload> payload = new ArrayList<>();
        private Element occurrence;
        private DateTime authoredOn;
        private Reference requester;
        private List<Reference> recipient = new ArrayList<>();
        private Reference sender;
        private List<CodeableConcept> reasonCode = new ArrayList<>();
        private List<Reference> reasonReference = new ArrayList<>();
        private List<Annotation> note = new ArrayList<>();

        private Builder() {
            super();
        }

        /**
         * The logical id of the resource, as used in the URL for the resource. Once assigned, this value never changes.
         * 
         * @param id
         *     Logical id of this artifact
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        /**
         * The metadata about the resource. This is content that is maintained by the infrastructure. Changes to the content 
         * might not always be associated with version changes to the resource.
         * 
         * @param meta
         *     Metadata about the resource
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder meta(Meta meta) {
            return (Builder) super.meta(meta);
        }

        /**
         * A reference to a set of rules that were followed when the resource was constructed, and which must be understood when 
         * processing the content. Often, this is a reference to an implementation guide that defines the special rules along 
         * with other profiles etc.
         * 
         * @param implicitRules
         *     A set of rules under which this content was created
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder implicitRules(Uri implicitRules) {
            return (Builder) super.implicitRules(implicitRules);
        }

        /**
         * The base language in which the resource is written.
         * 
         * @param language
         *     Language of the resource content
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder language(Code language) {
            return (Builder) super.language(language);
        }

        /**
         * A human-readable narrative that contains a summary of the resource and can be used to represent the content of the 
         * resource to a human. The narrative need not encode all the structured data, but is required to contain sufficient 
         * detail to make it "clinically safe" for a human to just read the narrative. Resource definitions may define what 
         * content should be represented in the narrative to ensure clinical safety.
         * 
         * @param text
         *     Text summary of the resource, for human interpretation
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder text(Narrative text) {
            return (Builder) super.text(text);
        }

        /**
         * These resources do not have an independent existence apart from the resource that contains them - they cannot be 
         * identified independently, and nor can they have their own independent transaction scope.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param contained
         *     Contained, inline Resources
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder contained(Resource... contained) {
            return (Builder) super.contained(contained);
        }

        /**
         * These resources do not have an independent existence apart from the resource that contains them - they cannot be 
         * identified independently, and nor can they have their own independent transaction scope.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param contained
         *     Contained, inline Resources
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        @Override
        public Builder contained(Collection<Resource> contained) {
            return (Builder) super.contained(contained);
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
         * May be used to represent additional information that is not part of the basic definition of the resource and that 
         * modifies the understanding of the element that contains it and/or the understanding of the containing element's 
         * descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe and 
         * manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
         * implementer is allowed to define an extension, there is a set of requirements that SHALL be met as part of the 
         * definition of the extension. Applications processing a resource are required to check for modifier extensions.
         * 
         * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
         * change the meaning of modifierExtension itself).
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param modifierExtension
         *     Extensions that cannot be ignored
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder modifierExtension(Extension... modifierExtension) {
            return (Builder) super.modifierExtension(modifierExtension);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource and that 
         * modifies the understanding of the element that contains it and/or the understanding of the containing element's 
         * descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe and 
         * manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
         * implementer is allowed to define an extension, there is a set of requirements that SHALL be met as part of the 
         * definition of the extension. Applications processing a resource are required to check for modifier extensions.
         * 
         * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
         * change the meaning of modifierExtension itself).
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param modifierExtension
         *     Extensions that cannot be ignored
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        @Override
        public Builder modifierExtension(Collection<Extension> modifierExtension) {
            return (Builder) super.modifierExtension(modifierExtension);
        }

        /**
         * Business identifiers assigned to this communication request by the performer or other systems which remain constant as 
         * the resource is updated and propagates from server to server.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param identifier
         *     Unique identifier
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder identifier(Identifier... identifier) {
            for (Identifier value : identifier) {
                this.identifier.add(value);
            }
            return this;
        }

        /**
         * Business identifiers assigned to this communication request by the performer or other systems which remain constant as 
         * the resource is updated and propagates from server to server.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param identifier
         *     Unique identifier
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder identifier(Collection<Identifier> identifier) {
            this.identifier = new ArrayList<>(identifier);
            return this;
        }

        /**
         * A plan or proposal that is fulfilled in whole or in part by this request.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param basedOn
         *     Fulfills plan or proposal
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder basedOn(Reference... basedOn) {
            for (Reference value : basedOn) {
                this.basedOn.add(value);
            }
            return this;
        }

        /**
         * A plan or proposal that is fulfilled in whole or in part by this request.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param basedOn
         *     Fulfills plan or proposal
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder basedOn(Collection<Reference> basedOn) {
            this.basedOn = new ArrayList<>(basedOn);
            return this;
        }

        /**
         * Completed or terminated request(s) whose function is taken by this new request.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * <p>Allowed resource types for the references:
         * <ul>
         * <li>{@link CommunicationRequest}</li>
         * </ul>
         * 
         * @param replaces
         *     Request(s) replaced by this request
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder replaces(Reference... replaces) {
            for (Reference value : replaces) {
                this.replaces.add(value);
            }
            return this;
        }

        /**
         * Completed or terminated request(s) whose function is taken by this new request.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * <p>Allowed resource types for the references:
         * <ul>
         * <li>{@link CommunicationRequest}</li>
         * </ul>
         * 
         * @param replaces
         *     Request(s) replaced by this request
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder replaces(Collection<Reference> replaces) {
            this.replaces = new ArrayList<>(replaces);
            return this;
        }

        /**
         * A shared identifier common to all requests that were authorized more or less simultaneously by a single author, 
         * representing the identifier of the requisition, prescription or similar form.
         * 
         * @param groupIdentifier
         *     Composite request this is part of
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder groupIdentifier(Identifier groupIdentifier) {
            this.groupIdentifier = groupIdentifier;
            return this;
        }

        /**
         * The status of the proposal or order.
         * 
         * <p>This element is required.
         * 
         * @param status
         *     draft | active | on-hold | revoked | completed | entered-in-error | unknown
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder status(CommunicationRequestStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Captures the reason for the current state of the CommunicationRequest.
         * 
         * @param statusReason
         *     Reason for current status
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder statusReason(CodeableConcept statusReason) {
            this.statusReason = statusReason;
            return this;
        }

        /**
         * The type of message to be sent such as alert, notification, reminder, instruction, etc.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param category
         *     Message category
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder category(CodeableConcept... category) {
            for (CodeableConcept value : category) {
                this.category.add(value);
            }
            return this;
        }

        /**
         * The type of message to be sent such as alert, notification, reminder, instruction, etc.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param category
         *     Message category
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder category(Collection<CodeableConcept> category) {
            this.category = new ArrayList<>(category);
            return this;
        }

        /**
         * Characterizes how quickly the proposed act must be initiated. Includes concepts such as stat, urgent, routine.
         * 
         * @param priority
         *     routine | urgent | asap | stat
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder priority(CommunicationPriority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Convenience method for setting {@code doNotPerform}.
         * 
         * @param doNotPerform
         *     True if request is prohibiting action
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @see #doNotPerform(net.sovrinhealth.fhir.model.type.Boolean)
         */
        public Builder doNotPerform(java.lang.Boolean doNotPerform) {
            this.doNotPerform = (doNotPerform == null) ? null : Boolean.of(doNotPerform);
            return this;
        }

        /**
         * If true indicates that the CommunicationRequest is asking for the specified action to *not* occur.
         * 
         * @param doNotPerform
         *     True if request is prohibiting action
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder doNotPerform(Boolean doNotPerform) {
            this.doNotPerform = doNotPerform;
            return this;
        }

        /**
         * A channel that was used for this communication (e.g. email, fax).
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param medium
         *     A channel of communication
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder medium(CodeableConcept... medium) {
            for (CodeableConcept value : medium) {
                this.medium.add(value);
            }
            return this;
        }

        /**
         * A channel that was used for this communication (e.g. email, fax).
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param medium
         *     A channel of communication
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder medium(Collection<CodeableConcept> medium) {
            this.medium = new ArrayList<>(medium);
            return this;
        }

        /**
         * The patient or group that is the focus of this communication request.
         * 
         * <p>Allowed resource types for this reference:
         * <ul>
         * <li>{@link Patient}</li>
         * <li>{@link Group}</li>
         * </ul>
         * 
         * @param subject
         *     Focus of message
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder subject(Reference subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Other resources that pertain to this communication request and to which this communication request should be 
         * associated.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param about
         *     Resources that pertain to this communication request
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder about(Reference... about) {
            for (Reference value : about) {
                this.about.add(value);
            }
            return this;
        }

        /**
         * Other resources that pertain to this communication request and to which this communication request should be 
         * associated.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param about
         *     Resources that pertain to this communication request
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder about(Collection<Reference> about) {
            this.about = new ArrayList<>(about);
            return this;
        }

        /**
         * The Encounter during which this CommunicationRequest was created or to which the creation of this record is tightly 
         * associated.
         * 
         * <p>Allowed resource types for this reference:
         * <ul>
         * <li>{@link Encounter}</li>
         * </ul>
         * 
         * @param encounter
         *     Encounter created as part of
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder encounter(Reference encounter) {
            this.encounter = encounter;
            return this;
        }

        /**
         * Text, attachment(s), or resource(s) to be communicated to the recipient.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param payload
         *     Message payload
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder payload(Payload... payload) {
            for (Payload value : payload) {
                this.payload.add(value);
            }
            return this;
        }

        /**
         * Text, attachment(s), or resource(s) to be communicated to the recipient.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param payload
         *     Message payload
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder payload(Collection<Payload> payload) {
            this.payload = new ArrayList<>(payload);
            return this;
        }

        /**
         * The time when this communication is to occur.
         * 
         * <p>This is a choice element with the following allowed types:
         * <ul>
         * <li>{@link DateTime}</li>
         * <li>{@link Period}</li>
         * </ul>
         * 
         * @param occurrence
         *     When scheduled
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder occurrence(Element occurrence) {
            this.occurrence = occurrence;
            return this;
        }

        /**
         * For draft requests, indicates the date of initial creation. For requests with other statuses, indicates the date of 
         * activation.
         * 
         * @param authoredOn
         *     When request transitioned to being actionable
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder authoredOn(DateTime authoredOn) {
            this.authoredOn = authoredOn;
            return this;
        }

        /**
         * The device, individual, or organization who initiated the request and has responsibility for its activation.
         * 
         * <p>Allowed resource types for this reference:
         * <ul>
         * <li>{@link Practitioner}</li>
         * <li>{@link PractitionerRole}</li>
         * <li>{@link Organization}</li>
         * <li>{@link Patient}</li>
         * <li>{@link RelatedPerson}</li>
         * <li>{@link Device}</li>
         * </ul>
         * 
         * @param requester
         *     Who/what is requesting service
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder requester(Reference requester) {
            this.requester = requester;
            return this;
        }

        /**
         * The entity (e.g. person, organization, clinical information system, device, group, or care team) which is the intended 
         * target of the communication.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * <p>Allowed resource types for the references:
         * <ul>
         * <li>{@link Device}</li>
         * <li>{@link Organization}</li>
         * <li>{@link Patient}</li>
         * <li>{@link Practitioner}</li>
         * <li>{@link PractitionerRole}</li>
         * <li>{@link RelatedPerson}</li>
         * <li>{@link Group}</li>
         * <li>{@link CareTeam}</li>
         * <li>{@link HealthcareService}</li>
         * </ul>
         * 
         * @param recipient
         *     Message recipient
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder recipient(Reference... recipient) {
            for (Reference value : recipient) {
                this.recipient.add(value);
            }
            return this;
        }

        /**
         * The entity (e.g. person, organization, clinical information system, device, group, or care team) which is the intended 
         * target of the communication.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * <p>Allowed resource types for the references:
         * <ul>
         * <li>{@link Device}</li>
         * <li>{@link Organization}</li>
         * <li>{@link Patient}</li>
         * <li>{@link Practitioner}</li>
         * <li>{@link PractitionerRole}</li>
         * <li>{@link RelatedPerson}</li>
         * <li>{@link Group}</li>
         * <li>{@link CareTeam}</li>
         * <li>{@link HealthcareService}</li>
         * </ul>
         * 
         * @param recipient
         *     Message recipient
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder recipient(Collection<Reference> recipient) {
            this.recipient = new ArrayList<>(recipient);
            return this;
        }

        /**
         * The entity (e.g. person, organization, clinical information system, or device) which is to be the source of the 
         * communication.
         * 
         * <p>Allowed resource types for this reference:
         * <ul>
         * <li>{@link Device}</li>
         * <li>{@link Organization}</li>
         * <li>{@link Patient}</li>
         * <li>{@link Practitioner}</li>
         * <li>{@link PractitionerRole}</li>
         * <li>{@link RelatedPerson}</li>
         * <li>{@link HealthcareService}</li>
         * </ul>
         * 
         * @param sender
         *     Message sender
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder sender(Reference sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Describes why the request is being made in coded or textual form.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param reasonCode
         *     Why is communication needed?
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reasonCode(CodeableConcept... reasonCode) {
            for (CodeableConcept value : reasonCode) {
                this.reasonCode.add(value);
            }
            return this;
        }

        /**
         * Describes why the request is being made in coded or textual form.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param reasonCode
         *     Why is communication needed?
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder reasonCode(Collection<CodeableConcept> reasonCode) {
            this.reasonCode = new ArrayList<>(reasonCode);
            return this;
        }

        /**
         * Indicates another resource whose existence justifies this request.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * <p>Allowed resource types for the references:
         * <ul>
         * <li>{@link Condition}</li>
         * <li>{@link Observation}</li>
         * <li>{@link DiagnosticReport}</li>
         * <li>{@link DocumentReference}</li>
         * </ul>
         * 
         * @param reasonReference
         *     Why is communication needed?
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reasonReference(Reference... reasonReference) {
            for (Reference value : reasonReference) {
                this.reasonReference.add(value);
            }
            return this;
        }

        /**
         * Indicates another resource whose existence justifies this request.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * <p>Allowed resource types for the references:
         * <ul>
         * <li>{@link Condition}</li>
         * <li>{@link Observation}</li>
         * <li>{@link DiagnosticReport}</li>
         * <li>{@link DocumentReference}</li>
         * </ul>
         * 
         * @param reasonReference
         *     Why is communication needed?
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder reasonReference(Collection<Reference> reasonReference) {
            this.reasonReference = new ArrayList<>(reasonReference);
            return this;
        }

        /**
         * Comments made about the request by the requester, sender, recipient, subject or other participants.
         * 
         * <p>Adds new element(s) to the existing list.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param note
         *     Comments made about communication request
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder note(Annotation... note) {
            for (Annotation value : note) {
                this.note.add(value);
            }
            return this;
        }

        /**
         * Comments made about the request by the requester, sender, recipient, subject or other participants.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection.
         * If any of the elements are null, calling {@link #build()} will fail.
         * 
         * @param note
         *     Comments made about communication request
         * 
         * @return
         *     A reference to this Builder instance
         * 
         * @throws NullPointerException
         *     If the passed collection is null
         */
        public Builder note(Collection<Annotation> note) {
            this.note = new ArrayList<>(note);
            return this;
        }

        /**
         * Build the {@link CommunicationRequest}
         * 
         * <p>Required elements:
         * <ul>
         * <li>status</li>
         * </ul>
         * 
         * @return
         *     An immutable object of type {@link CommunicationRequest}
         * @throws IllegalStateException
         *     if the current state cannot be built into a valid CommunicationRequest per the base specification
         */
        @Override
        public CommunicationRequest build() {
            CommunicationRequest communicationRequest = new CommunicationRequest(this);
            if (validating) {
                validate(communicationRequest);
            }
            return communicationRequest;
        }

        protected void validate(CommunicationRequest communicationRequest) {
            super.validate(communicationRequest);
            ValidationSupport.checkList(communicationRequest.identifier, "identifier", Identifier.class);
            ValidationSupport.checkList(communicationRequest.basedOn, "basedOn", Reference.class);
            ValidationSupport.checkList(communicationRequest.replaces, "replaces", Reference.class);
            ValidationSupport.requireNonNull(communicationRequest.status, "status");
            ValidationSupport.checkList(communicationRequest.category, "category", CodeableConcept.class);
            ValidationSupport.checkList(communicationRequest.medium, "medium", CodeableConcept.class);
            ValidationSupport.checkList(communicationRequest.about, "about", Reference.class);
            ValidationSupport.checkList(communicationRequest.payload, "payload", Payload.class);
            ValidationSupport.choiceElement(communicationRequest.occurrence, "occurrence", DateTime.class, Period.class);
            ValidationSupport.checkList(communicationRequest.recipient, "recipient", Reference.class);
            ValidationSupport.checkList(communicationRequest.reasonCode, "reasonCode", CodeableConcept.class);
            ValidationSupport.checkList(communicationRequest.reasonReference, "reasonReference", Reference.class);
            ValidationSupport.checkList(communicationRequest.note, "note", Annotation.class);
            ValidationSupport.checkReferenceType(communicationRequest.replaces, "replaces", "CommunicationRequest");
            ValidationSupport.checkReferenceType(communicationRequest.subject, "subject", "Patient", "Group");
            ValidationSupport.checkReferenceType(communicationRequest.encounter, "encounter", "Encounter");
            ValidationSupport.checkReferenceType(communicationRequest.requester, "requester", "Practitioner", "PractitionerRole", "Organization", "Patient", "RelatedPerson", "Device");
            ValidationSupport.checkReferenceType(communicationRequest.recipient, "recipient", "Device", "Organization", "Patient", "Practitioner", "PractitionerRole", "RelatedPerson", "Group", "CareTeam", "HealthcareService");
            ValidationSupport.checkReferenceType(communicationRequest.sender, "sender", "Device", "Organization", "Patient", "Practitioner", "PractitionerRole", "RelatedPerson", "HealthcareService");
            ValidationSupport.checkReferenceType(communicationRequest.reasonReference, "reasonReference", "Condition", "Observation", "DiagnosticReport", "DocumentReference");
        }

        protected Builder from(CommunicationRequest communicationRequest) {
            super.from(communicationRequest);
            identifier.addAll(communicationRequest.identifier);
            basedOn.addAll(communicationRequest.basedOn);
            replaces.addAll(communicationRequest.replaces);
            groupIdentifier = communicationRequest.groupIdentifier;
            status = communicationRequest.status;
            statusReason = communicationRequest.statusReason;
            category.addAll(communicationRequest.category);
            priority = communicationRequest.priority;
            doNotPerform = communicationRequest.doNotPerform;
            medium.addAll(communicationRequest.medium);
            subject = communicationRequest.subject;
            about.addAll(communicationRequest.about);
            encounter = communicationRequest.encounter;
            payload.addAll(communicationRequest.payload);
            occurrence = communicationRequest.occurrence;
            authoredOn = communicationRequest.authoredOn;
            requester = communicationRequest.requester;
            recipient.addAll(communicationRequest.recipient);
            sender = communicationRequest.sender;
            reasonCode.addAll(communicationRequest.reasonCode);
            reasonReference.addAll(communicationRequest.reasonReference);
            note.addAll(communicationRequest.note);
            return this;
        }
    }

    /**
     * Text, attachment(s), or resource(s) to be communicated to the recipient.
     */
    public static class Payload extends BackboneElement {
        @Choice({ String.class, Attachment.class, Reference.class })
        @Required
        private final Element content;

        private Payload(Builder builder) {
            super(builder);
            content = builder.content;
        }

        /**
         * The communicated content (or for multi-part communications, one portion of the communication).
         * 
         * @return
         *     An immutable object of type {@link String}, {@link Attachment} or {@link Reference} that is non-null.
         */
        public Element getContent() {
            return content;
        }

        @Override
        public boolean hasChildren() {
            return super.hasChildren() || 
                (content != null);
        }

        @Override
        public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
            if (visitor.preVisit(this)) {
                visitor.visitStart(elementName, elementIndex, this);
                if (visitor.visit(elementName, elementIndex, this)) {
                    // visit children
                    accept(id, "id", visitor);
                    accept(extension, "extension", visitor, Extension.class);
                    accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                    accept(content, "content", visitor);
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
            Payload other = (Payload) obj;
            return Objects.equals(id, other.id) && 
                Objects.equals(extension, other.extension) && 
                Objects.equals(modifierExtension, other.modifierExtension) && 
                Objects.equals(content, other.content);
        }

        @Override
        public int hashCode() {
            int result = hashCode;
            if (result == 0) {
                result = Objects.hash(id, 
                    extension, 
                    modifierExtension, 
                    content);
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

        public static class Builder extends BackboneElement.Builder {
            private Element content;

            private Builder() {
                super();
            }

            /**
             * Unique id for the element within a resource (for internal references). This may be any string value that does not 
             * contain spaces.
             * 
             * @param id
             *     Unique id for inter-element referencing
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder id(java.lang.String id) {
                return (Builder) super.id(id);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
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
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
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
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Adds new element(s) to the existing list.
             * If any of the elements are null, calling {@link #build()} will fail.
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder modifierExtension(Extension... modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection.
             * If any of the elements are null, calling {@link #build()} will fail.
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             * 
             * @throws NullPointerException
             *     If the passed collection is null
             */
            @Override
            public Builder modifierExtension(Collection<Extension> modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * Convenience method for setting {@code content} with choice type String.
             * 
             * <p>This element is required.
             * 
             * @param content
             *     Message part content
             * 
             * @return
             *     A reference to this Builder instance
             * 
             * @see #content(Element)
             */
            public Builder content(java.lang.String content) {
                this.content = (content == null) ? null : String.of(content);
                return this;
            }

            /**
             * The communicated content (or for multi-part communications, one portion of the communication).
             * 
             * <p>This element is required.
             * 
             * <p>This is a choice element with the following allowed types:
             * <ul>
             * <li>{@link String}</li>
             * <li>{@link Attachment}</li>
             * <li>{@link Reference}</li>
             * </ul>
             * 
             * @param content
             *     Message part content
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder content(Element content) {
                this.content = content;
                return this;
            }

            /**
             * Build the {@link Payload}
             * 
             * <p>Required elements:
             * <ul>
             * <li>content</li>
             * </ul>
             * 
             * @return
             *     An immutable object of type {@link Payload}
             * @throws IllegalStateException
             *     if the current state cannot be built into a valid Payload per the base specification
             */
            @Override
            public Payload build() {
                Payload payload = new Payload(this);
                if (validating) {
                    validate(payload);
                }
                return payload;
            }

            protected void validate(Payload payload) {
                super.validate(payload);
                ValidationSupport.requireChoiceElement(payload.content, "content", String.class, Attachment.class, Reference.class);
                ValidationSupport.requireValueOrChildren(payload);
            }

            protected Builder from(Payload payload) {
                super.from(payload);
                content = payload.content;
                return this;
            }
        }
    }
}
