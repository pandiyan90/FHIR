/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.schema.control;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sovrinhealth.fhir.database.utils.api.IDatabaseSupplier;
import net.sovrinhealth.fhir.database.utils.api.IDatabaseTranslator;
import net.sovrinhealth.fhir.schema.model.ResourceType;

/**
 * Selects the existing RESOURCE_TYPES from DB2
 */
public class GetResourceTypeList implements IDatabaseSupplier<List<ResourceType>> {
    private final String schemaName;

    public GetResourceTypeList(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public List<ResourceType> run(IDatabaseTranslator translator, Connection c) {
        List<ResourceType> result = new ArrayList<>();

        final String SQL = "SELECT resource_type_id, resource_type "
                + "  FROM " + schemaName + ".RESOURCE_TYPES";

        try (Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery(SQL);
            while (rs.next()) {
                ResourceType rt = new ResourceType();
                rt.setId(rs.getInt(1));
                rt.setName(rs.getString(2));
                result.add(rt);
            }
        }
        catch (SQLException x) {
            throw translator.translate(x);
        }

        return result;
    }

}
