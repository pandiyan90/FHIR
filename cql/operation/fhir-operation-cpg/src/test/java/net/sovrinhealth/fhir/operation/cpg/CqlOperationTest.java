/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.operation.cpg;

import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.bundle;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.coding;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.concept;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.fhirboolean;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.fhirstring;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import javax.ws.rs.core.MultivaluedMap;

import org.mockito.MockedStatic;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.cql.Constants;
import net.sovrinhealth.fhir.cql.helpers.LibraryHelper;
import net.sovrinhealth.fhir.cql.helpers.ModelHelper;
import net.sovrinhealth.fhir.cql.helpers.ParameterMap;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Encounter;
import net.sovrinhealth.fhir.model.resource.Endpoint;
import net.sovrinhealth.fhir.model.resource.Library;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Parameters.Parameter;
import net.sovrinhealth.fhir.model.resource.Patient;
import net.sovrinhealth.fhir.model.type.Canonical;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.DateTime;
import net.sovrinhealth.fhir.model.type.Period;
import net.sovrinhealth.fhir.model.type.Url;
import net.sovrinhealth.fhir.model.type.code.BundleType;
import net.sovrinhealth.fhir.model.type.code.EncounterStatus;
import net.sovrinhealth.fhir.model.type.code.EndpointStatus;
import net.sovrinhealth.fhir.model.type.code.IssueType;
import net.sovrinhealth.fhir.registry.FHIRRegistry;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

public class CqlOperationTest extends BaseCqlOperationTest<CqlOperation> {

    @Override
    protected CqlOperation getOperation() {
        return new CqlOperation();
    }

    @Test
    public void testRequestContainsUnsupportedParameters() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter = Encounter.builder()
                .reasonCode(concept(reason))
                .status(EncounterStatus.FINISHED)
                .clazz(reason)
                .period(Period.builder()
                    .start(DateTime.now())
                    .end(DateTime.now())
                    .build())
                .build();

        String expression = "[Encounter] e where e.status = 'finished'";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();

