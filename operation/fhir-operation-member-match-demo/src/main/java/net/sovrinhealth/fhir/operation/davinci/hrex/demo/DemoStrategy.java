/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.davinci.hrex.demo;

import java.util.logging.Logger;

import net.sovrinhealth.fhir.config.PropertyGroup;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.operation.davinci.hrex.configuration.ConfigurationFactory;
import net.sovrinhealth.fhir.operation.davinci.hrex.provider.strategy.DefaultMemberMatchStrategy;
import net.sovrinhealth.fhir.operation.davinci.hrex.provider.strategy.MemberMatchResult;

/**
 * Used to Demonstrate a custom Strategy
 */
public class DemoStrategy extends DefaultMemberMatchStrategy {

    private static final Logger LOG = Logger.getLogger(DemoStrategy.class.getSimpleName());

    @Override
    public String getMemberMatchIdentifier() {
        return "demo";
    }

    @Override
    public void validate(Parameters input) throws FHIROperationException {
        LOG.info("Validating Content for strategy - " + this.getMemberMatchIdentifier());
        super.validate(input);
    }

    @Override
    public MemberMatchResult executeMemberMatch() throws FHIROperationException {
        LOG.info("executeMemberMatch for strategy - " + this.getMemberMatchIdentifier());
        PropertyGroup group = ConfigurationFactory.factory().getConfigurationAdapter().getExtendedStrategyPropertyGroup();
        if (group != null) {
            LOG.info("executeMemberMatch Extend Strategy Config is " + group.getJsonObj());
        }
        return super.executeMemberMatch();
    }
}