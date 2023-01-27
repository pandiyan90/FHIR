/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.remote.index.kafka;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.database.utils.thread.ThreadHandler;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceDataAccessException;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.helper.RemoteIndexSupport;
import net.sovrinhealth.fhir.persistence.index.DateParameter;
import net.sovrinhealth.fhir.persistence.index.LocationParameter;
import net.sovrinhealth.fhir.persistence.index.NumberParameter;
import net.sovrinhealth.fhir.persistence.index.ProfileParameter;
import net.sovrinhealth.fhir.persistence.index.QuantityParameter;
import net.sovrinhealth.fhir.persistence.index.ReferenceParameter;
import net.sovrinhealth.fhir.persistence.index.RemoteIndexMessage;
import net.sovrinhealth.fhir.persistence.index.SearchParametersTransport;
import net.sovrinhealth.fhir.persistence.index.SecurityParameter;
import net.sovrinhealth.fhir.persistence.index.StringParameter;
import net.sovrinhealth.fhir.persistence.index.TagParameter;
import net.sovrinhealth.fhir.persistence.index.TokenParameter;
import net.sovrinhealth.fhir.persistence.params.api.IMessageHandler;
import net.sovrinhealth.fhir.persistence.params.api.IParamValueCollector;
import net.sovrinhealth.fhir.persistence.params.api.IParamValueProcessor;


/**
 * Base for the Kafka message handler to load message data into
 * a database via JDBC.
 */
public abstract class ParamMessageHandler implements IMessageHandler {
    private final Logger logger = Logger.getLogger(ParamMessageHandler.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    private static final int MIN_SUPPORTED_MESSAGE_VERSION = 1;

    // If we fail 10 times due to deadlocks, then something is seriously wrong
    private static final int MAX_TX_ATTEMPTS = 10;
    private SecureRandom random = new SecureRandom();

    private final long maxReadyWaitMs;

    // Process messages only from a known origin
    private final String instanceIdentifier;

    // The collector to collect all the search parameter values
    protected final IParamValueCollector paramValueCollector;

    // The processor to which we delegate our parameter calls
    protected final IParamValueProcessor paramValueProcessor;

    /**
     * Protected constructor
     * @param instanceIdentifier
     * @param maxReadyWaitMs the max time in ms to wait for the upstream transaction to make the data ready
     * @param paramValueCollector
     * @param paramValueProcessor
     */
    protected ParamMessageHandler(String instanceIdentifier, long maxReadyWaitMs, 
            IParamValueCollector paramValueCollector, IParamValueProcessor paramValueProcessor) {
        if (instanceIdentifier == null || instanceIdentifier.isEmpty()) {
            throw new IllegalArgumentException("Must specify an instanceIdentifier value");
        }
        this.instanceIdentifier = instanceIdentifier;
        this.maxReadyWaitMs = maxReadyWaitMs;
        this.paramValueCollector = paramValueCollector;
        this.paramValueProcessor = paramValueProcessor;
    }

    @Override
    public void process(List<String> messages) throws FHIRPersistenceException {
        List<RemoteIndexMessage> unmarshalled = new ArrayList<>(messages.size());
        for (String payload: messages) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Processing message payload: " + payload);
            }
            RemoteIndexMessage message = RemoteIndexSupport.unmarshall(payload);
            if (message != null) {
                if (message.getMessageVersion() >= MIN_SUPPORTED_MESSAGE_VERSION) {
                    // check to make sure that the instanceIdentifier matches our configuration. This protects us
                    // from messages accidentally sent over the same topic from another instance
                    if (this.instanceIdentifier.equals(message.getInstanceIdentifier())) {
                        unmarshalled.add(message);
                    } else {
                        logger.warning("Message from unknown origin, ignoring payload=[" + payload + "]");
                    }
                } else {
                    logger.warning("Message version [" + message.getMessageVersion() + "] not supported, ignoring payload=[" + payload + "]");
                }
            }
        }

