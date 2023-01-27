/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.flow.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.bucket.client.FHIRBucketClientUtil;
import net.sovrinhealth.fhir.flow.api.FlowInteraction;
import net.sovrinhealth.fhir.flow.api.IFlowInteractionHandler;
import net.sovrinhealth.fhir.flow.api.IFlowWriter;
import net.sovrinhealth.fhir.flow.api.ResourceIdentifier;
import net.sovrinhealth.fhir.flow.util.PartitionExecutor;
import net.sovrinhealth.fhir.model.resource.Resource;

/**
 * Handle a stream of interactions received from the upstream system
 * by simply logging the details of the request. 
 * Useful for development/debug
 */
public class DownstreamLogWriter implements IFlowWriter, IFlowInteractionHandler {
    private static final Logger logger = Logger.getLogger(DownstreamLogWriter.class.getName());

    // The partitioned thread pool for processing requests
    private final PartitionExecutor<FlowInteraction> pool;

    // set to false to shut down
    private boolean running = true;

    // Should we log resource data in the output
    private final boolean logData;

    /**
     * Public constructor
     * @param partitionCount
     * @param partitionQueueSize
     * @param logData
     */
    public DownstreamLogWriter(int partitionCount, int partitionQueueSize, boolean logData) {
        this.pool = new PartitionExecutor<>(partitionCount, partitionQueueSize, (interaction) -> partitionKey(interaction), (interaction) -> handler(interaction));
        this.logData = logData;
    }

    @Override
    public void submit(FlowInteraction interaction) {
        // Submit the interaction to the partitioned thread pool. The interactions
        // are distributed across the threads in the pool using a partition key
        // based on the resourceType/logicalId value. This guarantees that we
        // serialize any interactions on the same resource, thus preserving the
        // order of changes as they occurred on the upstream system
        if (this.running) {
            logger.fine(() -> "SUBMIT: " + interaction.toString());
            this.pool.submit(interaction);
        } else {
            logger.severe("SUBMIT BLOCKED: " + interaction.toString());
            throw new IllegalStateException("writer shut down");
        }
    }

    @Override
    public void waitForShutdown() {
        // block new work from being submitted to the pool
        this.running = false;
        this.pool.shutdown();
    }

    /**
     * Perform the interaction
     * @param interaction
     */
    private void handler(FlowInteraction interaction) {
        interaction.accept(this); // this is also an IFlowInteractionHandler
    }

    /**
     * Compute a partition key value
     * @param interaction
     * @return
     */
    private String partitionKey(FlowInteraction interaction) {
        // make sure that the same resourceType/identifier values go to
        // one partition - this ensures that they will be processed
        // in order, thus preserving the correct change history for
        // any given resource.
        return interaction.getIdentifier().toString();
    }

    @Override
    public void delete(String entryId, ResourceIdentifier identifier) {
        logger.fine(() -> "DELETE [" + entryId + "] " + identifier.toString());
    }

    @Override
    public void createOrUpdate(String entryId, ResourceIdentifier identifier, String resourceData, Resource resource) {
        if (logData) {
            if (resourceData == null) {
                resourceData = FHIRBucketClientUtil.resourceToString(resource);
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("PUT [" + entryId + "] " + identifier.getFullUrl() + " " + resourceData);
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("PUT [" + entryId + "] " + identifier.getFullUrl());
            }
        }
    }
}