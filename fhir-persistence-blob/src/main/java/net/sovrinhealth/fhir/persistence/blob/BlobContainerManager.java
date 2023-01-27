/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.blob;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.config.FHIRConfigHelper;
import net.sovrinhealth.fhir.config.FHIRConfiguration;
import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.config.PropertyGroup;
import net.sovrinhealth.fhir.core.lifecycle.EventCallback;
import net.sovrinhealth.fhir.core.lifecycle.EventManager;

import com.azure.core.http.HttpClient;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;

import okhttp3.OkHttpClient;

/**
 * Singleton to abstract and manage Azure Blob containers.
 * Each tenant/datasource gets its own container.
 * 
 * TODO: investigate if BlobContainerClient should be long-lived and shared
 * by multiple threads and how to configure its http client library and executor
 * when running inside Liberty.
 */
public class BlobContainerManager implements EventCallback {
    private static final Logger logger = Logger.getLogger(BlobContainerManager.class.getName());

    // Map holding one container client instance per tenant/datasource
    private final ConcurrentHashMap<TenantDatasourceKey, BlobContainerAsyncClient> connectionMap = new ConcurrentHashMap<>();
    
    // so we can reject future requests when shut down
    private volatile boolean running = true;

    // We provide our own OkHttpClient so we can shut down and exit quickly when done
    private final OkHttpClient okHttpClient;
    /**
     * Singleton pattern safe construction 
     */
    private static class Helper {
        private static BlobContainerManager INSTANCE = new BlobContainerManager();
    }
    
    /**
     * Private constructor
     */
    private BlobContainerManager() {
        // If we use the client created by the Azure Blob SDK, we don't have a way
        // to shut things down which results in the the JVM taking a couple of minutes
        // to exit after the program has completed. By supplying our own, we get to
        // initiate some cleanup on it, and the JVM then exits immediately.
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        this.okHttpClient = httpClientBuilder.build();

        // receive server lifecycle events
        EventManager.register(this);
    }
    
    /**
     * Get the singleton instance of this class
     * @return
     */
    public static BlobContainerManager getInstance() {
        return Helper.INSTANCE;
    }
    
    /**
     * Get the (shared, thread-safe) connection object representing the Azure
     * Blob connection for the current tenant/datasource 
     * (see {@link FHIRRequestContext}).
     * @return
     */
    public static BlobManagedContainer getSessionForTenantDatasource() {
        return BlobContainerManager.getInstance().getOrCreateSession();
    }

    /**
     * Get or create the Azure Blob client for the current
     * tenant/datasource.
     * @return a BlobManagedContainer for the current tenant/datasource
     */
    private BlobManagedContainer getOrCreateSession() {
        if (!running) {
            throw new IllegalStateException("BlobConnectionManager is shut down");
        }
        
        // Connections can be tenant-specific, so find out what tenant we're associated with and use its persistence
        // configuration to obtain the appropriate instance (shared by multiple threads).
        final String tenantId = FHIRRequestContext.get().getTenantId();
        final String dsId = FHIRRequestContext.get().getDataStoreId();
        TenantDatasourceKey key = new TenantDatasourceKey(tenantId, dsId);

        // Get the session for this tenant/datasource, or create a new one if needed
        BlobContainerAsyncClient client = connectionMap.computeIfAbsent(key, tdk -> newConnection(tdk));
        String dsPropertyName = FHIRConfiguration.PROPERTY_PERSISTENCE_PAYLOAD + "/" + key.getDatasourceId();
        BlobPropertyGroupAdapter properties = getPropertyGroupAdapter(dsPropertyName);
        
        return new BlobManagedContainer(client, properties);
    }

    /**
     * Build a new CqlSession object for the tenant/datasource tuple described by key.
     * @param key
     * @return
     */
    private BlobContainerAsyncClient newConnection(TenantDatasourceKey key) {
        String dsPropertyName = FHIRConfiguration.PROPERTY_PERSISTENCE_PAYLOAD + "/" + key.getDatasourceId();
        BlobPropertyGroupAdapter adapter = getPropertyGroupAdapter(dsPropertyName);
        return makeConnection(key, adapter);
    }
 