        Parameter pUseServerData = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_USE_SERVER_DATA)).value(fhirboolean(true)).build();

        Bundle data = Bundle.builder().type(BundleType.COLLECTION).build();
        Parameter pData = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DATA)).resource(data).build();

        Parameters prefetchData = Parameters.builder().parameter(Parameter.builder().name("key").value("key-value").build()).build();
        Parameter pPrefetchData = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_PREFETCH_DATA)).resource(prefetchData).build();

        Endpoint endpoint = Endpoint.builder()
                .status(EndpointStatus.ACTIVE)
                .connectionType(Coding.builder()
                    .code(Code.of("hl7-fhir-rest"))
                    .build())
                .payloadType(concept(coding("urn:hl7-org:sdwg:ccda-structuredBody:1.1")))
                .address(Url.of("http://localhost:9443/fhir-server/api/v4"))
                .build();
        Parameter pDataEndpoint = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DATA_ENDPOINT)).resource(endpoint).build();
        Parameter pContentEndpoint = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_CONTENT_ENDPOINT)).resource(endpoint).build();
        Parameter pTerminologyEndpoint = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_TERMINOLOGY_ENDPOINT)).resource(endpoint).build();

        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary, pUseServerData, pData, pPrefetchData, pDataEndpoint, pContentEndpoint, pTerminologyEndpoint).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString())).thenReturn(bundle(encounter));

        // Library fhirHelpers = TestHelper.getTestLibraryResource("FHIRHelpers-4.0.1.json");

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            // when(mockRegistry.getResource(javastring(fhirHelpers.getUrl()), Library.class)).thenReturn(fhirHelpers);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext(null);
            getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper, searchHelper);
            fail("Operation was expected to fail due to unsupported parameters");

        } catch (FHIROperationException fex) {
            assertNotNull(fex.getIssues());
            assertEquals(fex.getIssues().size(), 6);
            assertEquals(fex.getIssues().get(0).getCode().getValue(), IssueType.NOT_SUPPORTED.getValue());
            System.out.println(fex.getMessage());
        }
    }

    @Test
    public void testInlineExpression() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter = Encounter.builder()
                .reasonCode(concept(reason))
                .status(EncounterStatus.FINISHED)
                .clazz(reason)
                .period(Period.builder()
                    .start(DateTime.now())
                    .end(DateTime.now())
                    .build())
                .build();

        String expression = "[Encounter] e where e.status = 'finished'";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pDebug = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DEBUG)).value(fhirboolean(true)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary, pDebug).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString())).thenReturn(bundle(encounter));

        // Library fhirHelpers = TestHelper.getTestLibraryResource("FHIRHelpers-4.0.1.json");

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            // when(mockRegistry.getResource(javastring(fhirHelpers.getUrl()), Library.class)).thenReturn(fhirHelpers);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext(null);
            Parameters result = getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper, searchHelper);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            assertNotNull(resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_RETURN));

        }
    }

    @Test
    public void testInlineExpressionUsesResourceId() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter = Encounter.builder()
                .reasonCode(concept(reason))
                .status(EncounterStatus.FINISHED)
                .clazz(reason)
                .period(Period.builder()
                    .start(DateTime.now())
                    .end(DateTime.now())
                    .build())
                .build();

        String expression = "[Encounter] e return Last(Split(e.id,'/'))";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pDebug = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DEBUG)).value(fhirboolean(true)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary, pDebug).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString())).thenReturn(bundle(encounter));

        // Library fhirHelpers = TestHelper.getTestLibraryResource("FHIRHelpers-4.0.1.json");

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            // when(mockRegistry.getResource(javastring(fhirHelpers.getUrl()), Library.class)).thenReturn(fhirHelpers);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext(null);
            Parameters result = getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper, searchHelper);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            assertNotNull(resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_RETURN));

        }
    }

    @Test
    public void testInlineExpressionPatientGender() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter = Encounter.builder()
                .reasonCode(concept(reason))
                .status(EncounterStatus.FINISHED)
                .clazz(reason)
                .period(Period.builder()
                    .start(DateTime.now())
                    .end(DateTime.now())
                    .build())
                .build();

        String expression = "[Patient] p where p.gender = 'female'";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pDebug = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DEBUG)).value(fhirboolean(true)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary, pDebug).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString())).thenReturn(bundle(encounter));

        // Library fhirHelpers = TestHelper.getTestLibraryResource("FHIRHelpers-4.0.1.json");

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            // when(mockRegistry.getResource(javastring(fhirHelpers.getUrl()), Library.class)).thenReturn(fhirHelpers);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext(null);
            Parameters result = getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper, searchHelper);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            assertNotNull(resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_RETURN));

        }
    }

    @Test
    public void testInlineExpressionWithIncludes() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter =
                Encounter.builder().reasonCode(concept(reason)).status(EncounterStatus.FINISHED).clazz(reason).period(Period.builder().start(DateTime.now()).end(DateTime.now()).build()).build();

        Library fhirHelpers = TestHelper.getTestLibraryResource("FHIRHelpers-4.0.1.json");

        Library.Builder builder = TestHelper.buildBasicLibrary("FHIR-ModelInfo-4.0.1", "http://hl7.org/fhir/Library/FHIR-ModelInfo", "FHIR-ModelInfo", "4.0.1");
        builder.type(ModelHelper.concept(Constants.HL7_TERMINOLOGY_LIBRARY_TYPE, Constants.LIBRARY_TYPE_MODEL_DEFINITION));
        Library modelInfo = builder.build();

        String expression = "[Encounter] e where e.status = 'finished'";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pExpression = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pIncludeFHIRHelpers =
                Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_LIBRARY_URL)).value(Canonical.of(LibraryHelper.canonicalUrl(fhirHelpers))).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_LIBRARY)).part(pIncludeFHIRHelpers).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pExpression, pLibrary).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString())).thenReturn(bundle(encounter));

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            Canonical canonical;
            canonical = ModelHelper.canonical(fhirHelpers.getUrl(), fhirHelpers.getVersion());
            when(mockRegistry.getResource(canonical.getValue(), Library.class)).thenReturn(fhirHelpers);

            canonical = ModelHelper.canonical(modelInfo.getUrl(), modelInfo.getVersion());
            when(mockRegistry.getResource(canonical.getValue(), Library.class)).thenReturn(modelInfo);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext("cql");
            getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper, searchHelper);
        }
    }

    @Test
    public void testInlineExpressionInvalid() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String expression = "[NotAResource]";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString())).thenAnswer(x -> asResult(patient));

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext("cql");
            getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper, searchHelper);
            fail("Missing expected Exception");
        } catch (FHIROperationException fex) {
            assertNotNull(fex.getIssues());
            assertEquals(fex.getIssues().size(), 1);
            assertEquals(fex.getIssues().get(0).getCode(), IssueType.INVALID);
            System.out.println(fex.getMessage());
        }
    }



    @SuppressWarnings("unchecked")
    @Test
    public void testInlineExpressionWithDebug() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String expression = "[Condition]";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pDebug = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DEBUG)).value(fhirboolean(true)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary, pDebug).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Condition"), anyString(), anyString(), any(MultivaluedMap.class), anyString())).thenReturn(ModelHelper.bundle());

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext("cql");
            Parameters result = getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper, searchHelper);

            assertNotNull(result);
            System.out.println(result.toString());
            ParameterMap resultMap = new ParameterMap(result);
            resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_RETURN);
            resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_DEBUG_RESULT);
        }
    }
}
