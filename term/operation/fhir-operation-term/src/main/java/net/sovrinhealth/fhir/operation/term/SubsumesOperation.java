/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.term;

import static net.sovrinhealth.fhir.model.type.String.string;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Parameters.Parameter;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.code.ConceptSubsumptionOutcome;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;
import net.sovrinhealth.fhir.term.service.exception.FHIRTermServiceException;

public class SubsumesOperation extends AbstractTermOperation {
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/CodeSystem-subsumes", OperationDefinition.class);
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
            CodeSystem codeSystem = FHIROperationContext.Type.INSTANCE.equals(operationContext.getType()) ?
                    getResource(operationContext, logicalId, parameters, resourceHelper, CodeSystem.class) : null;
            Coding codingA = getCoding(parameters, "codingA", "codeA", (codeSystem == null));
            Coding codingB = getCoding(parameters, "codingB", "codeB", (codeSystem == null));
            if (codeSystem != null) {
                codingA = codingA.toBuilder().system(codeSystem.getUrl()).build();
                codingB = codingB.toBuilder().system(codeSystem.getUrl()).build();
            }
            ConceptSubsumptionOutcome outcome = service.subsumes(codingA, codingB);
            if (outcome == null) {
                throw buildExceptionWithIssue("Subsumption cannot be tested", IssueType.NOT_SUPPORTED);
            }
            return Parameters.builder()
                    .parameter(Parameter.builder()
                        .name(string("outcome"))
                        .value(outcome)
                        .build())
                    .build();
        } catch (FHIROperationException e) {
            throw e;
        } catch (FHIRTermServiceException e) {
            throw new FHIROperationException(e.getMessage(), e.getCause()).withIssue(e.getIssues());
        } catch (UnsupportedOperationException e) {
            throw buildExceptionWithIssue(e.getMessage(), IssueType.NOT_SUPPORTED, e);
        } catch (Exception e) {
            throw new FHIROperationException("An error occurred during the CodeSystem subsumes operation", e);
        }
    }
}