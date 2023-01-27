/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.core.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import net.sovrinhealth.fhir.core.FHIRVersionParam;
import net.sovrinhealth.fhir.core.ResourceType;

/**
 * Helper methods for working with FHIR Resource Type Strings
 */
public class ResourceTypeUtil {
    private static final Set<ResourceType> R4_ENUMS = collectResourceTypesFor(FHIRVersionParam.VERSION_40);
    private static final Set<ResourceType> R4B_ENUMS = collectResourceTypesFor(FHIRVersionParam.VERSION_43);
    private static final Set<ResourceType> R4B_ONLY_RESOURCE_ENUMS = collectR4bOnlyResourceTypes();
    private static final Set<ResourceType> ABSTRACT_TYPE_ENUMS = Sets.newHashSet(
                ResourceType.RESOURCE,
                ResourceType.DOMAIN_RESOURCE
            );
    /**
     * valid instances from 4.0 may not be valid in 4.3
     */
    private static final Set<ResourceType> BACKWARD_BREAKING_R4B_ENUMS = Sets.newHashSet(
                ResourceType.EVIDENCE,
                ResourceType.EVIDENCE_VARIABLE
            );
    /**
     * valid instances from 4.3 may not be valid in 4.0
     */
    private static final Set<ResourceType> FORWARD_BREAKING_R4B_ENUMS = Sets.newHashSet(
                ResourceType.EVIDENCE,
                ResourceType.EVIDENCE_VARIABLE,
                ResourceType.ACTIVITY_DEFINITION,
                ResourceType.PLAN_DEFINITION
            );

    private static final Set<String> R4_COMPATIBLE_RESOURCES = Collections.unmodifiableSet(new LinkedHashSet<>(
            R4_ENUMS.stream()
                .filter(rtn -> !ABSTRACT_TYPE_ENUMS.contains(rtn))
                .map(ResourceType::value)
                .collect(Collectors.toList())));

    private static final Set<String> R4B_COMPATIBLE_RESOURCES = Collections.unmodifiableSet(new LinkedHashSet<>(
            R4B_ENUMS.stream()
                .filter(rtn -> !ABSTRACT_TYPE_ENUMS.contains(rtn))
                .map(ResourceType::value)
                .collect(Collectors.toList())));

    private static final Set<String> R4_AND_R4B_COMPATIBLE_RESOURCES = Collections.unmodifiableSet(new LinkedHashSet<>(
            R4B_ENUMS.stream()
                .filter(rtn -> !R4B_ONLY_RESOURCE_ENUMS.contains(rtn))
                .filter(rtn -> !BACKWARD_BREAKING_R4B_ENUMS.contains(rtn))
                .filter(rtn -> !ABSTRACT_TYPE_ENUMS.contains(rtn))
                .map(ResourceType::value)
                .collect(Collectors.toList())));

    private static final Set<String> ABSTRACT_RESOURCES = Collections.unmodifiableSet(
            ABSTRACT_TYPE_ENUMS.stream()
                .map(ResourceType::value)
                .collect(Collectors.toSet()));

    private static final Set<String> BACKWARD_BREAKING_R4B_RESOURCES = Collections.unmodifiableSet(
            BACKWARD_BREAKING_R4B_ENUMS.stream()
                .map(ResourceType::value)
                .collect(Collectors.toSet()));

    private static final Set<String> FORWARD_BREAKING_R4B_RESOURCES = Collections.unmodifiableSet(
            FORWARD_BREAKING_R4B_ENUMS.stream()
                .map(ResourceType::value)
                .collect(Collectors.toSet()));

    /**
     * @return the set of resource type names for all abstract resource types, as of FHIR R4B
     */
    public static Set<String> getAbstractResourceTypeNames() {
        return ABSTRACT_RESOURCES;
    }

