/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.dao.api;

import java.util.Map;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceDataAccessException;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.jdbc.exception.FHIRPersistenceDBConnectException;

/**
 * This Data Access Object interface defines methods for creating, updating,
 * and retrieving rows in the IBM FHIR Server parameter-related tables.
 */
public interface ParameterDAO extends FHIRDbDAO {

    /**
     * Reads all rows in the Parameter_Names table and returns the data as a Map
     * @return Map<String, Long> - A map containing key=parameter-name, value=parameter-name-id
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    Map<String,Integer> readAllSearchParameterNames() throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;

    /**
     * Reads all rows in the Code_Systems table and returns the data as a Map
     * @return Map<String, Long> - A map containing key=system-name, value=system-id
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    Map<String,Integer> readAllCodeSystems() throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;

    /**
     * Reads the id associated with the name of the passed Parameter from the Parameter_Names table. If the id for the passed name is not present
     * in the database, an id is generated, persisted, and returned.
     * @param String A valid FHIR search  parameter name.
     * @return Integer - the id associated with the name of the passed Parameter.
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    int readOrAddParameterNameId(String parameterName) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;

    /**
     * Read the id for the given parameter name, but do not create a new record if it doesn't exist.
     * @param parameterName
     * @return the id for the parameter name, or null not found
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    Integer readParameterNameId(String parameterName) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;

    /**
     * Reads the id associated with the name of the passed code system name from the Code_Systems table. If the id for the passed system name is not present
     * in the database, an id is generated, persisted, and returned.
     * @param systemName - The name of a FHIR code system.
     * @return Integer - The id associated with the passed code system name.
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    int readOrAddCodeSystemId(String systemName) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;

    /**
     * Read the id for the given code system name, but do not create a new record if it doesn't exist.
     * @param systemName
     * @return Integer - The id associated with the passed code system name, or null if it doesn't exist
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    Integer readCodeSystemId(String systemName) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;


    /**
     * Acquire and return the id associated with the passed parameter name.
     * @param parameterName The name of a valid FHIR search parameter.
     * @return Integer A parameter id.
     * @throws FHIRPersistenceException
     */
    int acquireParameterNameId(String parameterName) throws FHIRPersistenceException;
}
