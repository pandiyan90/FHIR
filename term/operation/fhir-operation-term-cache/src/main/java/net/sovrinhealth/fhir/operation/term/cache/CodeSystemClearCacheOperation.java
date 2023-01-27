/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.term.cache;

import java.io.InputStream;

import net.sovrinhealth.fhir.cache.CacheKey;
import net.sovrinhealth.fhir.cache.CacheManager;
import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.core.HTTPReturnPreference;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.CodeSystem;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.code.IssueSeverity;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.operation.term.AbstractTermOperation;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.registry.ServerRegistryResourceProvider;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationUtil;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;
import net.sovrinhealth.fhir.term.util.CodeSystemSupport;

public class CodeSystemClearCacheOperation extends AbstractTermOperation {

    @Override
    protected OperationDefinition buildOperationDefinition() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("operation-codesystem-clear-cache.json")) {
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
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

        CacheManager.invalidateAll(CodeSystemSupport.ANCESTORS_AND_SELF_CACHE_NAME);
        CacheManager.invalidateAll(CodeSystemSupport.DESCENDANTS_AND_SELF_CACHE_NAME);

        try {
            if (FHIROperationContext.Type.INSTANCE.equals(operationContext.getType()) || parameters.getParameter().size() > 0 ) {
                CodeSystem codeSystem = getResource(operationContext, logicalId, parameters, resourceHelper, CodeSystem.class );
                clearServerRegistryCache(codeSystem);
            }

            OperationOutcome operationOutcome = OperationOutcome.builder().issue(
                OperationOutcome.Issue.builder()
                    .severity(IssueSeverity.INFORMATION)
                    .code(IssueType.INFORMATIONAL)
                    .details(CodeableConcept.builder().coding(
                        Coding.builder().code(Code.of("success")).build()
                     ).build()).build()
                ).build();

            if (FHIRRequestContext.get().getReturnPreference() == HTTPReturnPreference.OPERATION_OUTCOME) {
                return FHIROperationUtil.getOutputParameters(operationOutcome);
            } else {
                return null;
            }
        } catch( Throwable t ) {
            throw new FHIROperationException("Unexpected error occurred while processing request for operation '"
                    + getName() + "': " + getCausedByMessage(t), t);
        }
    }

    private String getCausedByMessage(Throwable throwable) {
        return throwable.getClass().getName() + ": " + throwable.getMessage();
    }

    private void clearServerRegistryCache(CodeSystem resource) {
        String dataStoreId = FHIRRequestContext.get().getDataStoreId();
        String url = resource.getUrl().getValue();
        CacheManager.invalidate(ServerRegistryResourceProvider.REGISTRY_RESOURCE_CACHE_NAME, CacheKey.key(dataStoreId,url));
        if( resource.getVersion() != null ) {
            url = url + "|" + resource.getVersion().getValue();
            CacheManager.invalidate(ServerRegistryResourceProvider.REGISTRY_RESOURCE_CACHE_NAME, CacheKey.key(dataStoreId,url));
        }
    }
}
