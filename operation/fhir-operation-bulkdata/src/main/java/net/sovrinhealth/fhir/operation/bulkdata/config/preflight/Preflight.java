/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata.config.preflight;

import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.operation.bulkdata.model.type.StorageDetail;

/**
 * Preflight is designed to sanity check a request prior to executing the bulkdata request.
 */
public interface Preflight {
    /**
     * The preflight execution checks access to the Source,Outcome.
     *
     * @throws FHIROperationException
     */
    void preflight() throws FHIROperationException;

    /**
     * checks the preflight execution for a conditional-write
     * @param write
     * @throws FHIROperationException
     */
    default void preflight(boolean write) throws FHIROperationException {
        preflight();
    }

    /**
     * Checks the storage type is allowed.
     * @param storageDetail
     */
    void checkStorageAllowed(StorageDetail storageDetail) throws FHIROperationException;

    /**
     * checks parquet is enabled for this type
     * @return
     */
    default boolean checkParquet() {
        return false;
    }
}