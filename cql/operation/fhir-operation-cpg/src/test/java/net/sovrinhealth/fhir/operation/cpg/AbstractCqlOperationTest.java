/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.operation.cpg;

import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.fhirboolean;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.fhircode;
import static net.sovrinhealth.fhir.cql.helpers.ModelHelper.fhirstring;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.cql.engine.model.FHIRModelResolver;
import net.sovrinhealth.fhir.cql.helpers.DataProviderFactory;
import net.sovrinhealth.fhir.cql.helpers.ParameterMap;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.OperationDefinition;
import net.sovrinhealth.fhir.model.resource.Parameters;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.code.OperationKind;
import net.sovrinhealth.fhir.model.type.code.PublicationStatus;
import net.sovrinhealth.fhir.search.util.SearchHelper;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.spi.operation.FHIRResourceHelpers;

public class AbstractCqlOperationTest extends BaseCqlOperationTest<AbstractCqlOperation> {

    @Test
    public void testGetCqlContext() {
        Parameters.Parameter p = Parameters.Parameter.builder().name(fhirstring("subject")).value(fhirstring("Patient/123")).build();
        Parameters parameters = Parameters.builder().parameter(p).build();

        ParameterMap paramMap = new ParameterMap(parameters);

        Pair<String, Object> context = op.getCqlContext(paramMap);
        assertEquals(context.getKey(), "Patient");
        assertEquals(context.getValue(), "123");
    }

    @Test
    public void testDataProviderResolveResourcePackage() {
        RetrieveProvider retrieveProvider = mock(RetrieveProvider.class);
        Map<String, DataProvider> dataProviders = DataProviderFactory.createDataProviders(retrieveProvider);
        assertTrue(dataProviders.size() > 0);

        org.cqframework.cql.elm.execution.Library library = mock(org.cqframework.cql.elm.execution.Library.class);
        when(library.getIdentifier()).thenReturn(new VersionedIdentifier().withId("test"));

        Context context = new Context(library);
        for (Map.Entry<String, DataProvider> entry : dataProviders.entrySet()) {
            context.registerDataProvider(entry.getKey(), entry.getValue());
        }

        DataProvider resolved = context.resolveDataProvider(FHIRModelResolver.BASE_PACKAGE_NAME + ".resource");
        assertNotNull(resolved);
    }

    @Override
    protected AbstractCqlOperation getOperation() {
        return new AbstractCqlOperation() {

            @Override
            protected OperationDefinition buildOperationDefinition() {
                return OperationDefinition.builder()
                        .name(fhirstring("dummy"))
                        .status(PublicationStatus.ACTIVE)
                        .kind(OperationKind.OPERATION).system(fhirboolean(true))
                        .type(fhirboolean(false))
                        .instance(fhirboolean(false))
                        .code(fhircode("dummy"))
                        .build();
            }

            @Override
            protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
                    Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper) throws FHIROperationException {
                throw new FHIROperationException("You should not have reached this point");
            }

            @Override
            protected Set<String> getCqlExpressionsToEvaluate(ParameterMap paramMap) {
                return null;
            }
        };
    }
}
