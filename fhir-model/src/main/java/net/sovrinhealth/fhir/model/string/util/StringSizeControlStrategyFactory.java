/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.string.util;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.string.util.strategy.MaxBytesStringSizeControlStrategy;
import net.sovrinhealth.fhir.model.string.util.strategy.StringSizeControlStrategy;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.model.util.FHIRUtil;

/**
 * Controls the creation of the StringSizeControlStrategy objects using the ServiceLoader.
 */
public class StringSizeControlStrategyFactory {
    
    private static final StringSizeControlStrategyFactory FACTORY = new StringSizeControlStrategyFactory();
    
   /**
    * Private Constructor
    */
    private StringSizeControlStrategyFactory() {
        // Nop
    }

    /**
     * Enumeration of the various types of StringSizeControlStrategy.
     */
    public enum Strategy {
        MAX_BYTES("max_bytes");
        public String value;
        
        /**
         * Constructor for the enum
         * @param value the type of StringSizeControlStrategy
         */
        Strategy(String value) {
            this.value = value;
        }
        
        /**
         * Get the type of StringSizeControlStrategy
         * @return the type of StringSizeControlStrategy
         */
        public String getValue() {
            return this.value;
        }
    }
    
    /**
     * Gets the factory
     * @return
     */
    public static StringSizeControlStrategyFactory factory() {
        return FACTORY;
    }

    

    /**
     * Gets the strategy for this specific strategyIdentifier.
     * @param strategyIdentifier the unique StringSizeControlStrategy identifier.
     * @return the StringSizeControlStrategy implementation
     * @throws FHIROperationException when the strategyIdentifier is invalid. 
     */
    public StringSizeControlStrategy getStrategy(Strategy strategyIdentifier) throws FHIROperationException {
        switch (strategyIdentifier) {
        case MAX_BYTES:
            return new MaxBytesStringSizeControlStrategy();
        default:
            String message =  "The StringSizeControlStrategy is not found [" + strategyIdentifier + "]";
            OperationOutcome.Issue ooi = FHIRUtil.buildOperationOutcomeIssue(message, IssueType.EXCEPTION);
            throw new FHIROperationException(message, null).withIssue(ooi);
        }
        
    }
}