    /**
     * @param fhirVersion The value of the MIME-type parameter 'fhirVersion' for the current interaction
     * @return the set of resource type names that were retired with, or before, the passed version of FHIR
     */
    public static Set<String> getRemovedResourceTypes(FHIRVersionParam fhirVersion) {
        return Arrays.stream(ResourceType.values())
            .filter(rt -> rt.getRetired() != null && rt.getRetired().compareTo(fhirVersion) <= 0)
            .map(ResourceType::value)
            .collect(Collectors.toSet());
    }

    /**
     * Get the set of concrete resource type names for which a valid instance in fhirVersion {@code sourceFhirVersion}
     * is expected to be valid in fhirVersion {@code targetFhirVersion}.
     * Does not include any abstract types.
     * @param sourceFhirVersion The source fhirVersion for the compatibility checks
     * @param targetFhirVersion The target fhirVersion for the compatibility checks
     * @return the set of resource type names for which
     *     {@code isCompatible(resourceType, sourceFhirVersion, targetFhirVersion)} would be true
     * @see #isCompatible(String, FHIRVersionParam, FHIRVersionParam)
     */
    public static Set<String> getCompatibleResourceTypes(FHIRVersionParam sourceFhirVersion, FHIRVersionParam targetFhirVersion) {
        if (sourceFhirVersion == FHIRVersionParam.VERSION_40) {
            switch(targetFhirVersion) {
            case VERSION_40:
                return R4_COMPATIBLE_RESOURCES;
            case VERSION_43:
                return R4_AND_R4B_COMPATIBLE_RESOURCES;
            }
        }

        // sourceFhirVersion is 4.3
        if (targetFhirVersion == FHIRVersionParam.VERSION_40) {
            return new LinkedHashSet<>(R4B_ENUMS.stream()
                    .filter(rtn -> !R4B_ONLY_RESOURCE_ENUMS.contains(rtn))
                    .filter(rtn -> !FORWARD_BREAKING_R4B_ENUMS.contains(rtn))
                    .filter(rtn -> !ABSTRACT_TYPE_ENUMS.contains(rtn))
                    .map(ResourceType::value)
                    .collect(Collectors.toList()));
        }
        return R4B_COMPATIBLE_RESOURCES;
    }

    /**
     * If a resourceType instance of type {@code resourceType} is known to be valid in fhirVersion
     * {@code knownValidFhirVersion}, is it expected to also be valid for fhirVersion
     * {@code fhirVersionUnderTest}?
     *
     * <p>Sample usage:
     * <ul>
     * <li>{@code isCompatible("Evidence", VERSION_40, VERSION_43)} returns 'false' because this resource
     *     type has numerous breaking changes in FHIR 4.3
     * <li>{@code isCompatible("ActivityDefinition", VERSION_40, VERSION_43)} returns 'true' because a valid ActivityDefinition
     *     in FHIR 4.0 is also expected to be valid in FHIR 4.3
     * <li>{@code isCompatible("ActivityDefinition", VERSION_43, VERSION_40)} returns 'false' because FHIR 4.3 adds an additional
     *     choice type to ActivityDefinition.subject[x] which is not valid in FHIR 4.0
     * <li>{@code isCompatible("Ingredient", VERSION_40, VERSION_43)} throws IllegalArgumentException because this resource
     *     type was introduced in FHIR 4.3 and therefore fails the "known to be valid in 4.0" precondition
     * </ul>
     *
     * @param resourceType a valid resource type string
     * @param knownValidFhirVersion the source fhirVersion for the compatibility check
     * @param fhirVersionUnderTest the target fhirVersion for the compatibility check
     * @return
     *     whether {@code resourceType} instances of fhirVersion {@code knownValidFhirVersion} are
     *     expected to be compatible with the target {@code fhirVersionUnderTest}
     * @throws IllegalArgumentException
     *     if resourceType is not a valid concrete resourceType for fhirVersion knownValidFhirVersion
     */
    public static boolean isCompatible(String resourceType, FHIRVersionParam knownValidFhirVersion, FHIRVersionParam fhirVersionUnderTest) {
        if (ABSTRACT_RESOURCES.contains(resourceType) || !getResourceTypesFor(knownValidFhirVersion).contains(resourceType)) {
            throw new IllegalArgumentException(resourceType + " is not a valid concrete resourceType for fhirVersion " +
                    knownValidFhirVersion.value());
        }

        if (knownValidFhirVersion == fhirVersionUnderTest) {
            return true;
        }

        if (!getResourceTypesFor(fhirVersionUnderTest).contains(resourceType)) {
            return false;
        }

        switch (knownValidFhirVersion) {
        case VERSION_40:
            // is a valid 4.3 instance of this resourceType expected to be valid in 4.0?
            return !BACKWARD_BREAKING_R4B_RESOURCES.contains(resourceType);
        case VERSION_43:
            // is a valid 4.0 instance of this resourceType expected to be valid in 4.3?
            return !FORWARD_BREAKING_R4B_RESOURCES.contains(resourceType);
        default:
            throw new IllegalArgumentException("unexpected fhirVersion " + knownValidFhirVersion.value());
        }
    }