        if (unmarshalled.size() > 0) {
            processWithRetry(unmarshalled);
        }
    }

    /**
     * Process the batch of messages with support for retries in the case
     * of a retryable error such as a database deadlock
     * 
     * @param messages
     * @throws FHIRPersistenceException
     */
    private void processWithRetry(List<RemoteIndexMessage> messages) throws FHIRPersistenceException {
        int attempt = 1;
        do {
            try {
                if (attempt > 1) {
                    // introduce a random delay before we re-attempt to process the batch. This
                    // may help to avoid subsequent deadlocks if there are multiple transactions
                    // involved
                    final long delay = random.nextInt(10) * 1000l;
                    logger.fine(() -> "Deadlock retry backoff ms: " + delay);
                    ThreadHandler.safeSleep(delay);
                }
                paramValueProcessor.startBatch();
                processMessages(messages);
                paramValueCollector.publish(paramValueProcessor);
                paramValueProcessor.pushBatch();

                attempt = MAX_TX_ATTEMPTS; // exit our do...while
            } catch (FHIRPersistenceDataAccessException x) {
                setRollbackOnly();
                // see if this is a retryable error
                if (x.isTransactionRetryable() && attempt++ < MAX_TX_ATTEMPTS) {
                    logger.warning("tx failed, but retry permitted: " + x.getMessage());
                    paramValueProcessor.resetBatch(); // clear up any cruft from the previous attempt
                } else {
                    throw x;
                }
            } catch (Throwable t) {
                setRollbackOnly();
                logger.log(Level.SEVERE, "batch failed", t);
                throw t;
            } finally {
                endTransaction();
            }
        } while (attempt < MAX_TX_ATTEMPTS);
    }

    /**
     * Mark the transaction for rollback
     */
    protected abstract void setRollbackOnly();

    /**
     * Process the list of messages
     * @param messages
     * @throws FHIRPersistenceException
     */
    private void processMessages(List<RemoteIndexMessage> messages) throws FHIRPersistenceException {
        // We need to do a quick scan of all the messages to make sure that
        // the logical resource records for each already exist. If the check
        // returns anything in the notReady list, it means one of two things:
        // 1. we received the message before the server transaction committed
        // 2. the server transaction failed/rolled back, so we'll never be ready
        long timeoutTime = System.nanoTime() + this.maxReadyWaitMs * 1000000;

        // Messages which match the current version info in the database
        List<RemoteIndexMessage> okToProcess = new ArrayList<>();

        // resources which don't yet exist of their version is older than the message
        List<RemoteIndexMessage> notReady = new ArrayList<>();

        // make at least one attempt
        do {
            if (okToProcess.size() > 0) {
                okToProcess.clear(); // reset ready for next prepare call
            }
            if (notReady.size() > 0) {
                notReady.clear(); // reset ready for next prepare call
            }

            // Ask the handle to check which messages match the database
            // and are therefore ready to be processed
            checkReady(messages, okToProcess, notReady);

            // If the ready check fails just sleep for a bit because we need
            // to wait until the upstream transaction commits. This means we
            // may need to keep waiting for a long time which unfortunately
            // stalls processing this partition
            if (notReady.size() > 0) {
                long snoozeMs = Math.min(1000l, (timeoutTime - System.nanoTime()) / 1000000);
                // short sleep to wait for the upstream transaction to complete
                if (snoozeMs > 0) {
                    ThreadHandler.safeSleep(snoozeMs);
                }
            }
        } while (notReady.size() > 0 && System.nanoTime() < timeoutTime);

        // okToProcess contains those messages for which we see the upstream transaction
        // has committed.
        for (RemoteIndexMessage message: okToProcess) {
            process(message);
        }

        // Make a note of which messages we were unable to process because the upstream
        // transaction did not commit before our maxReadyWaitMs timeout
        for (RemoteIndexMessage message: notReady) {
            logger.warning("Timed out waiting for upstream transaction to commit data for: " + message.toString());
        }
    }

    /**
     * Check to see if the database is ready to process the messages
     * @param  IN: messages to check
     * @param OUT: okToMessages the messages matching the current database
     * @param OUT: notReady the messages for which the upstream transaction has yet to commit
     */
    protected abstract void checkReady(List<RemoteIndexMessage> messages, List<RemoteIndexMessage> okToProcess, List<RemoteIndexMessage> notReady) throws FHIRPersistenceException;

    /**
     * Process the data 
     * @param message
     */
    private void process(RemoteIndexMessage message) throws FHIRPersistenceException {
        SearchParametersTransport params = message.getData();
        if (params.getStringValues() != null) {
            for (StringParameter p: params.getStringValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getDateValues() != null) {
            for (DateParameter p: params.getDateValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getNumberValues() != null) {
            for (NumberParameter p: params.getNumberValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getQuantityValues() != null) {
            for (QuantityParameter p: params.getQuantityValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);                
            }
        }

        if (params.getTokenValues() != null) {
            for (TokenParameter p: params.getTokenValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getLocationValues() != null) {
            for (LocationParameter p: params.getLocationValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getTagValues() != null) {
            for (TagParameter p: params.getTagValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getProfileValues() != null) {
            for (ProfileParameter p: params.getProfileValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getSecurityValues() != null) {
            for (SecurityParameter p: params.getSecurityValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }

        if (params.getRefValues() != null) {
            for (ReferenceParameter p: params.getRefValues()) {
                paramValueCollector.collect(message.getTenantId(), params.getRequestShard(), params.getResourceType(), params.getLogicalId(), params.getLogicalResourceId(), p);
            }
        }
    }

    /**
     * Tell the persistence layer to commit the current transaction, or perform a rollback
     * if setRollbackOnly() has been called.
     * 
     * @throws FHIRPersistenceException
     */
    protected abstract void endTransaction() throws FHIRPersistenceException;
}
