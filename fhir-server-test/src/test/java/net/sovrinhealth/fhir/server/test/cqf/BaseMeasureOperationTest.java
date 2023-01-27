/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.server.test.cqf;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.model.resource.CapabilityStatement;
import net.sovrinhealth.fhir.model.test.TestUtil;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Period;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.server.test.FHIRServerTestBase;

import jakarta.json.JsonObject;

public class BaseMeasureOperationTest extends FHIRServerTestBase {
    protected static final String TEST_PATIENT_ID = "Patient/sally-fields";
    protected static final String TEST_MEASURE_ID = "8-auq9X1o0TxTYLhRGlt8ISNC1w7ELS5sKyhmUTj4SE";
    protected static final String TEST_MEASURE_URL = "http://ibm.com/health/Measure/EXM74";
    protected static final String TEST_PERIOD_START = "2010-01-01";
    protected static final String TEST_PERIOD_END = "2010-12-31";

    @BeforeClass
    public void setup() throws Exception {
        CapabilityStatement conf = retrieveConformanceStatement();

        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        EvaluationContext evaluationContext = new EvaluationContext(conf);
        Collection<FHIRPathNode> tmpResults = evaluator.evaluate(evaluationContext, "rest.resource.where(type = 'Measure').operation.name");
        Collection<String> listOfOperations = tmpResults.stream().map(x -> x.getValue().asStringValue().string()).collect(Collectors.toList());
        if( ! listOfOperations.contains("evaluate-measure") ) {
            throw new SkipException("CQF Measure Operations are not enabled");
        }

        Properties testProperties = TestUtil.readTestProperties("test.properties");
        setUp(testProperties);

        JsonObject jsonObject = TestUtil.readJsonObject("testdata/Patient_SallyFields.json");
        Entity<JsonObject> entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = getWebTarget().path("/" + TEST_PATIENT_ID).request().put(entity);
        assertResponse(response, Response.Status.Family.SUCCESSFUL);

        jsonObject = TestUtil.readJsonObject("testdata/Bundle-EXM74-10.2.000.json");
        entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = getWebTarget().request().post( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );

        jsonObject = TestUtil.readJsonObject("testdata/Bundle-ValueSets.json");
        entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = getWebTarget().request().post( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
        
        jsonObject = TestUtil.readJsonObject("testdata/Measure-MissingLibrary.json");
        entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = getWebTarget().path("/Measure/MissingLibrary").request().put( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
        
        jsonObject = TestUtil.readJsonObject("testdata/Measure-NoLibrary.json");
        entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = getWebTarget().path("/Measure/NoLibrary").request().put( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
    }

    public Period getPeriod(String start, String end) {
        // Servers are in UTC
        OffsetDateTime expectedStart = OffsetDateTime.of(LocalDate.parse(start), LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime expectedEnd = OffsetDateTime.of(LocalDate.parse(end), LocalTime.MAX, ZoneOffset.UTC);
        Period expectedPeriod = Period.builder().start(DateTime.of(expectedStart.toZonedDateTime())).end(DateTime.of(expectedEnd.toZonedDateTime())).build();
        return expectedPeriod;
    }
}