    /**
     * Get the set of concrete resource type names for the passed fhirVersion.
     * Does not include any abstract types.
     * @param fhirVersion The value of the MIME-type parameter 'fhirVersion' for the current interaction
     * @return a set of resource type names that corresponds to the requested fhirVersion
     */
    public static Set<String> getResourceTypesFor(FHIRVersionParam fhirVersion) {
        switch (fhirVersion) {
        case VERSION_40:
            return R4_COMPATIBLE_RESOURCES;
        case VERSION_43:
            return R4B_COMPATIBLE_RESOURCES;
        default:
            throw new IllegalArgumentException("unexpected fhirVersion " + fhirVersion.value());
        }
    }

    /**
     * Get the set of concrete resource type names for all possible FHIRVersionParam values.
     * Does not include any abstract types.
     * @return
     */
    public static Set<String> getAllResourceTypeNames() {
        final Set<String> resourceTypes = new HashSet<>();
        for (FHIRVersionParam v: FHIRVersionParam.values()) {
            resourceTypes.addAll(getResourceTypesFor(v));
        }
        return Collections.unmodifiableSet(resourceTypes);
    }

    private static Set<ResourceType> collectResourceTypesFor(FHIRVersionParam fhirVersion) {
        Set<ResourceType> set = new LinkedHashSet<>();
        for(ResourceType r : ResourceType.values()) {
            if (r.getIntroduced().compareTo(fhirVersion) <= 0) {
                if (r.getRetired() == null || fhirVersion.compareTo(r.getRetired()) < 0) {
                    set.add(r);
                }
            }
        }
        return set;
    }

    private static Set<ResourceType> collectR4bOnlyResourceTypes() {
        Set<ResourceType> set = new HashSet<>();
        set.add(ResourceType.ADMINISTRABLE_PRODUCT_DEFINITION);
        set.add(ResourceType.CITATION);
        set.add(ResourceType.CLINICAL_USE_DEFINITION);
        set.add(ResourceType.EVIDENCE_REPORT);
        set.add(ResourceType.INGREDIENT);
        set.add(ResourceType.MANUFACTURED_ITEM_DEFINITION);
        set.add(ResourceType.MEDICINAL_PRODUCT_DEFINITION);
        set.add(ResourceType.NUTRITION_PRODUCT);
        set.add(ResourceType.PACKAGED_PRODUCT_DEFINITION);
        set.add(ResourceType.REGULATED_AUTHORIZATION);
        set.add(ResourceType.SUBSCRIPTION_STATUS);
        set.add(ResourceType.SUBSCRIPTION_TOPIC);
        set.add(ResourceType.SUBSTANCE_DEFINITION);
        return set;
    }
}
