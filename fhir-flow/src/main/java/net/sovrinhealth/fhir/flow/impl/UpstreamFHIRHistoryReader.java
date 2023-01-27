/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.flow.impl;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;

import net.sovrinhealth.fhir.bucket.client.FHIRBucketClient;
import net.sovrinhealth.fhir.bucket.client.FhirServerResponse;
import net.sovrinhealth.fhir.bucket.client.RequestOptions;
import net.sovrinhealth.fhir.database.utils.thread.ThreadHandler;
import net.sovrinhealth.fhir.flow.api.ICheckpointTracker;
import net.sovrinhealth.fhir.flow.api.IFlowPool;
import net.sovrinhealth.fhir.flow.api.IFlowWriter;
import net.sovrinhealth.fhir.flow.api.ITrackerTicket;
import net.sovrinhealth.fhir.flow.api.ResourceIdentifier;
import net.sovrinhealth.fhir.flow.api.ResourceIdentifierVersion;
import net.sovrinhealth.fhir.flow.checkpoint.Tracker;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Bundle.Entry;
import net.sovrinhealth.fhir.model.resource.Resource;

/**
 * Reads the change sequence from the upstream system using the
 * whole-system history API. Dispatches vread requests to the flowPool, 
 * and sends the future results to the downstream flowWriter.
 */
public class UpstreamFHIRHistoryReader {
    private static final Logger logger = Logger.getLogger(UpstreamFHIRHistoryReader.class.getName());

    // The pool used to read resource payloads from the upstream system
    private IFlowPool flowPool;

    // The writer used to push resources to the downstream system
    private IFlowWriter flowWriter;

    // The value we use to identify where we are in the upstream change sequence
    private String nextLinkParameters;

    private final int count;

    // Should we ask upstream to exclude the transaction timeout window
    private final boolean excludeTransactionWindow;

    // Should we use the Prefer: return=minimal header for history requests
    private final boolean preferReturnMinimal;

    // how many seconds to drain queued work before leaving
    private final long drainForSeconds;

    // The client we use to call the upstream whole-system history API
    private FHIRBucketClient client;

    // Tracking of which history bundles are still pending downstream completion
    private final ICheckpointTracker<String> tracker = new Tracker<>();

    /**
     * Public constructor
     * 
     * @param count
     * @param fromCheckpoint
     * @param excludeTransactionWindow
     * @param preferReturnMinimal
     * @param drainForSeconds
     */
    public UpstreamFHIRHistoryReader(int count, String fromCheckpoint, 
            boolean excludeTransactionWindow,
            boolean preferReturnMinimal,
            long drainForSeconds) {
        this.count = count;
        this.excludeTransactionWindow = excludeTransactionWindow;
        this.preferReturnMinimal = preferReturnMinimal;
        if (fromCheckpoint != null && fromCheckpoint.length() > 0) {
            this.nextLinkParameters = decodeCheckpoint(fromCheckpoint);
        }
        this.drainForSeconds = drainForSeconds;
    }

    /**
     * Setter for the flowPool
     * @param flowPool
     */
    public void setFlowPool(IFlowPool flowPool) {
        this.flowPool = flowPool;
    }

    /**
     * Setter for the client
     * @param client
     */
    public void setClient(FHIRBucketClient client) {
        this.client = client;
    }

    /**
     * Setter for the flowWriter
     * @param flowWriter
     */
    public void setFlowWriter(IFlowWriter flowWriter) {
        this.flowWriter = flowWriter;
    }

