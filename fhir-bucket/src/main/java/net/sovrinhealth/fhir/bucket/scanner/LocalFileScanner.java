/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.bucket.scanner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.bucket.api.CosItem;
import net.sovrinhealth.fhir.bucket.api.FileType;
import net.sovrinhealth.fhir.database.utils.thread.ThreadHandler;

/**
 * Active object to periodically scan a local directory looking for new
 * objects to load
 */
public class LocalFileScanner implements IResourceScanner {
    private static final Logger logger = Logger.getLogger(CosScanner.class.getName());

    // number of nanos per ms
    private static final long NANO_MS = 1000000;

    // in auto scan mode, do not scan more quickly than this (1 minute)
    private static final long MIN_AUTO_SCAN_DELAY = 60000L;

    // regular heartbeats every 5 seconds so we can see if a node has failed
    public static final long HEARTBEAT_INTERVAL_MS = 5000;

    // the list of dirs to scan
    private final List<String> dirs;

    // main thread control flag
    private volatile boolean running = true;

    // Interval between scans of COS looking for new items in the configured buckets
    private long scanIntervalMs;

    // active object thread
    private Thread mainLoopThread;

    // Access to our data layer for persistence
    private final DataAccess dataAccess;

    // Only process files matching these types
    private final Set<FileType> fileTypes;

    // optional prefix to narrow the scan inside the bucket
    private final String pathPrefix;

    // time tracker for regular heartbeats
    private long lastHeartbeatTime = -1;


    /**
     * Public constructor
     * 
     * @param dirs the directories to scan
     * @param dataAccess the data access layer for persisting items discovered during the scan
     * @param fileTypes set of FileType values accepted for processing
     * @param prefix only scan items with this prefix if set
     * @param scanIntervalMs the number of milliseconds between scans. -1 for automatic
     */
    public LocalFileScanner(Collection<String> dirs, DataAccess dataAccess, Set<FileType> fileTypes, String pathPrefix,
        int scanIntervalMs) {
        this.dirs = new ArrayList<>(dirs);
        this.dataAccess = dataAccess;
        this.fileTypes = fileTypes;
        this.pathPrefix = pathPrefix;
        this.scanIntervalMs = scanIntervalMs;
    }

    @Override
    public void init() {
        mainLoopThread = new Thread(new Runnable() {

            @Override
            public void run() {
                mainLoop();
            }

        });
        mainLoopThread.start();
    }


    @Override
    public void signalStop() {
        if (this.running) {
            logger.info("Stopping LocalFileScanner");
            this.running = false;
        }

        this.running = false;
        if (mainLoopThread != null) {
            this.mainLoopThread.interrupt();
        }
    }

    @Override
    public void waitForStop() {
        signalStop();

        logger.info("Waiting for CosScanner to stop");
        if (mainLoopThread != null) {
            try {
                // give it a few seconds to respond
                mainLoopThread.join(5000);
            } catch (InterruptedException x) {
                logger.warning("Main loop thread did not terminate in 5000ms");
            }
        }
        logger.info("CosScanner stopped");
    }

    /**
     * The main loop running inside this active object's thread
     */
    public void mainLoop() {
        long nextScanTime = -1;

        while (this.running) {
            long start = System.nanoTime();

            try {
                heartbeat();

                if (nextScanTime == -1 || start >= nextScanTime) {
                    scan();

                    double elapsed = (System.nanoTime() - start) / 1e9;
                    logger.info(String.format("Scan complete [took %4.1f s]", elapsed));

                    // roughly schedule the next scan. If the configured scan interval is < 0
                    // then we use an automatic calculation which is 10x the amount of time
                    // it took to complete the previous scan
                    long delayMs = scanIntervalMs >= 0 ? scanIntervalMs : Math.max((long)(10L * 1000L * elapsed), MIN_AUTO_SCAN_DELAY);
                    nextScanTime = start + delayMs * NANO_MS;
                }
            } catch (Exception x) {
                // Just catch and log so we don't break the main loop
                logger.severe("Error during COS scan: " + x.getMessage());
            }

            // Heartbeat is supposed to be a fraction of the scan interval (e.g. 5s vs. 30s)
            // so we don't need to bother with calculating different sleep schedules to wake
            // up exactly on time for the next heartbeat/scan.
            if (running) {
                ThreadHandler.safeSleep(HEARTBEAT_INTERVAL_MS);
            }
        }
    }

    /**
     * Perform a scan for each of the configured directories
     */
    protected void scan() {
        for (String dir: this.dirs) {
            scan(dir);
        }
    }

    /**
     * Scan the given directory
     * @param dir
     */
    protected void scan(String dir) {
        try {
            Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                  throws IOException {
                    if (!Files.isDirectory(file)) {
                        addFile(file, attrs.size(), attrs.lastModifiedTime());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException x) {
            throw new IllegalStateException("Failed to scan '" + dir + "'", x);
        }
    }
    
    /**
     * Add the file to the list of scanned objects we've found
     * @param file
     * @param size
     * @param lastModifiedTime
     */
    private void addFile(Path file, long size, FileTime lastModifiedTime) {
        final CosItem ci;
        final Date lastModifiedDate = Date.from(lastModifiedTime.toInstant());
        
        FileType fileType = fileTyper(file.toString());
        if (fileType != FileType.UNKNOWN) {
            ci = new CosItem(":local", file.toString(), size, fileType, "", lastModifiedDate);
            logger.info(() -> "Adding file: " + ci.toString());
            handle(ci);
        } else {
            logger.info("Ignoring unsupported file type: " + file.toString());
        }
    }

    /**
     * Determine the type of the file based on the suffix
     * @param itemName
     * @return
     */
    protected static FileType fileTyper(String itemName) {
        if (itemName.endsWith(".ndjson") || itemName.endsWith(".NDJSON")) {
            return FileType.NDJSON;
        } else if (itemName.endsWith(".json") || itemName.endsWith(".JSON")) {
            return FileType.JSON;
        } else {
            return FileType.UNKNOWN;
        }
    }

    /**
     * Process the item returned by the scan
     * @param item
     */
    protected void handle(CosItem item) {
        // Only process items we recognize and want
        if (fileTypes.contains(item.getFileType())) {
            dataAccess.registerBucketItem(item);
        }

        // keep the heartbeat going within the scan just in case a scan
        // takes a really long time
        heartbeat();
    }

    /**
     * Update the heartbeat on a (reasonably) regular basis to
     * demonstrate this loader instance is still alive
     */
    protected void heartbeat() {
        long now = System.nanoTime();
        long gap = (now - this.lastHeartbeatTime) / NANO_MS;
        if (this.lastHeartbeatTime < 0 || gap > HEARTBEAT_INTERVAL_MS) {
            this.lastHeartbeatTime = now;
            dataAccess.heartbeat();
        }
    }
}
