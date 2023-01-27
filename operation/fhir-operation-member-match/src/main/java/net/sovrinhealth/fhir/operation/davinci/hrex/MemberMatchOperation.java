/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.davinci.hrex;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.operation.davinci.hrex.configuration.ConfigurationAdapter;
import net.sovrinhealth.fhir.operation.davinci.hrex.configuration.ConfigurationFactory;
import net.sovrinhealth.fhir.operation.davinci.hrex.provider.MemberMatchFactory;
import net.sovrinhealth.fhir.operation.davinci.hrex.provider.strategy.MemberMatchStrategy;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.AbstractOperation;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationUtil;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * Implements the $MemberMatch Operation
 */
public class MemberMatchOperation extends AbstractOperation {

    public MemberMatchOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance()
                .getResource("http://hl7.org/fhir/us/davinci-hrex/OperationDefinition/member-match",
                    OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper)
            throws FHIROperationException {
        ConfigurationAdapter config = ConfigurationFactory.factory().getConfigurationAdapter();

        if (!config.enabled()) {
            throw FHIROperationUtil.buildExceptionWithIssue("$member-match is not supported", IssueType.NOT_SUPPORTED);
        }

        MemberMatchStrategy strategy = MemberMatchFactory.factory().getStrategy(config);
        return strategy.execute(operationContext, parameters, resourceHelper);
    }
}