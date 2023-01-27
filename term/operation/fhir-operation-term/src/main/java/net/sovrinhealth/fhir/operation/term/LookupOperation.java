/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.term;

import static net.sovrinhealth.fhir.model.type.String.string;

import net.sovrinhealth.fhir.core.FHIRConstants;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Extension;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;
import net.sovrinhealth.fhir.term.service.LookupOutcome;
import net.sovrinhealth.fhir.term.service.LookupParameters;
import net.sovrinhealth.fhir.term.service.exception.FHIRTermServiceException;

public class LookupOperation extends AbstractTermOperation {
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/CodeSystem-lookup",
                OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(
            FHIROperationContext operationContext,
            Class<? extends Resource> resourceType,
            String logicalId,
            String versionId,
            Parameters parameters,
            FHIRResourceHelpers resourceHelper,
            SearchHelper searchHelper) throws FHIROperationException {
        try {
            Coding coding = getCoding(parameters, "coding", "code");
            validate(coding);
            LookupOutcome outcome = service.lookup(coding, LookupParameters.from(parameters));
            if (outcome == null) {
                throw new FHIROperationException("Coding not found")
                    .withIssue(OperationOutcome.Issue.builder()
                        .severity(IssueSeverity.ERROR)
                        .code(IssueType.NOT_FOUND.toBuilder()
                            .extension(Extension.builder()
                                .url(FHIRConstants.EXT_BASE + "not-found-detail")
                                .value(Code.of("coding"))
                                .build())
                            .build())
                        .details(CodeableConcept.builder()
                            .text(string(String.format("Code '%s' not found in system '%s'", coding.getCode().getValue(), coding.getSystem().getValue())))
                            .build())
                        .build());
            }
            return outcome.toParameters();
        } catch (FHIROperationException e) {
            throw e;
        } catch (FHIRTermServiceException e) {
            throw new FHIROperationException(e.getMessage(), e.getCause()).withIssue(e.getIssues());
        } catch (UnsupportedOperationException e) {
            throw buildExceptionWithIssue(e.getMessage(), IssueType.NOT_SUPPORTED, e);
        } catch (Exception e) {
            throw new FHIROperationException("An error occurred during the CodeSystem lookup operation", e);
        }
    }

    private void validate(Coding coding) throws Exception {
        String system = coding.getSystem().getValue();
        String version = (coding.getVersion() != null && coding.getVersion().getValue() != null) ? coding.getVersion().getValue() : null;
        String url = (version != null) ? system + "|" + version : system;
        if (!FHIRRegistry.getInstance().hasResource(url, CodeSystem.class)) {
            throw buildExceptionWithIssue("CodeSystem with url '" + url + "' is not available", IssueType.NOT_SUPPORTED);
        }
    }
}
