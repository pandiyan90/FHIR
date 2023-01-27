/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.blob;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.persistence.FHIRPersistenceSupport;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;

/**
 * DAO command to store the configured payload in the Azure blob
 */
public class BlobReadPayload {
    private static final Logger logger = Logger.getLogger(BlobReadPayload.class.getName());
    private final int resourceTypeId;
    private final String logicalId;
    private final int version;
    private final String resourcePayloadKey;
    private final List<String> elements;
    private final boolean compress;

    /**
     * Public constructor
     * @param resourceTypeId
     * @param logicalId
     * @param version
     * @param resourcePayloadKey
     * @param elements
     * @param compress
     */
    public BlobReadPayload(int resourceTypeId, String logicalId, int version, String resourcePayloadKey, List<String> elements, boolean compress) {
        this.resourceTypeId = resourceTypeId;
        this.logicalId = logicalId;
        this.version = version;
        this.resourcePayloadKey = resourcePayloadKey;
        this.elements = elements;
        this.compress = compress;
    }

    /**
     * Parse the downloaded content and transform into a FHIR resource
     * @param <T>
     * @param resourceType
     * @param data
     * @return
     */
    private static <T extends Resource> T transform(Class<T> resourceType, BinaryData data, List<String> elements) {
        try {
            return FHIRPersistenceSupport.parse(resourceType, data.toStream(), elements, false);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Execute this command against the given client
     * @param resourceType
     * @param client
     * @throws FHIRPersistenceException
     */
    public <T extends Resource> CompletableFuture<T> run(Class<T> resourceType, BlobManagedContainer client) throws FHIRPersistenceException {
        final String blobPath = BlobPayloadSupport.getPayloadPath(resourceTypeId, logicalId, version, resourcePayloadKey);
        logger.fine(() -> "Reading payload using storage path: " + blobPath);
        BlobAsyncClient bc = client.getClient().getBlobAsyncClient(blobPath);
        return bc.downloadContent()
                .map(data -> transform(resourceType, data, elements))
                .toFuture();
    }
}