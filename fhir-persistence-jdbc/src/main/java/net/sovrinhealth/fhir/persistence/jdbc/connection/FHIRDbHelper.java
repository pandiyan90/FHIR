/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.connection;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.model.util.FHIRUtil;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceDataAccessException;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.jdbc.exception.FHIRPersistenceDBCleanupException;
import net.sovrinhealth.fhir.persistence.jdbc.exception.FHIRPersistenceDBConnectException;

/**
 * Helper functions used for managing FHIR database interactions
 */
public class FHIRDbHelper {
    private static final Logger log = Logger.getLogger(FHIRDbHelper.class.getName());
    
    /**
     * Convenience function to log the cause of an exception about to be thrown. This
     * is useful when avoiding chaining the cause with the persistence exception, which
     * could inadvertently leak sensitive information (details of the schema, for example)
     *
     * @param logger
     * @param fx
     * @param cause
     * @return
     */
    public static <XT extends FHIRPersistenceException> XT severe(Logger logger, XT fx, Throwable cause) {
        logger.log(Level.SEVERE, fx.getMessage(), cause);
        return fx;
    }
    
    /**
     * Log the exception message here along with the cause stack. Return the
     * exception fx to the caller so that it can be thrown easily.
     *
     * @param logger
     * @param fx
     * @param errorMessage
     * @param cause
     * @return
     */
    public static <XT extends FHIRPersistenceException> XT severe(Logger logger, XT fx, String errorMessage,
            Throwable cause) {
        if (cause != null) {
            logger.log(Level.SEVERE, fx.addProbeId(errorMessage), cause);
        } else {
            logger.log(Level.SEVERE, fx.addProbeId(errorMessage));
        }
        return fx;
    }
    
    public static FHIRPersistenceDataAccessException buildExceptionWithIssue(String msg, IssueType issueType)
            throws FHIRPersistenceDataAccessException {
        Issue ooi = FHIRUtil.buildOperationOutcomeIssue(msg, issueType);
        return new FHIRPersistenceDataAccessException(msg).withIssue(ooi);
    }

    public static FHIRPersistenceDBConnectException buildFHIRPersistenceDBConnectException(String msg, IssueType issueType)
            throws FHIRPersistenceDBConnectException {
        Issue ooi = FHIRUtil.buildOperationOutcomeIssue(msg, issueType);
        return new FHIRPersistenceDBConnectException(msg).withIssue(ooi);
    }
}
