/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.bucket.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sovrinhealth.fhir.bucket.api.BucketPath;
import net.sovrinhealth.fhir.bucket.api.FileType;
import net.sovrinhealth.fhir.bucket.api.IResourceEntryProcessor;
import net.sovrinhealth.fhir.bucket.client.ClientPropertyAdapter;
import net.sovrinhealth.fhir.bucket.client.FHIRBucketClient;
import net.sovrinhealth.fhir.bucket.client.Headers;
import net.sovrinhealth.fhir.bucket.cos.COSClient;
import net.sovrinhealth.fhir.bucket.interop.BenchWorkload;
import net.sovrinhealth.fhir.bucket.interop.InteropScenario;
import net.sovrinhealth.fhir.bucket.interop.InteropWorkload;
import net.sovrinhealth.fhir.bucket.persistence.FhirBucketSchema;
import net.sovrinhealth.fhir.bucket.persistence.MergeResourceTypes;
import net.sovrinhealth.fhir.bucket.persistence.MergeResourceTypesPostgres;
import net.sovrinhealth.fhir.bucket.reindex.ClientDrivenReindexOperation;
import net.sovrinhealth.fhir.bucket.reindex.DriveReindexOperation;
import net.sovrinhealth.fhir.bucket.reindex.ServerDrivenReindexOperation;
import net.sovrinhealth.fhir.bucket.scanner.BaseFileReader;
import net.sovrinhealth.fhir.bucket.scanner.BundleBreakerResourceProcessor;
import net.sovrinhealth.fhir.bucket.scanner.COSReader;
import net.sovrinhealth.fhir.bucket.scanner.CosScanner;
import net.sovrinhealth.fhir.bucket.scanner.DataAccess;
import net.sovrinhealth.fhir.bucket.scanner.FHIRClientResourceProcessor;
import net.sovrinhealth.fhir.bucket.scanner.IResourceScanner;
import net.sovrinhealth.fhir.bucket.scanner.ImmediateLocalFileReader;
import net.sovrinhealth.fhir.bucket.scanner.LocalFileReader;
import net.sovrinhealth.fhir.bucket.scanner.LocalFileScanner;
import net.sovrinhealth.fhir.bucket.scanner.ResourceHandler;
import net.sovrinhealth.fhir.core.FHIRVersionParam;
import net.sovrinhealth.fhir.core.util.ResourceTypeUtil;
import net.sovrinhealth.fhir.core.util.handler.HostnameHandler;
import net.sovrinhealth.fhir.database.utils.api.IConnectionProvider;
import net.sovrinhealth.fhir.database.utils.api.IDatabaseAdapter;
import net.sovrinhealth.fhir.database.utils.api.IDatabaseTranslator;
import net.sovrinhealth.fhir.database.utils.api.ILeaseManagerConfig;
import net.sovrinhealth.fhir.database.utils.api.ISchemaAdapter;
import net.sovrinhealth.fhir.database.utils.api.ITransaction;
import net.sovrinhealth.fhir.database.utils.api.ITransactionProvider;
import net.sovrinhealth.fhir.database.utils.api.SchemaApplyContext;
import net.sovrinhealth.fhir.database.utils.api.UniqueConstraintViolationException;
import net.sovrinhealth.fhir.database.utils.common.JdbcConnectionProvider;
import net.sovrinhealth.fhir.database.utils.common.PlainSchemaAdapter;
import net.sovrinhealth.fhir.database.utils.derby.DerbyAdapter;
import net.sovrinhealth.fhir.database.utils.derby.DerbyPropertyAdapter;
import net.sovrinhealth.fhir.database.utils.derby.DerbyTranslator;
import net.sovrinhealth.fhir.database.utils.model.DbType;
import net.sovrinhealth.fhir.database.utils.model.PhysicalDataModel;
import net.sovrinhealth.fhir.database.utils.pool.PoolConnectionProvider;
import net.sovrinhealth.fhir.database.utils.postgres.PostgresAdapter;
import net.sovrinhealth.fhir.database.utils.postgres.PostgresPropertyAdapter;
import net.sovrinhealth.fhir.database.utils.postgres.PostgresTranslator;
import net.sovrinhealth.fhir.database.utils.schema.LeaseManager;
import net.sovrinhealth.fhir.database.utils.schema.LeaseManagerConfig;
import net.sovrinhealth.fhir.database.utils.schema.SchemaVersionsManager;
import net.sovrinhealth.fhir.database.utils.transaction.SimpleTransactionProvider;
import net.sovrinhealth.fhir.database.utils.version.CreateControl;
import net.sovrinhealth.fhir.database.utils.version.CreateVersionHistory;
import net.sovrinhealth.fhir.database.utils.version.CreateWholeSchemaVersion;
import net.sovrinhealth.fhir.database.utils.version.VersionHistoryService;
import net.sovrinhealth.fhir.task.api.ITaskCollector;
import net.sovrinhealth.fhir.task.api.ITaskGroup;
import net.sovrinhealth.fhir.task.core.service.TaskService;

