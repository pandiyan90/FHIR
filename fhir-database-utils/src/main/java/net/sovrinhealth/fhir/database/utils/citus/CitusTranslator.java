/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package net.sovrinhealth.fhir.database.utils.citus;

import net.sovrinhealth.fhir.database.utils.model.DbType;
import net.sovrinhealth.fhir.database.utils.postgres.PostgresTranslator;


/**
 * IDatabaseTranslator implementation supporting Citus
 */
public class CitusTranslator extends PostgresTranslator {
    @Override
    public DbType getType() {
        return DbType.CITUS;
    }
}
