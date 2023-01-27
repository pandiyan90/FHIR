/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.blob;

import net.sovrinhealth.fhir.persistence.jdbc.dao.api.IResourceTypeMaps;

/**
 * Representation of a blob name broken down into its individual elements
 * but with slightly more sophisticated handling so that we can use either
 * the real resource name or the resource type id when creating a path. Note
 * that the resource-name version of the path is only used for log/user messages.
 * The real blob path used as the key for the blob service always uses
 * resource type id as the first field.
 */
public class BlobName {
    private final String resourceTypeName;
    private final int resourceTypeId;
    private final String logicalId;
    private final int version; // can be 0 (if partial)
    private final String resourcePayloadKey; // can be null

    /**
     * Private constructor used by the {@link Builder}
     * @param builder
     */
    private BlobName(Builder builder) {
        this.resourceTypeName = builder.resourceTypeName;
        this.resourceTypeId = builder.resourceTypeId;
        this.logicalId = builder.logicalId;
        this.version = builder.version;
        this.resourcePayloadKey = builder.resourcePayloadKey;
    }

    /**
     * Getter for the resourceTypeId
     * @return
     */
    public int getResourceTypeId() {
        return this.resourceTypeId;
    }

    /**
     * Return the path using the resourceTypeId value. This can be
     * used to retrieve the blob, or list blobs if the resourcePayloadKey
     * is not set.
     * @return
     */
    public String toBlobPath() {
        StringBuilder result = new StringBuilder();
        result.append(resourceTypeId);
        result.append("/");
        result.append(BlobPayloadSupport.encodeLogicalId(logicalId));
        result.append("/");
        if (version > 0) {
            // note...if we don't add the version here, then the
            // trailing / is still required because this value will be used
            // as a prefix
            result.append(version);
            result.append("/");
            if (this.resourcePayloadKey != null) {
                // note...if we don't add the resourcePayloadKey here, then the
                // trailing / is still required because this value will be used
                // as a prefix
                result.append(resourcePayloadKey);
            }
        }
        return result.toString();
    }

    /**
     * Return the path using the resourceTypeName value if it is available, otherwise
     * use resourceTypeId. This is only used for informational/debug messages, not
     * for interacting with the Azure Blob service.
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (resourceTypeName != null) {
            result.append(resourceTypeName);
        } else {
            result.append(resourceTypeId);
        }
        result.append("/");
        result.append(logicalId); // no need to encode for messages
        result.append("/");
        if (version > 0) {
            result.append(version);
            result.append("/");
            if (this.resourcePayloadKey != null) {
                result.append(resourcePayloadKey);
            }
        }
        return result.toString();
    }

    /**
     * Is this a partial name? Partial names do not have a version or resourcePayloadKey and represent only
     * a prefix of the path in the blob store
     * @return
     */
    public boolean isPartial() {
        return this.resourcePayloadKey == null || this.version < 1;
    }

    /**
     * Builder for creating {@link BlobName} instances
     */
    public static class Builder {
        private String resourceTypeName;
        private int resourceTypeId = -1;
        private String logicalId;
        private int version;
        private String resourcePayloadKey;

        /**
         * Public default constructor
         */
        public Builder() {
        }

        /**
         * Set the resource type name and lookup the resource type id
         * @param value
         * @return this instance of Builder
         */
        public Builder resourceTypeName(String value) {
            this.resourceTypeName = value;
            return this;
        }

        /**
         * Set the resource type id and lookup the resource type name
         * @param id
         * @return this instance of Builder
         */
        public Builder resourceTypeId(int id) {
            this.resourceTypeId = id;
            return this;
        }

        /**
         * Set the logicalId
         * @param id
         * @return this instance of Builder
         */
        public Builder logicalId(String id) {
            this.logicalId = id;
            return this;
        }

        /**
         * Set the version
         * @param version
         * @return this instance of Builder
         */
        public Builder version(int version) {
            this.version = version;
            return this;
        }

        /**
         * Set the resourcePayloadKey value
         * @param key
         * @return this instance of Builder
         */
        public Builder resourcePayloadKey(String key) {
            this.resourcePayloadKey = key;
            return this;
        }

        /**
         * Build a BlobName from the current state of this Builder
         * @return
         */
        public BlobName build() {
            if (this.resourceTypeName == null && this.resourceTypeId < 0) {
                throw new IllegalStateException("No resource type");
            }

            if (this.logicalId == null) {
                throw new IllegalStateException("No logicalId");
            }

            // version and resourcePayloadKey can be unset
            return new BlobName(this);
        }
    }

    /**
     * Factory method to create a new {@link Builder} instance
     * @param resourceTypeMaps
     * @return
     */
    public static BlobName.Builder builder() {
        return new BlobName.Builder();
    }

    /**
     * Build a blob name by parsing components of the blob path and interpreting the
     * resource type as either an id or name
     * @param blobPath
     * @return
     */
    public static BlobName create(IResourceTypeMaps resourceTypeMaps, String blobPath) {
        String[] parts = blobPath.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("blobPath must contain at least 2 parts: resourceType/logicalId");
        }
        if (parts.length > 4) {
            throw new IllegalArgumentException("blobPath must contain no more than 4 parts: resourceType/logicalId/version/resourcePayloadKey");            
        }
        BlobName.Builder builder = BlobName.builder();
        try {
            final int resourceTypeId = Integer.parseInt(parts[0]);
            builder.resourceTypeId(resourceTypeId);
            if (resourceTypeMaps != null) {
                final String resourceTypeName = resourceTypeMaps.getResourceTypeName(resourceTypeId);
                builder.resourceTypeName(resourceTypeName);
            }
        } catch (NumberFormatException x) {
            // that's OK. This must be a resourceTypeName
            final String resourceTypeName = parts[0];
            builder.resourceTypeName(resourceTypeName);
            if (resourceTypeMaps != null) {
                final int resourceTypeId = resourceTypeMaps.getResourceTypeId(resourceTypeName);
                builder.resourceTypeId(resourceTypeId);                
            }
        }
        builder.logicalId(BlobPayloadSupport.decodeLogicalId(parts[1]));
        // Check if a version was included
        if (parts.length > 2) {
            builder.version(Integer.parseInt(parts[2]));
            if (parts.length == 4) {
                // resourcePayloadKey is optional
                builder.resourcePayloadKey(parts[3]);
            }
        }
        return builder.build();
    }

    /**
     * Create a BlobName but without any mapping between resourceTypeId and
     * resourceTypeName
     * @param blobPath
     * @return
     */
    public static BlobName create(String blobPath) {
        return create(null, blobPath);
    }
}