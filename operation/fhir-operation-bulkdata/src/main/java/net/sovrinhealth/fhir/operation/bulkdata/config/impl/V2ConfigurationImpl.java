/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata.config.impl;

import java.util.Collections;
import java.util.List;

import net.sovrinhealth.fhir.config.FHIRConfigHelper;

/**
 * Starting with FHIR Server version 4.6
 */
public class V2ConfigurationImpl extends AbstractSystemConfigurationImpl {
    // the new FHIRPersistenceJDBCBlob implementation is not compatible with fast-export, so we need to watch for that
    private static final String BLOB_PERSISTENCE_FACTORY = "net.sovrinhealth.fhir.persistence.blob.FHIRPersistenceJDBCBlobFactory";

    @Override
    public boolean legacy() {
        return false;
    }

    @Override
    public String getCoreApiBatchUrl() {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/core/api/url", null);
    }

    @Override
    public String getCoreApiBatchUser() {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/core/api/user", null);
    }

    @Override
    public String getCoreApiBatchPassword() {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/core/api/password", null);
    }

    @Override
    public String getCoreApiBatchTrustStore() {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/core/api/truststore", null);
    }

    @Override
    public String getCoreApiBatchTrustStorePassword() {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/core/api/truststorePassword", null);
    }

    @Override
    public String getStorageProviderType(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/type", "ibm-cos");
    }

    @Override
    public List<String> getStorageProviderValidBaseUrls(String provider) {
        final String PATH = "fhirServer/bulkdata/storageProviders/" + provider + "/validBaseUrls";
        List<String> results = FHIRConfigHelper.getStringListProperty(PATH);
        if (results == null) {
            results = Collections.emptyList();
        }
        return results;
    }

    @Override
    public String getStorageProviderBucketName(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/bucketName", null);
    }

    @Override
    public String getStorageProviderLocation(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/location", null);
    }

    @Override
    public String getStorageProviderEndpointInternal(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/endpointInternal", null);
    }

    @Override
    public String getStorageProviderEndpointExternal(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/endpointExternal", null);
    }

    @Override
    public String getStorageProviderAuthType(String provider) {
        String path = "fhirServer/bulkdata/storageProviders/" + provider + "/auth/type";
        return FHIRConfigHelper.getStringProperty(path, null);
    }

    @Override
    public boolean isStorageProviderAuthTypeIam(String provider) {
        String auth = getStorageProviderAuthType(provider);
        return "iam".equalsIgnoreCase(auth);
    }

    @Override
    public String getStorageProviderAuthTypeIamApiKey(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/auth/iamApiKey", null);
    }

    @Override
    public String getStorageProviderAuthTypeIamApiResourceInstanceId(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/auth/iamResourceInstanceId", null);
    }

    @Override
    public boolean isStorageProviderAuthTypeHmac(String provider) {
        String auth = getStorageProviderAuthType(provider);
        return "hmac".equalsIgnoreCase(auth);
    }

    @Override
    public String getStorageProviderAuthTypeHmacAccessKey(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/auth/accessKeyId", null);
    }

    @Override
    public String getStorageProviderAuthTypeHmacSecretKey(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/auth/secretAccessKey", null);
    }

    @Override
    public boolean isStorageProviderAuthTypeBasic(String provider) {
        String auth = getStorageProviderAuthType(provider);
        return "basic".equalsIgnoreCase(auth);
    }

    @Override
    public String getStorageProviderAuthTypeUsername(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/auth/username", null);
    }

    @Override
    public String getStorageProviderAuthTypePassword(String provider) {
        return FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/storageProviders/" + provider + "/auth/password", null);
    }

    @Override
    public boolean shouldStorageProviderValidateBaseUrl(String provider) {
        return FHIRConfigHelper.getBooleanProperty("fhirServer/bulkdata/storageProviders/" + provider + "/disableBaseUrlValidation", Boolean.FALSE);
    }

    @Override
    public boolean shouldStorageProviderCollectOperationOutcomes(String provider) {
        // 2910: Switches the default to TRUE meaning we don't record them.
        return !FHIRConfigHelper.getBooleanProperty(
                "fhirServer/bulkdata/storageProviders/" + provider + "/disableOperationOutcomes", Boolean.TRUE);
    }

    @Override
    public boolean shouldStorageProviderCheckDuplicate(String provider) {
        return FHIRConfigHelper.getBooleanProperty("fhirServer/bulkdata/storageProviders/" + provider + "/duplicationCheck", Boolean.TRUE);
    }

    @Override
    public boolean shouldStorageProviderValidateResources(String provider) {
        return FHIRConfigHelper.getBooleanProperty("fhirServer/bulkdata/storageProviders/" + provider + "/validateResources", Boolean.TRUE);
    }

    @Override
    public boolean shouldStorageProviderCreate(String provider) {
        return FHIRConfigHelper.getBooleanProperty("fhirServer/bulkdata/storageProviders/" + provider + "/create", Boolean.FALSE);
    }

    @Override
    public boolean isFastExport() {
        String persistenceFactoryClassname = FHIRConfigHelper.getStringProperty("fhirServer/persistence/factoryClassname", "");
        String type = FHIRConfigHelper.getStringProperty("fhirServer/bulkdata/core/systemExportImpl", "fast");
        return !BLOB_PERSISTENCE_FACTORY.equals(persistenceFactoryClassname) && "fast".equals(type);
    }

    @Override
    public boolean getStorageProviderUsesRequestAccessToken(String provider) {
        return FHIRConfigHelper.getBooleanProperty("fhirServer/bulkdata/storageProviders/" + provider + "/requiresAccessToken", Boolean.FALSE)
                && !isStorageProviderHmacPresigned(provider);
    }

    @Override
    public boolean shouldStorageProviderAllowAllResources(String provider) {
        return FHIRConfigHelper.getBooleanProperty("fhirServer/bulkdata/storageProviders/" + provider + "/allowAllResources", Boolean.FALSE);
    }
}