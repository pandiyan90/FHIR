/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.persistence.jdbc.test.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.config.FHIRConfigProvider;
import net.sovrinhealth.fhir.database.utils.api.IConnectionProvider;
import net.sovrinhealth.fhir.database.utils.derby.DerbyMaster;
import net.sovrinhealth.fhir.database.utils.pool.PoolConnectionProvider;
import net.sovrinhealth.fhir.model.test.TestUtil;
import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.jdbc.FHIRPersistenceJDBCCache;
import net.sovrinhealth.fhir.persistence.jdbc.cache.CommonValuesCacheImpl;
import net.sovrinhealth.fhir.persistence.jdbc.cache.FHIRPersistenceJDBCCacheImpl;
import net.sovrinhealth.fhir.persistence.jdbc.cache.IdNameCache;
import net.sovrinhealth.fhir.persistence.jdbc.cache.LogicalResourceIdentCacheImpl;
import net.sovrinhealth.fhir.persistence.jdbc.cache.NameIdCache;
import net.sovrinhealth.fhir.persistence.jdbc.dao.api.ICommonValuesCache;
import net.sovrinhealth.fhir.persistence.jdbc.dao.api.ILogicalResourceIdentCache;
import net.sovrinhealth.fhir.persistence.jdbc.impl.FHIRPersistenceJDBCImpl;
import net.sovrinhealth.fhir.search.util.SearchHelper;

/**
 * Encapsulates the instantiation of objects needed to support the JDBC persistence tests.
 * If the constructors for these objects change, we only need to modify thir instantiation
 * here instead of every for every concrete test class
 */
public class PersistenceTestSupport {
    private static final Logger logger = Logger.getLogger(PersistenceTestSupport.class.getName());
    private Properties testProps;

    private PoolConnectionProvider connectionPool;

    private FHIRPersistenceJDBCCache cache;

    /**
     * Public constructor
     * @throws Exception
     */
    public PersistenceTestSupport() throws Exception {
        this.testProps = TestUtil.readTestProperties("test.jdbc.properties");
        DerbyInitializer derbyInit;
        String dbDriverName = this.testProps.getProperty("dbDriverName");
        if (dbDriverName != null && dbDriverName.contains("derby")) {
            derbyInit = new DerbyInitializer(this.testProps);
            IConnectionProvider cp = derbyInit.getConnectionProvider(false);
            this.connectionPool = new PoolConnectionProvider(cp, 1);
            ICommonValuesCache rrc = new CommonValuesCacheImpl(100, 100, 100);
            ILogicalResourceIdentCache lric = new LogicalResourceIdentCacheImpl(100);
            cache = new FHIRPersistenceJDBCCacheImpl(new NameIdCache<Integer>(), new IdNameCache<Integer>(), new NameIdCache<Integer>(), rrc, lric);
        }
    }

    /**
     * Return a new FHIRPersistence implementation configured using the connection pool
     * and cache from this object and the given configProvider and searchHelper.
     * @return
     * @throws Exception
     */
    public FHIRPersistence getPersistenceImpl(FHIRConfigProvider configProvider, SearchHelper searchHelper) throws Exception {
        if (this.connectionPool == null) {
            throw new IllegalStateException("Database not bootstrapped");
        }
        return new FHIRPersistenceJDBCImpl(this.testProps, this.connectionPool, configProvider, cache, searchHelper);
    }

    /**
     * Return a new FHIRPersistence implementation configured using the connection pool
     * and cache from this object
     * @return
     * @throws Exception
     */
    public FHIRPersistence getPersistenceImpl() throws Exception {
        if (this.connectionPool == null) {
            throw new IllegalStateException("Database not bootstrapped");
        }
        return new FHIRPersistenceJDBCImpl(this.testProps, this.connectionPool, cache);
    }

    /**
     * Close any resources we may still have open
     */
    public void shutdown() {
        if (this.connectionPool != null) {
            this.connectionPool.close();
        }
    }

    /**
     * Debug locks in the Derby database we're using
     */
    public void debugLocks() {
        // Exception running a query. Let's dump the lock table
        try (Connection c = connectionPool.getConnection()) {
            DerbyMaster.dumpLockInfo(c);
        } catch (SQLException x) {
            // just log the error...things are already bad if this method has been called
            logger.severe("dumpLockInfo - connection failure: " + x.getMessage());
        }
        
    }
}