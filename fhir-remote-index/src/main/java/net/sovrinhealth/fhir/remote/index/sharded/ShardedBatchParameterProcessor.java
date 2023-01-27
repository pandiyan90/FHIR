/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.remote.index.sharded;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.index.DateParameter;
import net.sovrinhealth.fhir.persistence.index.LocationParameter;
import net.sovrinhealth.fhir.persistence.index.NumberParameter;
import net.sovrinhealth.fhir.persistence.index.ProfileParameter;
import net.sovrinhealth.fhir.persistence.index.QuantityParameter;
import net.sovrinhealth.fhir.persistence.index.ReferenceParameter;
import net.sovrinhealth.fhir.persistence.index.SecurityParameter;
import net.sovrinhealth.fhir.persistence.index.StringParameter;
import net.sovrinhealth.fhir.persistence.index.TagParameter;
import net.sovrinhealth.fhir.persistence.index.TokenParameter;
import net.sovrinhealth.fhir.persistence.params.api.IBatchParameterProcessor;
import net.sovrinhealth.fhir.persistence.params.model.CodeSystemValue;
import net.sovrinhealth.fhir.persistence.params.model.CommonCanonicalValue;
import net.sovrinhealth.fhir.persistence.params.model.CommonTokenValue;
import net.sovrinhealth.fhir.persistence.params.model.LogicalResourceIdentValue;
import net.sovrinhealth.fhir.persistence.params.model.ParameterNameValue;


/**
 * Processes batched parameters by pushing the values to various
 * JDBC statements based on the distributed (shard_key) variant
 * of the schema
 */
public class ShardedBatchParameterProcessor implements IBatchParameterProcessor {
    private static final Logger logger = Logger.getLogger(ShardedBatchParameterProcessor.class.getName());

    // A cache of the resource-type specific DAOs we've created
    private final Map<String, ShardedPostgresParameterBatch> daoMap = new HashMap<>();

    // Encapculates the statements for inserting whole-system level search params
    private final ShardedPostgresSystemParameterBatch systemDao;

    // Resource types we've touched in the current batch
    private final Set<String> resourceTypesInBatch = new HashSet<>();

    // The database connection this consumer thread is using
    private final Connection connection;

    /**
     * Public constructor
     * @param connection
     */
    public ShardedBatchParameterProcessor(Connection connection) {
        this.connection = connection;
        this.systemDao = new ShardedPostgresSystemParameterBatch(connection);        
    }

    /**
     * Close any resources we're holding to support a cleaner exit
     */
    public void close() {
        for (Map.Entry<String, ShardedPostgresParameterBatch> entry: daoMap.entrySet()) {
            entry.getValue().close();
        }
        systemDao.close();
    }

    /**
     * Start processing a new batch
     */
    public void startBatch() {
        resourceTypesInBatch.clear();
    }

    /**
     * Make sure that each statement that may contain data is cleared before we
     * retry a batch
     */
    public void reset() {
        for (String resourceType: resourceTypesInBatch) {
            ShardedPostgresParameterBatch dao = daoMap.get(resourceType);
            dao.close();
        }
        systemDao.close();
    }
    /**
     * Push any statements that have been batched but not yet executed
     * @throws FHIRPersistenceException
     */
    public void pushBatch() throws FHIRPersistenceException {
        try {
            for (String resourceType: resourceTypesInBatch) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Pushing batch for [" + resourceType + "]");
                }
                ShardedPostgresParameterBatch dao = daoMap.get(resourceType);
                try {
                    dao.pushBatch();
                } catch (SQLException x) {
                    throw new FHIRPersistenceException("pushBatch failed for '" + resourceType + "'");
                }
            }

