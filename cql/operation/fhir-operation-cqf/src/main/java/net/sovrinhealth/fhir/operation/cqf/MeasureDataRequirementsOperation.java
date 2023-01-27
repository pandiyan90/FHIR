/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.operation.cqf;

import java.util.Arrays;
import java.util.List;

import net.sovrinhealth.fhir.core.ResourceType;
import net.sovrinhealth.fhir.cql.helpers.LibraryHelper;
import net.sovrinhealth.fhir.ecqm.r4.MeasureHelper;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.Library;
import net.sovrinhealth.fhir.model.resource.Measure;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.RelatedArtifact;
import net.sovrinhealth.fhir.model.type.code.RelatedArtifactType;
import net.sovrinhealth.fhir.persistence.SingleResourceResult;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

public class MeasureDataRequirementsOperation extends AbstractDataRequirementsOperation {

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Measure-data-requirements", OperationDefinition.class);
    }

    @Override
    public Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
            Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper) throws FHIROperationException {

        Measure measure = null;
        try {
            SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.MEASURE.value(), logicalId);
            measure = (Measure) readResult.getResource();
            if (measure == null) {
                throw new FHIROperationException("Failed to resolve measure with resource id: " + logicalId);
            }
        } catch (FHIROperationException fex) {
            throw fex;
        } catch (Exception ex) {
            throw new FHIROperationException("Failed to read resource", ex);
        }

        int numLibraries = (measure.getLibrary() != null) ? measure.getLibrary().size() : 0;
        if (numLibraries != 1) {
            throw new IllegalArgumentException(String.format("Unexpected number of libraries '%d' referenced by measure '%s'", numLibraries, measure.getId()));
        }

        String primaryLibraryId = MeasureHelper.getPrimaryLibraryId(measure);
        Library primaryLibrary = OperationHelper.loadLibraryByReference(resourceHelper, primaryLibraryId);
        List<Library> fhirLibraries = LibraryHelper.loadLibraries(primaryLibrary);

        RelatedArtifact related = RelatedArtifact.builder().type(RelatedArtifactType.DEPENDS_ON).resource(measure.getLibrary().get(0)).build();
        return doDataRequirements(fhirLibraries, () -> Arrays.asList(related) );
    }
}