    /**
     * Keep asking for changes until duration expires, at which point we return
     * with the last completed checkpoint value
     * @param duration the length of time to run for; run forever if null
     * @return the last completed checkpoint value
     */
    public String fetch(Duration duration) {
        // Keep calling the whole-system history API. Run for the given
        // duration, or forever if duration is null. The changeIdMarker
        // will advance based on the value of the 'next' link found in
        // the response Bundle. Not that the checkpoint marker is slightly
        // different, because we can only advance the checkpoint once we
        // have processed those records...and that processing is asynchronous.
        long startTime = System.nanoTime();
        long endTime = duration != null ? startTime + duration.getSeconds() * 1000000000l : -1l; 
        String lastNextLinkParameters = this.nextLinkParameters; // to slow down polling when we make no progress
        String lastCheckpointValue = null;
        do {
            fetch();
            if (this.nextLinkParameters == null || this.nextLinkParameters.equals(lastNextLinkParameters)) {
                // If no change was detected, then take a breather so we don't keep spamming
                // the upstream system looking for data when the system is quiet
                ThreadHandler.safeSleep(1000);
            } else {
                lastNextLinkParameters = nextLinkParameters;
            }

            // Write out a new checkpoint value if it has changed since we last looped
            if (lastCheckpointValue == null || !tracker.getCheckpoint().equals(lastCheckpointValue)) {
                lastCheckpointValue = tracker.getCheckpoint();
                if (lastCheckpointValue != null) {
                    logger.info("PROCESSED: " + tracker.getProcessed());
                    logger.info("CHECKPOINT: " + encodeCheckpoint(lastCheckpointValue));
                }
            }
        } while (duration == null || System.nanoTime() < endTime);

        // wait up to 'drainForSeconds' for the remaining work to complete
        logger.info("Waiting for queued work to complete");
        drain();

        // We know we have completed processing up to this value. Make sure
        // we use the same value in our last message as we return, just for
        // consistency
        lastCheckpointValue = tracker.getCheckpoint();
        if (lastCheckpointValue != null) {
            long processed = tracker.getProcessed();
            double elapsed = (System.nanoTime() - startTime) / 1e9;
            double rate = elapsed > 0.0 ? processed / elapsed : Double.NaN;
            logger.info(String.format("Processed %d resources in %.1f seconds [rate=%.1f resources/second]", processed, elapsed, rate));
            logger.info("FINAL CHECKPOINT: " + encodeCheckpoint(lastCheckpointValue));
        }
        return lastCheckpointValue;
    }

    /**
     * Encode the parameters to make it look opaque (not intended in any way
     * to protect the value...just make it easier to use as a command line
     * argument
     * @param value
     * @return
     */
    private String encodeCheckpoint(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode the checkpoint value provided as a parameter
     * @param paramValue
     * @return
     */
    private String decodeCheckpoint(String paramValue) {
        return new String(Base64.getDecoder().decode(paramValue), StandardCharsets.UTF_8);
    }
    /**
     * Allow some time for any outstanding work to complete
     */
    private void drain() {
        long endTime = System.nanoTime() + drainForSeconds * 1000000000l; 
        while (System.nanoTime() < endTime && !isChangeTrackerEmpty()) {
            ThreadHandler.safeSleep(1000);
        }
    }

    /**
     * Returns true if the changeTrackerMap is empty
     * @return
     */
    private boolean isChangeTrackerEmpty() {
        return this.tracker.isEmpty();
    }
    /**
     * Perform one whole-system history call
     */
    private void fetch() {
        StringBuilder request = new StringBuilder();
        request.append("_history?");
        
        if (this.nextLinkParameters == null) {
            // Initial request
            request.append("_sort=none");
            request.append("&_count=").append(this.count);
            if (this.excludeTransactionWindow) {
                // IBM FHIR Server extension to delay receiving changes within the
                // transaction timeout window to avoid potentially ordering issues
                // which could cause data loss
                request.append("&_excludeTransactionTimeoutWindow=true");
            }
        } else {
            // TODO extract all parameters and separate the pagination params
            // following the `next` link
            request.append(this.nextLinkParameters);
        }
        
        // Make the request and process the result
        RequestOptions.Builder requestOptions = RequestOptions.builder();
        if (this.preferReturnMinimal) {
            // ask the upstream server to only return meta-data related to the history
            // entry, not the resource itself. The resource can be fetched via an
            // asynchronous VREAD, which can be done concurrently and should support
            // greater throughput.
            requestOptions.header("Prefer", "return=minimal");
            logger.info("FETCH MINIMAL: " + request.toString());
        } else {
            logger.info("FETCH: " + request.toString());
        }
        FhirServerResponse response = client.get(request.toString(), requestOptions.build());
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            Resource r = response.getResource();
            if (r instanceof Bundle) {
                Bundle bundle = r.as(Bundle.class);
                processBundle(bundle);
                this.nextLinkParameters = getNextParametersFromBundle(bundle);
            } else {
                logger.warning("Expected a Bundle, but got this: " + r.toString());
            }
        } else {
            logger.warning("FETCH failed: [" + response.getStatusCode() + "] " 
                    + response.getStatusMessage());
        }
    }

