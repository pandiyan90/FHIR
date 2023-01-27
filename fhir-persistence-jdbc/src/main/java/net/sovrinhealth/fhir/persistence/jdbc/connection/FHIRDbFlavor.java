/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.connection;

import net.sovrinhealth.fhir.database.utils.api.SchemaType;
import net.sovrinhealth.fhir.database.utils.model.DbType;

/**
 * Capabilities supported by the different flavors of database we connect to.
 * The flavor is a combination of the database type (e.g. DB2/Derby etc) and
 * the capabilities of the installed schema
 */
public interface FHIRDbFlavor {

    /**
     * What type of schema is this
     * @return
     */
    public SchemaType getSchemaType();

    /**
     * What type of database is this?
     * @return
     */
    public DbType getType();

    /**
     * Is the dbType from the PostgreSQL family?
     * @return
     */
    public boolean isFamilyPostgreSQL();
}