/**
 * The fhir-bucket application for loading data from COS into a FHIR server
 * and tracking the returned ids along with response times.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int DEFAULT_CONNECTION_POOL_SIZE = 10;
    private static final int DEFAULT_MAX_FHIR_CONCURRENT_REQUESTS = 40;
    private static final int DEFAULT_COS_SCAN_INTERVAL_MS = 300000; // 5 mins
    private static final String DEFAULT_SCHEMA_NAME = "FHIRBUCKET";
    private final Properties cosProperties = new Properties();
    private final Properties dbProperties = new Properties();
    private final Properties fhirClientProperties = new Properties();

    // The type of database we're talking to
    private DbType dbType;

    // Connection pool used to work alongside the transaction provider
    private PoolConnectionProvider connectionPool;

    // Simple transaction service for use outside of JEE
    private ITransactionProvider transactionProvider;

    // The database schema for all tables used by this application
    private String schemaName = null;

    // Database connection pool size
    private int connectionPoolSize = DEFAULT_CONNECTION_POOL_SIZE;

    // How many JSON files can we process at the same time
    private int maxConcurrentJsonFiles = 10;

    // How many NDJSON files can we process at the same time (typically 1)
    private int maxConcurrentNdJsonFiles = 1;

    // How many FHIR requests should be allowed concurrently
    private int maxConcurrentFhirRequests = DEFAULT_MAX_FHIR_CONCURRENT_REQUESTS;

    // Just slightly over 2 minutes, which is just slightly longer than the FHIR default tx timeout
    private int poolShutdownTimeoutSeconds = 130;

    // By default we want to periodically scan COS looking for new entries
    private boolean runScanner = true;

    // The thread-pool shared by the services for async processing
    private ExecutorService commonPool;

    // Configured connection to IBM Cloud Object Storage (S3)
    private COSClient cosClient;

    // FHIR server requests go through this client
    private FHIRBucketClient fhirClient;

    // The list of buckets to scan for resources to load
    private final List<String> cosBucketList = new ArrayList<>();

    // The translator for the configured database type
    private IDatabaseTranslator translator;

    // The adapter configured for the type of database we're using
    private IDatabaseAdapter adapter;

    // The (plain) schema adapter which wraps the database adapter
    private ISchemaAdapter schemaAdapter;

    // The number of threads to use for the schema creation step
    private int createSchemaThreads = 1;

    private int cosScanIntervalMs = DEFAULT_COS_SCAN_INTERVAL_MS;

    // The resource scanner active object
    private IResourceScanner scanner;

    // The reader handling JSON files
    private BaseFileReader jsonReader;

    // The reader handling NDJSON files (which are processed one at a time
    private BaseFileReader ndJsonReader;

    // A reader which simply scans a local directory, bypassing the database and COS
    private String baseDirectory;
    private ImmediateLocalFileReader immediateLocalFileReader;

    // The active object processing resources read from COS
    private ResourceHandler resourceHandler;

    // The tenant name
    private String tenantName;

    // Set of file types we are interested in
    private Set<FileType> fileTypes = new HashSet<>();

    // optional prefix for scanning a subset of the COS bucket
    private String pathPrefix;

    // Create the schema before the program runs
    private boolean createSchema = false;

    // When true, exit after creating the schema (default behavior unless --bootstrap-schema is given)
    private boolean exitAfterCreatingSchema = true;

    // How many seconds to wait to obtain the schema update lease on the database
    private int waitForUpdateLeaseSeconds = 10;

    // Skip NDJSON rows already processed. Assumes each row is an individual resource or transaction bundle
    private boolean incremental = false;

    // Skip NDJSON rows for which we already have processed and recorded logical ids. Requires a lookup per line
    private boolean incrementalExact = false;

    // optionally reload the same data after this seconds. -1 == do not recycle
    private int recycleSeconds = -1;

    // Assign a higher cost to processing bundles to reduce concurrency and avoid overload/timeouts
    private double bundleCostFactor = 1.0;

    // How many payer scenario requests do we want to make at a time.
    private int concurrentPayerRequests = 0;

    // How many $bench requests do we want to make at a time.
    private int concurrentBenchRequests = 0;

    // Parameter values used for the $bench calls when concurrentBenchRequests > 0
    private int benchThreads = 1;
    private int benchSize = 1;

    // Simple scenario to add some read load to a FHIR server
    private InteropWorkload interopWorkload;

    // Simple CPU load only on the FHIR server
    private BenchWorkload benchWorkload;

    // Special operation to break bundles into bite-sized pieces to avoid tx timeouts. Store new bundles under this bucket and key prefix:
    private String targetBucket;
    private String targetPrefix;

    // The list of bucket-paths we limit reading from
    private List<BucketPath> bucketPaths = new ArrayList<>();

    // How many resources should we pack into new bundles
    private int maxResourcesPerBundle = 100;

    private DriveReindexOperation driveReindexOperation;

    // the _tstamp parameter if we are executing $reindex custom operation calls. Disabled when null
    private String reindexTstampParam;

    // the _resourceCount parameter if we are executing $reindex custom operation calls
    private int reindexResourceCount = 10;

    // How many reindex calls should we run in parallel
    private int reindexConcurrentRequests = 1;

    // Force reindex even if parameter hash matches
    private boolean reindexForce = false;

    // The number of patients to fetch into the buffer
    private int patientBufferSize = 500000;

    // How many times should we cycle through the patient buffer before refilling
    private int bufferRecycleCount = 1;

    // Whether to use client-side-driven reindex, which uses $retrieve-index and $reindex in parallel
    private boolean clientSideDrivenReindex = false;

    // The index ID to start with for client-side-driven reindex. If not specified, it starts from the first index ID that exists
    private String reindexStartWithIndexId;

    // Should we load directly from a dir, without scanning and recording files in the FHIRBUCKET DB
    private boolean isImmediateLocal;

    // Configuration to control how the LeaseManager operates
    private ILeaseManagerConfig leaseManagerConfig;

    /**
     * Parse command line arguments
     * @param args
     */
    public void parseArgs(String[] args) {
        LeaseManagerConfig.Builder lmConfig = LeaseManagerConfig.builder();
        lmConfig.withHost(new HostnameHandler().getHostname());
        lmConfig.withLeaseTimeSeconds(100); // default
        lmConfig.withStayAlive(true);       // default

        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            switch (arg) {
            case "--db-type":
                if (i < args.length + 1) {
                    this.dbType = DbType.from(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --db-type");
                }
                break;
            case "--bootstrap-schema":
                // new behavior - create the schema before main run
                this.createSchema = true;
                this.exitAfterCreatingSchema = false;
                break;
            case "--create-schema":
                // old behavior - just create the schema then exit
                this.createSchema = true;
                this.exitAfterCreatingSchema = true;
                break;
            case "--schema-name":
                if (i < args.length + 1) {
                    this.schemaName = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --schema-name");
                }
                break;
            case "--cos-properties":
                if (i < args.length + 1) {
                    loadCosProperties(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --cos-properties");
                }
                break;
            case "--db-properties":
                if (i < args.length + 1) {
                    loadDbProperties(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --db-properties");
                }
                break;
            case "--db-prop":
                if (i < args.length + 1) {
                    addDbProperty(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --db-properties");
                }
                break;
            case "--fhir-properties":
                if (i < args.length + 1) {
                    loadFhirClientProperties(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --fhir-properties");
                }
                break;
            case "--bucket":
                if (i < args.length + 1) {
                    this.cosBucketList.add(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --bucket");
                }
                break;
            case "--scan-local-dir":
                if (i < args.length + 1) {
                    this.baseDirectory = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --scan-local-dir");
                }
                break;
            case "--file-type":
                if (i < args.length + 1) {
                    this.fileTypes.add(FileType.valueOf(args[++i]));
                } else {
                    throw new IllegalArgumentException("missing value for --file-type");
                }
                break;
            case "--cos-scan-interval-ms":
                if (i < args.length + 1) {
                    this.cosScanIntervalMs = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --cos-scan-interval-ms");
                }
                break;
            case "--recycle-seconds":
                if (i < args.length + 1) {
                    this.recycleSeconds = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --recycle-seconds");
                }
                break;
            case "--max-concurrent-fhir-requests":
                if (i < args.length + 1) {
                    this.maxConcurrentFhirRequests = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --max-concurrent-fhir-requests");
                }
                break;
            case "--max-concurrent-json-files":
                if (i < args.length + 1) {
                    this.maxConcurrentJsonFiles = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --max-concurrent-json-files");
                }
                break;
            case "--max-concurrent-ndjson-files":
                if (i < args.length + 1) {
                    this.maxConcurrentNdJsonFiles = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --max-concurrent-ndjson-files");
                }
                break;
            case "--connection-pool-size":
                if (i < args.length + 1) {
                    this.connectionPoolSize = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --connection-pool-size");
                }
                break;
            case "--concurrent-payer-requests":
                if (i < args.length + 1) {
                    this.concurrentPayerRequests = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --concurrent-payer-requests");
                }
                break;
            case "--concurrent-bench-requests":
                if (i < args.length + 1) {
                    this.concurrentBenchRequests = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --concurrent-bench-requests");
                }
                break;
            case "--bench-threads":
                if (i < args.length + 1) {
                    this.benchThreads = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --bench-threads");
                }
                break;
            case "--bench-size":
                if (i < args.length + 1) {
                    this.benchSize = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --bench-size");
                }
                break;
            case "--pool-shutdown-timeout-seconds":
                if (i < args.length + 1) {
                    this.poolShutdownTimeoutSeconds = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --resource-pool-shutdown-timeout-seconds");
                }
                break;
            case "--bundle-cost-factor":
                if (i < args.length + 1) {
                    this.bundleCostFactor = Double.parseDouble(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --bundle-cost-factor");
                }
                break;
            case "--tenant-name":
                if (i < args.length + 1) {
                    this.tenantName = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --tenant-name");
                }
                break;
            case "--path-prefix":
                if (i < args.length + 1) {
                    this.pathPrefix = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --path-prefix");
                }
                break;
            case "--target-bucket":
                if (i < args.length + 1) {
                    this.targetBucket = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --target-bucket");
                }
                break;
            case "--target-prefix":
                if (i < args.length + 1) {
                    this.targetPrefix = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --target-prefix");
                }
                break;
            case "--bucket-path":
                if (i < args.length + 1) {
                    addBucketPath(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --bucket-path");
                }
                break;
            case "--max-resources-per-bundle":
                if (i < args.length + 1) {
                    this.maxResourcesPerBundle = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --max-resources-per-bundle");
                }
                break;
            case "--patient-buffer-size":
                if (i < args.length + 1) {
                    this.patientBufferSize = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --patient-buffer-size");
                }
                break;
            case "--buffer-recycle-count":
                if (i < args.length + 1) {
                    this.bufferRecycleCount = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --buffer-recycle-count");
                }
                break;
            case "--immediate-local":
                this.isImmediateLocal = true;
                break;
            case "--incremental":
                this.incremental = true;
                break;
            case "--incremental-exact":
                this.incrementalExact = true;
                break;
            case "--no-scan":
                this.runScanner = false;
                break;
            case "--reindex-tstamp":
                if (i < args.length + 1) {
                    this.reindexTstampParam = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --reindex-tstamp");
                }
                break;
            case "--reindex-resource-count":
                if (i < args.length + 1) {
                    this.reindexResourceCount = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --reindex-resource-count");
                }
                break;
            case "--reindex-force":
                this.reindexForce = true;
                break;
            case "--reindex-concurrent-requests":
                if (i < args.length + 1) {
                    this.reindexConcurrentRequests = Integer.parseInt(args[++i]);
                } else {
                    throw new IllegalArgumentException("missing value for --reindex-concurrent-requests");
                }
                break;
            case "--reindex-client-side-driven":
                this.clientSideDrivenReindex = true;
                break;
            case "--reindex-start-with-index-id":
                if (i < args.length + 1) {
                    this.reindexStartWithIndexId = args[++i];
                } else {
                    throw new IllegalArgumentException("missing value for --reindex-start-with-index-id");
                }
                break;
            default:
                throw new IllegalArgumentException("Bad arg: " + arg);
            }
        }
        this.leaseManagerConfig = lmConfig.build();
    }

    /**
     * Add the bucket-name/path-prefix pair to the list we use for filtering
     * @param arg bucket-path specified as <bucket-name>:<path-prefix>
     */
    private void addBucketPath(String arg) {
        String[] values = arg.split(":");
        if (values.length != 2) {
            throw new IllegalArgumentException("Bad bucket path. Bucket paths must be specific as <bucket-name>:<path-prefix>");
        }

        this.bucketPaths.add(new BucketPath(values[0], values[1]));
    }

    /**
     * Load COS properties from the given properties file
     * @param filename
     */
    protected void loadCosProperties(String filename) {
        try (InputStream is = new FileInputStream(filename)) {
            cosProperties.load(is);
        } catch (IOException x) {
            throw new IllegalArgumentException(x);
        }
    }

    /**
     * Load the FHIR client properties from the given properties file
     * @param filename
     */
    protected void loadFhirClientProperties(String filename) {
        try (InputStream is = new FileInputStream(filename)) {
            fhirClientProperties.load(is);
        } catch (IOException x) {
            throw new IllegalArgumentException(x);
        }
    }

    /**
     * Load DB properties from the given properties file
     * @param filename
     */
    protected void loadDbProperties(String filename) {
        try (InputStream is = new FileInputStream(filename)) {
            dbProperties.load(is);
        } catch (IOException x) {
            throw new IllegalArgumentException(x);
        }
    }

    /**
     * Add the property from the arg given in the form of:
     *   param=value
     * @param arg
     */
    protected void addDbProperty(String arg) {
        String props[] = arg.split("=");
        if (props.length == 2) {
            dbProperties.put(props[0], props[1]);
        } else {
            logger.warning("Invalid property value: " + arg);
        }
    }

    /**
     * Rudimentary check of the configuration to make sure the
     * basics have been provided
     */
    public void checkConfig() {

        // If we have a COS configuration, then we also need a database configuration
        if (this.createSchema || !cosProperties.isEmpty()) {
            if (dbType == null) {
                throw new IllegalArgumentException("No --db-type given");
            }

            if (dbProperties.isEmpty()) {
                throw new IllegalArgumentException("No database properties");
            }
        }

        if (!this.createSchema && fhirClientProperties.isEmpty()) {
            // always need FHIR properties, unless we're in create schema mode
            throw new IllegalArgumentException("No FHIR properties");
        }
    }

    /**
     * Set up the database configuration we are going to use to coordinate
     * loading activities. Only one instance should be performing the schema
     * update, so we need a special table which can be used to serialize
     * the schema update process and avoid race conditions.
     */
    public void configure() {

        if (fileTypes.isEmpty()) {
            // use NDJSON if the user didn't provide their own choice
            this.fileTypes.add(FileType.NDJSON);
        }

        // Having a database is now optional, because we don't need access to the FHIRBUCKET tables
        // if we're only running the client-load or reindex helper modes
        if (this.dbType != null) {
            switch (this.dbType) {
            case DERBY:
                setupDerbyRepository();
                break;
            case POSTGRESQL:
            case CITUS:
                setupPostgresRepository();
                break;
            }
        }

        if (cosProperties != null && cosProperties.size() > 0) {
            cosClient = new COSClient(cosProperties);
        }

        // We constrain the number of concurrent tasks which are inflight, so a breathable
        // pool works nicely because it cannot grow unbounded
        this.commonPool = Executors.newCachedThreadPool();
    }

    /**
     * Set up the connection pool and transaction provider for connecting to a Derby
     * database
     */
    public void setupDerbyRepository() {
        if (schemaName == null) {
            // use the default schema for Derby
            schemaName = "APP";
        }

        DerbyPropertyAdapter propertyAdapter = new DerbyPropertyAdapter(dbProperties);
        this.translator = new DerbyTranslator();
        IConnectionProvider cp = new JdbcConnectionProvider(this.translator, propertyAdapter);
        this.connectionPool = new PoolConnectionProvider(cp, connectionPoolSize);
        this.connectionPool.setCloseOnAnyError();
        this.adapter = new DerbyAdapter(connectionPool);
        this.schemaAdapter = new PlainSchemaAdapter(adapter);
        this.transactionProvider = new SimpleTransactionProvider(connectionPool);
    }

    /**
     * Set up the connection pool and transaction provider for connecting to a PostgreSQL
     * database
     */
    public void setupPostgresRepository() {
        if (schemaName == null) {
            schemaName = DEFAULT_SCHEMA_NAME;
        }

        this.translator = new PostgresTranslator();
        try {
            Class.forName(translator.getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        PostgresPropertyAdapter propertyAdapter = new PostgresPropertyAdapter(dbProperties);
        IConnectionProvider cp = new JdbcConnectionProvider(translator, propertyAdapter);
        this.connectionPool = new PoolConnectionProvider(cp, connectionPoolSize);
        this.adapter = new PostgresAdapter(connectionPool);
        this.schemaAdapter = new PlainSchemaAdapter(adapter);
        this.transactionProvider = new SimpleTransactionProvider(connectionPool);
    }

    /**
     * Create the version history table and a simple service which is used to
     * access information from it.
     *
     * @throws SQLException
     */
    protected VersionHistoryService createVersionHistoryService() {
        if (this.adapter == null) {
            throw new IllegalStateException("Database adapter not configured");
        }

        // Create the version history table if it doesn't yet exist
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                CreateVersionHistory.createTableIfNeeded(schemaName, this.schemaAdapter);
            } catch (Exception x) {
                logger.log(Level.SEVERE, "failed to create version history table", x);
                tx.setRollbackOnly();
                throw x;
            }
        }

        // Current version history for the data schema.
        VersionHistoryService vhs = new VersionHistoryService(schemaName, schemaName);
        vhs.setTransactionProvider(transactionProvider);
        vhs.setTarget(this.adapter);
        vhs.init();
        return vhs;
    }

    /**
     * Create or update the database schema to the latest definition
     */
    public void bootstrapDb() {
        if (this.adapter == null) {
            throw new IllegalStateException("Database adapter not configured");
        }

        boolean success = false;
        while (!success) {
            try (ITransaction tx = transactionProvider.getTransaction()) {
                try {
                    adapter.createSchema(schemaName);
                    CreateControl.createTableIfNeeded(schemaName, schemaAdapter);
                    CreateWholeSchemaVersion.createTableIfNeeded(schemaName, schemaAdapter);
                    success = true;
                } catch (Exception x) {
                    logger.log(Level.SEVERE, "failed to create schema management tables", x);
                    tx.setRollbackOnly();
                    throw x;
                }
            } catch (UniqueConstraintViolationException x) {
                // Race condition - two or more instances trying to create either the CONTROL or
                // whole schema version table. These are idempotent, so we leave success - false
                // to try again
            }
        }

        // Obtain a lease on the CONTROL table so that only once instance attempts to update
        // the main schema
        LeaseManager leaseManager = new LeaseManager(this.translator, connectionPool, transactionProvider, schemaName, schemaName,
            leaseManagerConfig);

        try {
            if (!leaseManager.waitForLease(waitForUpdateLeaseSeconds)) {
                throw new IllegalStateException("Concurrent update for FHIR data schema: '" + schemaName + "'");
            }

            // Check to see if we have the latest version before processing any updates
            // If our schema is already at the latest version, we can skip a lot of processing
            SchemaVersionsManager svm = new SchemaVersionsManager(translator, connectionPool, transactionProvider, schemaName,
                FhirBucketSchemaVersion.getLatestSchemaVersion().vid());
            if (svm.isSchemaOld()) {
                buildSchema();

                // Update the whole schema version
                svm.updateSchemaVersion();
            } else {
                logger.info("Already at latest version; skipping update for: '" + schemaName + "'");
            }
        } finally {
            leaseManager.cancelLease();
        }
    }

    /**
     * Build the FHIRBUCKET schema, applying updates as needed to bring the schema up
     * to the latest version
     */
    private void buildSchema() {
        // The version history service is used to track schema changes
        // so we know which to apply and which to skip
        VersionHistoryService vhs = createVersionHistoryService();

        // Create the schema in a managed transaction
        FhirBucketSchema schema = new FhirBucketSchema(schemaName);
        PhysicalDataModel pdm = new PhysicalDataModel();
        schema.constructModel(pdm);

        // Use the dependency information in the physical data model to
        // build a task tree which can be executed in parallel, if desired
        TaskService taskService = new TaskService();
        ExecutorService pool = Executors.newFixedThreadPool(this.createSchemaThreads);
        ITaskCollector collector = taskService.makeTaskCollector(pool);
        SchemaApplyContext context = SchemaApplyContext.getDefault();
        pdm.collect(collector, schemaAdapter, context, this.transactionProvider, vhs);

        // FHIR in the hole!
        logger.info("Starting schema updates");
        collector.startAndWait();

        pool.shutdown();

        Collection<ITaskGroup> failedTaskGroups = collector.getFailedTaskGroups();
        if (failedTaskGroups.size() > 0) {
            final String failedStr =
                    failedTaskGroups.stream().map((tg) -> tg.getTaskId()).collect(Collectors.joining(","));
            logger.severe("Schema update [FAILED]: " + failedStr);
            throw new IllegalStateException("Schema update failed");
        } else {
            logger.info("Schema update [SUCCEEDED]");
        }

        // populate the RESOURCE_TYPES table
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                Set<String> resourceTypes = ResourceTypeUtil.getResourceTypesFor(FHIRVersionParam.VERSION_43);

                if (adapter.getTranslator().isFamilyPostgreSQL()) {
                    // Postgres doesn't support batched merges, so we go with a simpler UPSERT
                    MergeResourceTypesPostgres mrt = new MergeResourceTypesPostgres(schemaName, resourceTypes);
                    adapter.runStatement(mrt);
                } else {
                    MergeResourceTypes mrt = new MergeResourceTypes(schemaName, resourceTypes);
                    adapter.runStatement(mrt);
                }
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * Called by the shutdown hook to stop everything in an orderly fashion
     */
    protected void shutdown() {
        // The goal here is to stop the generation of new work and let
        // existing work drain before we completely pull the plug. This
        // hopefully means we don't end up with gaps in the resources
        // processed from an NDJSON file, allowing the --incremental option
        // to be used safely without skipping rows that weren't actually loaded
        logger.info("Stopping all services");

        // First up, signal everything to stop. This is just a notification,
        // we don't block on any of these. Probably would be cleaner using
        // futures here
        if (this.scanner != null) {
            this.scanner.signalStop();
        }

        if (driveReindexOperation != null) {
            driveReindexOperation.signalStop();
        }

        if (interopWorkload != null) {
            interopWorkload.signalStop();
        }

        if (benchWorkload != null) {
            benchWorkload.signalStop();
        }

        if (this.jsonReader != null) {
            this.jsonReader.signalStop();
        }

        if (this.ndJsonReader != null) {
            this.ndJsonReader.signalStop();
        }

        if (this.immediateLocalFileReader != null) {
            this.immediateLocalFileReader.signalStop();
        }

        if (this.resourceHandler != null) {
            this.resourceHandler.signalStop();
        }

        if (this.scanner != null) {
            this.scanner.waitForStop();
        }

        if (driveReindexOperation != null) {
            driveReindexOperation.waitForStop();
        }

        if (interopWorkload != null) {
            interopWorkload.waitForStop();
        }

        if (benchWorkload != null) {
            benchWorkload.waitForStop();
        }

        if (this.jsonReader != null) {
            this.jsonReader.waitForStop();
        }

        if (this.ndJsonReader != null) {
            this.ndJsonReader.waitForStop();
        }

        if (this.immediateLocalFileReader != null) {
            this.immediateLocalFileReader.waitForStop();
        }

        if (this.resourceHandler != null) {
            this.resourceHandler.waitForStop();
        }

        if (fhirClient != null) {
            this.fhirClient.shutdown();
        }

        // Finally we can ask the common thread-pool to close up shop. Typically we
        // should wait for at least as long as the FHIR server transaction timeout
        // so that we don't lose any responses (and therefore fail to record the
        // resource ids in our database.
        this.commonPool.shutdown();
        try {
            this.commonPool.awaitTermination(this.poolShutdownTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException x) {
            logger.warning("Common thread-pool failed to terminate within " + poolShutdownTimeoutSeconds + "s");
        }
        logger.info("All services stopped");
    }

    /**
     * Choose which mode of the program we want to run:
     * - create the schema
     * - drive reindex
     * - scan and load
     */
    public void process() {
        if (this.createSchema) {
            bootstrapDb();
        }

        if (!this.createSchema || !this.exitAfterCreatingSchema) {
            // Set up the shutdown hook to keep things orderly when asked to terminate
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));

            // FHIR client is always needed, unless we're running the bundle-breaker special mode
            if (this.targetBucket == null || this.targetBucket.length() == 0) {
                // Set up the client we use to send requests to the FHIR server
                fhirClient = new FHIRBucketClient(new ClientPropertyAdapter(fhirClientProperties));
                fhirClient.addHeader(Headers.PREFER_HEADER, "return=representation");
                fhirClient.init(this.tenantName);
            }

            if (this.reindexTstampParam != null) {
                doReindex();
            } else {
                scanAndLoad();
            }
            // JVM won't exit until the threads are stopped via the
            // shutdown hook
        }
    }

    /**
     * Start the processing threads and wait until we get told to stop
     */
    protected void scanAndLoad() {
        // DataAccess hides the details of our interactions with the FHIRBUCKET tracking tables
        DataAccess dataAccess = null;
        if (this.adapter != null) {
            dataAccess = new DataAccess(this.adapter, this.transactionProvider, this.schemaName);
            dataAccess.init();
        }

        if (this.runScanner) {
            // scanning is optional, although needs to be run at least once to populate
            // the tracking FHIRBUCKET database with files discovered in COS or a local
            // directory
            startScanner(dataAccess);
        }

        if (this.isImmediateLocal) {
            // immediate loading so we can process without the need for a FHIRBUCKET database
            logger.info("Immediate local file mode");
            if (this.baseDirectory == null || this.baseDirectory.isEmpty()) {
                throw new IllegalArgumentException("Must specify base directory when using --immediate-local mode");
            }
            if (dataAccess != null) {
                // useful tip
                logger.info("FHIRBUCKET DB not required for --immediate-local mode");
            }
            if (fhirClient == null) {
                throw new IllegalArgumentException("FHIR client configuration required");
            }
            final IResourceEntryProcessor resourceEntryProcessor = new FHIRClientResourceProcessor(fhirClient, null);
            this.resourceHandler = new ResourceHandler(this.commonPool, this.maxConcurrentFhirRequests, resourceEntryProcessor);
            Set<FileType> fileTypes = Collections.singleton(FileType.JSON);
            immediateLocalFileReader = new ImmediateLocalFileReader(commonPool, fileTypes, baseDirectory,
                resource -> resourceHandler.process(resource), DEFAULT_CONNECTION_POOL_SIZE,
                bundleCostFactor);
            immediateLocalFileReader.init();
        } else {
            // Use the FHIRBUCKET database to manage which resources we pick up and load
            logger.info("FHIRBUCKET DB tracking mode");
            if (dataAccess == null) {
                // Obviously we need a database in order to fetch the jobs
                throw new IllegalArgumentException("FHIRBUCKET database required for tracking source content");
            }

            if (!this.cosBucketList.isEmpty()) {
                // Process using COS as our source data repository
                if (cosClient == null) {
                    throw new IllegalArgumentException("COS configuration required");
                }

                final IResourceEntryProcessor resourceEntryProcessor;
                if (this.targetBucket != null && this.targetBucket.length() > 0) {
                    // No fhirClient required here...process each resource locally
                    resourceEntryProcessor = new BundleBreakerResourceProcessor(cosClient, this.maxResourcesPerBundle, this.targetBucket, this.targetPrefix);
                } else {
                    // Process resources by sending them to a FHIR server
                    if (fhirClient == null) {
                        throw new IllegalArgumentException("FHIR client configuration required");
                    }
                    resourceEntryProcessor = new FHIRClientResourceProcessor(fhirClient, dataAccess);
                }

                // Set up the handler to process resources as they are read from COS
                // Uses an internal pool to parallelize NDJSON work
                this.resourceHandler = new ResourceHandler(this.commonPool, this.maxConcurrentFhirRequests, resourceEntryProcessor);

                // Set up the COS reader and wire it to the resourceHandler
                if (maxConcurrentJsonFiles > 0) {
                    this.jsonReader = new COSReader(commonPool, FileType.JSON, cosClient,
                        resource -> resourceHandler.process(resource),
                        this.maxConcurrentJsonFiles, dataAccess, incremental, recycleSeconds,
                        incrementalExact, this.bundleCostFactor, bucketPaths);
                    this.jsonReader.init();
                }

                if (maxConcurrentNdJsonFiles > 0) {
                    this.ndJsonReader = new COSReader(commonPool, FileType.NDJSON, cosClient,
                        resource -> resourceHandler.process(resource),
                        this.maxConcurrentNdJsonFiles, dataAccess, incremental, recycleSeconds,
                        incrementalExact, this.bundleCostFactor, bucketPaths);
                    this.ndJsonReader.init();
                }
            } else if (this.baseDirectory != null && !this.baseDirectory.isEmpty()){
                // Process using a local dir as our source data repository
                final IResourceEntryProcessor resourceEntryProcessor = new FHIRClientResourceProcessor(fhirClient, dataAccess);
                this.resourceHandler = new ResourceHandler(this.commonPool, this.maxConcurrentFhirRequests, resourceEntryProcessor);

                if (maxConcurrentJsonFiles > 0) {
                    this.jsonReader = new LocalFileReader(commonPool, FileType.JSON,
                        resource -> resourceHandler.process(resource),
                        this.maxConcurrentJsonFiles, dataAccess, incremental, recycleSeconds,
                        incrementalExact, this.bundleCostFactor, bucketPaths);
                    this.jsonReader.init();
                }

                if (maxConcurrentNdJsonFiles > 0) {
                    this.ndJsonReader = new LocalFileReader(commonPool, FileType.NDJSON,
                        resource -> resourceHandler.process(resource),
                        this.maxConcurrentNdJsonFiles, dataAccess, incremental, recycleSeconds,
                        incrementalExact, this.bundleCostFactor, bucketPaths);
                    this.ndJsonReader.init();
                }
            }

            // Optionally apply a read-based workload to stress the FHIR server and database
            // with random requests for resources
            if (this.concurrentPayerRequests > 0) {
                if (fhirClient == null) {
                    throw new IllegalArgumentException("Interop test workload requires FHIR client configuration");
                }

                // The interop workload uses patient resource ids captured during previous data load runs
                // and stored in the FHIRBUCKET.LOGICAL_RESOURCES table
                InteropScenario scenario = new InteropScenario(this.fhirClient);
                interopWorkload = new InteropWorkload(dataAccess, scenario, concurrentPayerRequests, this.patientBufferSize, this.bufferRecycleCount);
                interopWorkload.init();
            }

            // Optionally start a bench workload to stress the FHIR server CPU
            if (this.concurrentBenchRequests > 0) {
                if (fhirClient == null) {
                    throw new IllegalArgumentException("Bench test workload requires FHIR client configuration");
                }

                benchWorkload = new BenchWorkload(fhirClient, concurrentBenchRequests, this.benchThreads, this.benchSize);
                benchWorkload.init();
            }
        }
    }

    /**
     * Use fhir-bucket to drive the (parallel) reindex process.
     */
    private void doReindex() {
        if (this.clientSideDrivenReindex) {
            this.driveReindexOperation = new ClientDrivenReindexOperation(fhirClient, reindexConcurrentRequests, reindexTstampParam, reindexResourceCount, reindexStartWithIndexId, reindexForce);
        } else {
            this.driveReindexOperation = new ServerDrivenReindexOperation(fhirClient, reindexConcurrentRequests, reindexTstampParam, reindexResourceCount, reindexForce);
        }
        this.driveReindexOperation.init();

    }

    /**
     * Start a scanner to find resources to read and load
     * @param dataAccess
     */
    private void startScanner(DataAccess dataAccess) {
        if (baseDirectory != null) {
            // Use a local file scanner, instead of a COS scanner. Records files in
            // the FHIRBUCKET tracking database. For loading local files directly
            // without using a tracking database, see isImmediateLocal
            List<String> localDirList = Collections.singletonList(this.baseDirectory);
            this.scanner = new LocalFileScanner(localDirList, dataAccess, this.fileTypes, pathPrefix, cosScanIntervalMs);
        } else if (cosClient != null) {
            // Set up the scanner to look for new COS objects and register them in our database
            this.scanner = new CosScanner(cosClient, cosBucketList, dataAccess, this.fileTypes, pathPrefix, cosScanIntervalMs);
        } else {
            throw new IllegalArgumentException("No COS or File scanner configuration. Use --no-scan when scanning is not required");
        }
        scanner.init();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        Main m = new Main();
        m.parseArgs(args);
        m.checkConfig();
        m.configure();
        m.process();
    }
}
