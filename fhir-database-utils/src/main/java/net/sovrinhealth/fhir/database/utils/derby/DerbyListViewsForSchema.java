/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.derby;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sovrinhealth.fhir.database.utils.api.IDatabaseSupplier;
import net.sovrinhealth.fhir.database.utils.api.IDatabaseTranslator;
import net.sovrinhealth.fhir.database.utils.common.DataDefinitionUtil;
import net.sovrinhealth.fhir.database.utils.common.SchemaInfoObject;
import net.sovrinhealth.fhir.database.utils.common.SchemaInfoObject.Type;

/**
 * DAO to fetch the names of views in the given schema
 */
public class DerbyListViewsForSchema implements IDatabaseSupplier<List<SchemaInfoObject>> {
    
    // The schema of the table
    private final String schemaName;

    /**
     * Public constructor
     * @param schemaName
     */
    public DerbyListViewsForSchema(String schemaName) {
        this.schemaName = DataDefinitionUtil.assertValidName(schemaName);
    }

    @Override
    public List<SchemaInfoObject> run(IDatabaseTranslator translator, Connection c) {
        List<SchemaInfoObject> result = new ArrayList<>();
        // Grab the list of views for the configured schema from the Derby sys catalog
        final String sql = ""
                + "SELECT tables.tablename FROM sys.systables AS tables "
                + "  JOIN sys.sysschemas AS schemas "
                + "    ON (tables.schemaid = schemas.schemaid) "
                + "  JOIN sys.sysviews AS views "
                + "    ON (views.tableid = tables.tableid) "
                + " WHERE schemas.schemaname = ?"
                ;
        
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new SchemaInfoObject(Type.VIEW, rs.getString(1)));
            }
        }
        catch (SQLException x) {
            throw translator.translate(x);
        }
        
        return result;
    }
}