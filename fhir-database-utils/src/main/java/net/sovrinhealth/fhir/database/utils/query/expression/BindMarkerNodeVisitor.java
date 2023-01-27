/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.query.expression;

import java.math.BigDecimal;
import java.time.Instant;

import net.sovrinhealth.fhir.database.utils.api.DataAccessException;

/**
 * A visitor for processing bind markers
 */
public interface BindMarkerNodeVisitor {

    /**
     * Bind the given value for the idx'th parameter. The idx value
     * starts at 1, matching the semantics of the PreparedStatement setXX API.
     * @param value
     * @throws DataAccessException
     */
    void bindString(String value);

    /**
     * Bind the given value for the idx'th parameter. The idx value
     * starts at 1, matching the semantics of the PreparedStatement setXX API.
     * @param idx
     * @param value
     * @throws DataAccessException
     */
    void bindLong(Long value);

    /**
     * Bind the given value for the idx'th parameter. The idx value
     * starts at 1, matching the semantics of the PreparedStatement setXX API.
     * @param value
     * @throws DataAccessException
     */
    void bindInt(Integer value);

    /**
     * Bind the given value for the idx'th parameter. The idx value
     * starts at 1, matching the semantics of the PreparedStatement setXX API.
     * @param value
     * @throws DataAccessException
     */
    void bindInstant(Instant value);

    /**
     * Bind the given value for the idx'th parameter. The idx value
     * starts at 1, matching the semantics of the PreparedStatement setXX API.
     * @param value
     * @throws DataAccessException
     */
    void bindDouble(Double value);

    /**
     * Bind the given value for the idx'th parameter. The idx value
     * starts at 1, matching the semantics of the PreparedStatement setXX API.
     * @param value
     * @throws DataAccessException
     * @param value
     */
    void bindBigDecimal(BigDecimal value);
}