            try {
                logger.fine("Pushing batch for whole-system parameters");
                systemDao.pushBatch();
            } catch (SQLException x) {
                throw new FHIRPersistenceException("batch insert for whole-system parameters", x);
            }
        } finally {
            // Reset the set of active resource-types ready for the next batch
            resourceTypesInBatch.clear();
        }
    }

    private ShardedPostgresParameterBatch getParameterBatchDao(String resourceType) {
        resourceTypesInBatch.add(resourceType);
        ShardedPostgresParameterBatch dao = daoMap.get(resourceType);
        if (dao == null) {
            dao = new ShardedPostgresParameterBatch(connection, resourceType);
            daoMap.put(resourceType, dao);
        }
        return dao;
    }

    @Override
    public Short encodeShardKey(String requestShard) {
        if (requestShard != null) {
            return Short.valueOf((short)requestShard.hashCode());
        } else {
            return null;
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, StringParameter parameter) throws FHIRPersistenceException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process string parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                + parameter.toString() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addString(logicalResourceId, parameterNameValue.getParameterNameId(), parameter.getValue(), parameter.getValue().toLowerCase(), parameter.getCompositeId(), shardKey);

            if (parameter.isSystemParam()) {
                systemDao.addString(logicalResourceId, parameterNameValue.getParameterNameId(), parameter.getValue(), parameter.getValue().toLowerCase(), parameter.getCompositeId(), shardKey);
            }
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting string params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, NumberParameter p) throws FHIRPersistenceException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process number parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addNumber(logicalResourceId, parameterNameValue.getParameterNameId(), p.getValue(), p.getLowValue(), p.getHighValue(), p.getCompositeId(), shardKey);
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting string params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, QuantityParameter p, CodeSystemValue codeSystemValue) throws FHIRPersistenceException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process quantity parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addQuantity(logicalResourceId, parameterNameValue.getParameterNameId(), codeSystemValue.getCodeSystemId(), p.getValueCode(), p.getValueNumber(), p.getValueNumberLow(), p.getValueNumberHigh(), p.getCompositeId(), shardKey);
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting quantity params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, LocationParameter p) throws FHIRPersistenceException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process location parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addLocation(logicalResourceId, parameterNameValue.getParameterNameId(), p.getValueLatitude(), p.getValueLongitude(), p.getCompositeId(), shardKey);
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting location params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, DateParameter p) throws FHIRPersistenceException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process date parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            final Timestamp valueDateStart = Timestamp.from(p.getValueDateStart());
            final Timestamp valueDateEnd = Timestamp.from(p.getValueDateEnd());
            dao.addDate(logicalResourceId, parameterNameValue.getParameterNameId(), valueDateStart, valueDateEnd, p.getCompositeId(), shardKey);
            if (p.isSystemParam()) {
                systemDao.addDate(logicalResourceId, parameterNameValue.getParameterNameId(), valueDateStart, valueDateEnd, p.getCompositeId(), shardKey);
            }
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting date params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, TokenParameter p,
        CommonTokenValue commonTokenValue) throws FHIRPersistenceException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process token parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "] [" + commonTokenValue.getCommonTokenValueId() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addResourceTokenRef(logicalResourceId, parameterNameValue.getParameterNameId(), commonTokenValue.getCommonTokenValueId(), p.getRefVersionId(), p.getCompositeId(), shardKey);
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting token params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, TagParameter p,
        CommonTokenValue commonTokenValue) throws FHIRPersistenceException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process tag parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "] [" + commonTokenValue.getCommonTokenValueId() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addTag(logicalResourceId, commonTokenValue.getCommonTokenValueId(), shardKey);
            
            if (p.isSystemParam()) {
                systemDao.addTag(logicalResourceId, commonTokenValue.getCommonTokenValueId(), shardKey);
            }
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting tag params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, ProfileParameter p,
        CommonCanonicalValue commonCanonicalValue) throws FHIRPersistenceException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process profile parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "] [" + commonCanonicalValue.getCanonicalId() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addProfile(logicalResourceId, commonCanonicalValue.getCanonicalId(), p.getVersion(), p.getFragment(), shardKey);
            if (p.isSystemParam()) {
                systemDao.addProfile(logicalResourceId, commonCanonicalValue.getCanonicalId(), p.getVersion(), p.getFragment(), shardKey);
            }
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting profile params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, SecurityParameter p,
        CommonTokenValue commonTokenValue) throws FHIRPersistenceException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process security parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + p.toString() + "] [" + commonTokenValue.getCommonTokenValueId() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addSecurity(logicalResourceId, commonTokenValue.getCommonTokenValueId(), shardKey);
            
            if (p.isSystemParam()) {
                systemDao.addSecurity(logicalResourceId, commonTokenValue.getCommonTokenValueId(), shardKey);
            }
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting security params for '" + resourceType + "'");
        }
    }

    @Override
    public void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue,
        ReferenceParameter parameter, LogicalResourceIdentValue refLogicalResourceId) throws FHIRPersistenceException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process ref parameter [" + requestShard + "] [" + resourceType + "] [" + logicalId + "] [" + logicalResourceId + "] [" + parameterNameValue.getParameterName() + "] ["
                    + parameter.toString() + "] [" + refLogicalResourceId.getLogicalResourceId() + "]");
        }

        try {
            ShardedPostgresParameterBatch dao = getParameterBatchDao(resourceType);
            final Short shardKey = encodeShardKey(requestShard);
            dao.addReference(logicalResourceId, parameterNameValue.getParameterNameId(), refLogicalResourceId.getLogicalResourceId(), parameter.getRefVersionId(), shardKey);
        } catch (SQLException x) {
            throw new FHIRPersistenceException("Failed inserting ref param for '" + resourceType + "'");
        }
    }
}
