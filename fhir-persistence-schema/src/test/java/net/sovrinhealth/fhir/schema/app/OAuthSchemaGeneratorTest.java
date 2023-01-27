/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.schema.app;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.database.utils.api.ISchemaAdapter;
import net.sovrinhealth.fhir.database.utils.api.SchemaApplyContext;
import net.sovrinhealth.fhir.database.utils.common.JdbcTarget;
import net.sovrinhealth.fhir.database.utils.common.PlainSchemaAdapter;
import net.sovrinhealth.fhir.database.utils.model.PhysicalDataModel;
import net.sovrinhealth.fhir.database.utils.postgres.PostgresAdapter;
import net.sovrinhealth.fhir.database.utils.version.CreateVersionHistory;
import net.sovrinhealth.fhir.database.utils.version.VersionHistoryService;
import net.sovrinhealth.fhir.schema.app.JavaBatchSchemaGeneratorTest.ConfirmTagsVisitor;
import net.sovrinhealth.fhir.schema.app.JavaBatchSchemaGeneratorTest.PrintConnection;
import net.sovrinhealth.fhir.schema.control.OAuthSchemaGenerator;

public class OAuthSchemaGeneratorTest {
    @Test
    public void testOAuthSchemaGeneratorCheckTags() {
        JavaBatchSchemaGeneratorTest test = new JavaBatchSchemaGeneratorTest();
        PrintConnection connection = test.new PrintConnection();
        JdbcTarget target = new JdbcTarget(connection);
        PostgresAdapter adapter = new PostgresAdapter(target);
        ISchemaAdapter schemaAdapter = new PlainSchemaAdapter(adapter);

        // Set up the version history service first if it doesn't yet exist
        CreateVersionHistory.createTableIfNeeded(Main.ADMIN_SCHEMANAME, schemaAdapter);

        // Current version history for the database. This is used by applyWithHistory
        // to determine which updates to apply and to record the new changes as they
        // are applied
        VersionHistoryService vhs = new VersionHistoryService(Main.ADMIN_SCHEMANAME, Main.DATA_SCHEMANAME, Main.OAUTH_SCHEMANAME, Main.BATCH_SCHEMANAME);
        vhs.setTarget(adapter);

        PhysicalDataModel pdm = new PhysicalDataModel();
        OAuthSchemaGenerator generator = new OAuthSchemaGenerator(Main.OAUTH_SCHEMANAME);
        generator.buildOAuthSchema(pdm);
        SchemaApplyContext context = SchemaApplyContext.getDefault();
        pdm.apply(schemaAdapter, context);
        pdm.applyFunctions(schemaAdapter, context);
        pdm.applyProcedures(schemaAdapter, context);

        pdm.visit(new ConfirmTagsVisitor());
    }
}