    /**
     * Check if payload persistence is configured for the current tenant/datasource
     * @return
     */
    public static boolean isPayloadPersistenceConfigured() {
        final String tenantId = FHIRRequestContext.get().getTenantId();
        final String dsId = FHIRRequestContext.get().getDataStoreId();
        TenantDatasourceKey key = new TenantDatasourceKey(tenantId, dsId);
        String dsPropertyName = FHIRConfiguration.PROPERTY_PERSISTENCE_PAYLOAD + "/" + key.getDatasourceId();
        PropertyGroup dsPG = FHIRConfigHelper.getPropertyGroup(dsPropertyName);
        return dsPG != null;
    }

    /**
     * Get a CassandraPropertyGroupAdapter bound to the property group described by
     * the given dsPropertyName path (in fhir-server-config.json).
     * @param dsPropertyName
     * @return
     */
    public BlobPropertyGroupAdapter getPropertyGroupAdapter(String dsPropertyName) {
        
        PropertyGroup dsPG = FHIRConfigHelper.getPropertyGroup(dsPropertyName);
        if (dsPG == null) {
            throw new IllegalStateException("Could not locate configuration property group: " + dsPropertyName);
        }

        try {
            // Get the datasource type (should be "azure.blob" in this case).
            String type = dsPG.getStringProperty("type", null);
            if (type == null) {
                throw new IllegalStateException("Could not locate 'type' property within datasource property group: " + dsPropertyName);
            }
    
            // Confirm that this is an Azure Blob datasource configuration element
            if (!"azure.blob".equals(type)) {
                throw new IllegalStateException("Unsupported 'type' property value within datasource property group: " + type);  
            }
    
            // Get the connection properties
            PropertyGroup connectionProps = dsPG.getPropertyGroup("connectionProperties");
            if (connectionProps == null) {
                throw new IllegalStateException("Could not locate 'connectionProperties' property group within datasource property group: " + dsPropertyName);
            }
            
            return new BlobPropertyGroupAdapter(connectionProps);
        }
        catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
    
    /**
     * Get the BlobContainerClient for the Azure blob endpoint using the configuration
     * described by the {@link BlobPropertyGroupAdapter}.
     * @param key
     * @param adapter
     * @return
     */
    private BlobContainerAsyncClient makeConnection(TenantDatasourceKey key, BlobPropertyGroupAdapter adapter) {
        final String containerName;
        if (adapter.getContainerName() != null) {
            containerName = adapter.getContainerName();
        } else {
            // Fallback option, which can be used as long as the tenant and datasource ids
            // adhere to the restrictions required to container names (alphanum or '-')
            containerName = key.getTenantId().toLowerCase() + "-" + key.getDatasourceId().toLowerCase();
        }

        // Explicitly use the okhttp client so we don't end up with library versioning
        // issues for Netty.
        HttpClient httpClient = new OkHttpAsyncHttpClientBuilder(this.okHttpClient)
                .build();

        BlobServiceVersion serviceVersion = null;
        if (adapter.getServiceVersion() != null) {
            serviceVersion = BlobServiceVersion.valueOf(adapter.getServiceVersion());
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("BlobServiceVersion = " + serviceVersion.getVersion());
            }
        }
                
        BlobContainerAsyncClient blobContainerClient = new BlobContainerClientBuilder()
                .httpClient(httpClient)
                .connectionString(adapter.getConnectionString())
                .containerName(containerName)
                .serviceVersion(serviceVersion)
                .buildAsyncClient();
        
        return blobContainerClient;
    }

    /**
     * Get rid of any sessions we're holding on to
     */
    private void closeAllSessions() {
        // prevent anyone asking for a session
        this.running = false;
        connectionMap.clear();

        // Shut down the OkHttpClient to get a quicker exit
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
        try {
            if (okHttpClient.cache() != null) {
                okHttpClient.cache().close();
            }
        } catch (IOException x) {
            logger.log(Level.WARNING, "OkHttpClient cache", x);
        }
    }

    /**
     * Shut down the service so that we don't try and accept any
     * new work
     */
    public static void shutdown() {
        logger.info("Shutting down Azure Blob connection service");
        getInstance().closeAllSessions();
        logger.info("Shutdown complete");
    }

    @Override
    public void serverReady() {
        // NOP
    }

    @Override
    public void startShutdown() {
        this.running = false;
    }

    @Override
    public void finalShutdown() {
        connectionMap.clear();
    }
}