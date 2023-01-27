/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.operation.cqf;

import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.bundle;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.canonical;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.coding;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.concept;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.fhirstring;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.reference;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.valueset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.cql.helpers.ParameterMap;
import net.sovrinhealth.fhir.model.resource.Encounter;
import net.sovrinhealth.fhir.model.resource.Library;
import net.sovrinhealth.fhir.model.resource.Measure;
import net.sovrinhealth.fhir.model.resource.MeasureReport;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Parameters.Parameter;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.resource.Procedure;
import net.sovrinhealth.fhir.model.resource.ValueSet;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Date;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Period;
import net.sovrinhealth.fhir.model.type.Reference;
import net.sovrinhealth.fhir.model.type.code.EncounterStatus;
import net.sovrinhealth.fhir.model.type.code.MeasureReportType;
import net.sovrinhealth.fhir.model.type.code.ProcedureStatus;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

public class MeasureCollectDataOperationTest {
    private MeasureCollectDataOperation operation;
    private SearchHelper searchHelper;

    @BeforeClass
    public void initializeSearchUtil() {
        searchHelper = new SearchHelper();
    }

    @BeforeMethod
    public void setup() {
        operation = new MeasureCollectDataOperation();
    }

    @Test
    public void testDoEvaluationEXM74() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);
        Encounter encounter = Encounter.builder()
                .reasonCode(concept(reason))
                .status(EncounterStatus.FINISHED)
                .clazz(reason)
                .period(Period.builder().start(DateTime.now()).end(DateTime.now()).build())
                .build();

        String procedureCode = "fluoride-application";
        Coding type = coding(codesystem, procedureCode);
        Procedure procedure = Procedure.builder().subject(Reference.builder().reference(fhirstring("Patient/"
                + patient.getId())).build()).code(concept(type)).status(ProcedureStatus.COMPLETED).performed(DateTime.of("2019-03-14")).build();

        List<Measure> measures = TestHelper.getBundleResources("EXM74-10.2.000-request.json", Measure.class);
        assertEquals( measures.size(), 1 );
        Measure measure = measures.get(0);
        String measureURL = canonical(measure.getUrl(), measure.getVersion()).getValue();

        List<Library> fhirLibraries = TestHelper.getBundleResources("EXM74-10.2.000-request.json", Library.class);

        List<String> names = fhirLibraries.stream().map( l -> l.getName().getValue()).collect(Collectors.toList());
        System.out.println(names);

        LocalDate periodStart = LocalDate.of(2000, 1, 1);
        LocalDate periodEnd = periodStart.plus(1, ChronoUnit.YEARS);

        Parameters.Parameter pPeriodStart = Parameters.Parameter.builder().name(fhirstring(MeasureCollectDataOperation.PARAM_IN_PERIOD_START)).value(Date.of(periodStart)).build();
        Parameters.Parameter pPeriodEnd = Parameters.Parameter.builder().name(fhirstring(MeasureCollectDataOperation.PARAM_IN_PERIOD_END)).value(Date.of(periodEnd)).build();
        Parameters.Parameter pReportType = Parameters.Parameter.builder().name(fhirstring(MeasureCollectDataOperation.PARAM_IN_REPORT_TYPE)).value(MeasureReportType.INDIVIDUAL).build();
        Parameters.Parameter pMeasure = Parameters.Parameter.builder().name(fhirstring(MeasureCollectDataOperation.PARAM_IN_MEASURE)).value(fhirstring(measureURL)).build();
        Parameters.Parameter pSubject = Parameters.Parameter.builder().name(fhirstring(MeasureCollectDataOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();

        Parameters parameters = Parameters.builder().parameter(pPeriodStart, pPeriodEnd, pReportType, pMeasure, pSubject).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), eq(patient.getId()))).thenAnswer(x -> TestHelper.asResult(patient));

        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString())).thenReturn( bundle(encounter) );
        when(resourceHelper.doSearch(eq("Procedure"), anyString(), anyString(), any(), anyString())).thenReturn( bundle(procedure) );

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);


            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001", ValueSet.class)).thenReturn( valueset(codesystem, encounterCode) );
            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.125.12.1002", ValueSet.class)).thenReturn( valueset(codesystem, procedureCode) );

            when(mockRegistry.getResource(measureURL, Measure.class)).thenReturn( measure );
            fhirLibraries.stream().forEach( l -> when(mockRegistry.getResource( canonical(l.getUrl(), l.getVersion()).getValue(), Library.class )).thenReturn(l) );

            Parameters result = operation.doInvoke(FHIROperationContext.createResourceTypeOperationContext("collect-data"),
                    Measure.class, null, null, parameters, resourceHelper, searchHelper);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            MeasureReport report = (MeasureReport) resultMap.getSingletonParameter(MeasureCollectDataOperation.PARAM_OUT_MEASURE_REPORT).getResource();
            assertEquals( report.getMeasure().getValue(), measureURL );

            List<Parameter> resources = resultMap.getParameter(MeasureCollectDataOperation.PARAM_OUT_RESOURCE);
            assertEquals(resources.size(), 0);
        }
    }

    @Test
    public void testResolveReferences() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter = Encounter.builder()
                .reasonCode(concept(reason))
                .status(EncounterStatus.FINISHED)
                .clazz(reason)
                .period(Period.builder().start(DateTime.now()).end(DateTime.now()).build())
                .subject( reference(patient) )
                .build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), eq(patient.getId()))).thenAnswer(x -> TestHelper.asResult(patient));

        Parameters.Builder builder = Parameters.builder();
        operation.resolveReferences(encounter, builder, new HashMap<>(), resourceHelper);

        Parameters parameters = builder.build();
        assertEquals(parameters.getParameter().size(), 1);
    }
}