    /**
     * Update the changeIdMarker value from the bundle next link
     * @param bundle
     */
    private String getNextParametersFromBundle(Bundle bundle) {
        for (Bundle.Link link: bundle.getLink()) {
            if (link.getRelation() != null && "next".equals(link.getRelation().getValue())) {
                if (link.getUrl() != null && link.getUrl().getValue() != null) {
                    return getParamStringFromUrl(link.getUrl().getValue());
                } else {
                    throw new IllegalArgumentException("Bundle next link did not contain a url");
                }
            }
        }

        // If we don't get a next link, it probably means there's no data yet in the system
        return null;
    }

    /**
     * Extract the changeIdMarker value from the url, which might look something like this:
     *   - https://localhost:9443/fhir-server/api/v4/_history?_count=1000&_sort=none&_changeIdMarker=2048&_excludeTransactionTimeoutWindow=true
     *   - https://localhost:9443/fhir-server/api/v4/_history?_count=1000&_changeIdMarker=2048&_sort=none&_excludeTransactionTimeoutWindow=true
     * @param url
     */
    private String getParamStringFromUrl(String url) {
        // extract the parameters from the next link
        int index = url.indexOf('?');
        if (index >= 0 && index < url.length()-2) {
            return url.substring(index+1);
        }
        throw new IllegalArgumentException("url not a valid next link");
    }

    /**
     * Process the response bundle from the whole-system history call
     * @param bundle
     */
    private void processBundle(Bundle bundle) {
        if (bundle.getEntry().isEmpty()) {
            return;
        }

        // Each bundle gets one tracker ticket which is only closed once
        // all the entries within the bundle have been processed.
        final String bundleNextParams = getNextParametersFromBundle(bundle);
        ITrackerTicket ticket = tracker.track(bundleNextParams, bundle.getEntry().size());
        int entryNumber = 0;
        for (Bundle.Entry entry: bundle.getEntry()) {
            // We can't rely on the entry.id (servers do not need to provide it). We use it
            // when available, otherwise make our own. Used to help track requests and
            // correlate with the bundle
            final String entryId = entry.getId() != null ? entry.getId() : "FHIRFLOW_" + Integer.toString(entryNumber);
            switch (entry.getRequest().getMethod().getValueAsEnum()) {
            case DELETE:
                // no value to fetch, so just submit the interaction
                flowWriter.submit(new Delete(entryId, ticket, ResourceIdentifier.from(entry.getFullUrl())));
                break;
            case PUT:
            case POST:
                Resource r = entry.getResource();
                if (r != null) {
                    // If the entry already contains the resource, we don't need to issue a new VREAD
                    // We expect the returned resource to be valid, so it should have an id and meta
                    ResourceIdentifierVersion riv = new ResourceIdentifierVersion(r.getClass().getSimpleName(), r.getId(), r.getMeta().getVersionId().getValue());
                    FlowFetchResult ffr = new FlowFetchResult(riv);
                    ffr.setResource(r);
                    ffr.setStatus(200); // we didn't have to fetch it, so we're calling it OK
                    CompletableFuture<FlowFetchResult> flowFetchFuture = CompletableFuture.completedFuture(ffr);
                    flowWriter.submit(new CreateOrUpdate(entryId, ticket, riv, flowFetchFuture));
                } else {
                    // initiate the request to read the resource. Because we need the version, we have to
                    // use the response.location field
                    Entry.Response response = entry.getResponse();
                    if (response != null) {
                        ResourceIdentifierVersion riv = ResourceIdentifierVersion.from(response.getLocation());
                        CompletableFuture<FlowFetchResult> flowFetchFuture = flowPool.requestResource(riv);
        
                        // and submit the interaction using the future which can be resolved later
                        flowWriter.submit(new CreateOrUpdate(entryId, ticket, riv, flowFetchFuture));
                    } else {
                        throw new IllegalStateException("entry does not include a response: " + entry);
                    }
                }
                break;
            default:
                throw new IllegalStateException("Entry not supported: " + entry);
            }
            entryNumber++;
        }
    }
}