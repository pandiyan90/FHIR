/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.schema.control;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import net.sovrinhealth.fhir.database.utils.api.ISchemaAdapter;
import net.sovrinhealth.fhir.database.utils.api.SchemaApplyContext;
import net.sovrinhealth.fhir.database.utils.api.SchemaType;
import net.sovrinhealth.fhir.database.utils.common.PlainSchemaAdapter;
import net.sovrinhealth.fhir.database.utils.common.PrintTarget;
import net.sovrinhealth.fhir.database.utils.model.PhysicalDataModel;
import net.sovrinhealth.fhir.database.utils.postgres.PostgresAdapter;
import net.sovrinhealth.fhir.task.api.ITaskCollector;
import net.sovrinhealth.fhir.task.core.service.TaskService;

/**
 * Tests the parallel build out of the FHIR Schema across multiple threads and connections. 
 */
public class ParallelBuildTest {
    private static final Logger logger = Logger.getLogger(ParallelBuildTest.class.getName());
    private static final String SCHEMA_NAME = "PTNG";
    private static final String ADMIN_SCHEMA_NAME = "FHIR_ADMIN";

    @Test
    public void testParallelTableCreation() {
        logger.info("Testing DB2 parallel schema build");

        // Create an instance of the service and use it to test creation
        // of the FHIR schema
        FhirSchemaGenerator gen = new FhirSchemaGenerator(ADMIN_SCHEMA_NAME, SCHEMA_NAME, SchemaType.PLAIN);
        PhysicalDataModel model = new PhysicalDataModel();
        gen.buildSchema(model);

        VersionHistoryServiceTest vhs = new VersionHistoryServiceTest();

        TaskService taskService = new TaskService();
        ExecutorService pool = Executors.newFixedThreadPool(40);
        ITaskCollector collector = taskService.makeTaskCollector(pool);
        PrintTarget tgt = new PrintTarget(null, logger.isLoggable(Level.FINE));
        PostgresAdapter adapter = new PostgresAdapter(tgt);
        ISchemaAdapter schemaAdapter = new PlainSchemaAdapter(adapter);
        SchemaApplyContext context = SchemaApplyContext.getDefault();
        model.collect(collector, schemaAdapter, context, new TransactionProviderTest(), vhs);

        // FHIR in the hole!
        collector.startAndWait();
    }
}
