/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.registry;

import static net.sovrinhealth.fhir.cache.CacheKey.key;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sovrinhealth.fhir.cache.CacheKey;
import net.sovrinhealth.fhir.cache.CacheManager;
import net.sovrinhealth.fhir.cache.CacheManager.Configuration;
import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.core.FHIRVersionParam;
import net.sovrinhealth.fhir.core.util.ResourceTypeUtil;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.resource.SearchParameter;
import net.sovrinhealth.fhir.model.resource.StructureDefinition;
import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.MultiResourceResult;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContext;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceContextFactory;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.helper.FHIRTransactionHelper;
import net.sovrinhealth.fhir.persistence.helper.PersistenceHelper;
import net.sovrinhealth.fhir.registry.resource.FHIRRegistryResource;
import net.sovrinhealth.fhir.registry.spi.AbstractRegistryResourceProvider;
import net.sovrinhealth.fhir.search.context.FHIRSearchContext;
import net.sovrinhealth.fhir.search.util.SearchHelper;

public class ServerRegistryResourceProvider extends AbstractRegistryResourceProvider {
    public static final Logger log = Logger.getLogger(ServerRegistryResourceProvider.class.getName());

    public static final String REGISTRY_RESOURCE_CACHE_NAME = "net.sovrinhealth.fhir.server.registry.ServerRegistryResourceProvider.registryResourceCache";
    public static final Configuration REGISTRY_RESOURCE_CACHE_CONFIGURATION = Configuration.of(1024, Duration.of(1, ChronoUnit.MINUTES));

    public static final String ALL_RESOURCE_TYPES = String.join(",", ResourceTypeUtil.getResourceTypesFor(FHIRVersionParam.VERSION_43));

    private final PersistenceHelper persistenceHelper;
    private final SearchHelper searchHelper;

    public ServerRegistryResourceProvider(PersistenceHelper persistenceHelper, SearchHelper searchHelper) {
        this.persistenceHelper = Objects.requireNonNull(persistenceHelper);
        this.searchHelper = Objects.requireNonNull(searchHelper);
    }

    @Override
    protected List<FHIRRegistryResource> getRegistryResources(Class<? extends Resource> resourceType, String url) {
        String dataStoreId = FHIRRequestContext.get().getDataStoreId();
        CacheKey key = key(dataStoreId, url);
        try {
            Map<CacheKey, List<FHIRRegistryResource>> cacheAsMap = CacheManager.getCacheAsMap(REGISTRY_RESOURCE_CACHE_NAME, REGISTRY_RESOURCE_CACHE_CONFIGURATION);
            return cacheAsMap.computeIfAbsent(key, k -> computeRegistryResources(resourceType, url));
        } finally {
            CacheManager.reportCacheStats(log, REGISTRY_RESOURCE_CACHE_NAME);
        }
    }

    @Override
    public Collection<FHIRRegistryResource> getRegistryResources(Class<? extends Resource> resourceType) {
        try {
            return getRegistryResources(resourceType, Collections.emptyMap());
        } catch (Exception e) {
            log.log(Level.WARNING, "An error occurred during a search interaction", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<FHIRRegistryResource> getRegistryResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<FHIRRegistryResource> getProfileResources() {
        Map<String, List<String>> queryParameters = new HashMap<>();
        queryParameters.put("type", Collections.singletonList(ALL_RESOURCE_TYPES));
        queryParameters.put("kind", Collections.singletonList("resource"));
        queryParameters.put("derivation", Collections.singletonList("constraint"));
        return getRegistryResources(StructureDefinition.class, queryParameters);
    }

    @Override
    public Collection<FHIRRegistryResource> getProfileResources(String type) {
        Map<String, List<String>> queryParameters = new HashMap<>();
        queryParameters.put("type", Collections.singletonList(type));
        queryParameters.put("kind", Collections.singletonList("resource"));
        queryParameters.put("derivation", Collections.singletonList("constraint"));
        return getRegistryResources(StructureDefinition.class, queryParameters);
    }

    @Override
    public Collection<FHIRRegistryResource> getSearchParameterResources(String type) {
        Map<String, List<String>> queryParameters = new HashMap<>();
        queryParameters.put("type", Collections.singletonList(type));
        return getRegistryResources(SearchParameter.class, queryParameters);
    }

    private List<FHIRRegistryResource> computeRegistryResources(Class<? extends Resource> resourceType, String url) {
        FHIRTransactionHelper transactionHelper = null;
        try {
            FHIRPersistence persistence = persistenceHelper.getFHIRPersistenceImplementation();
            transactionHelper = new FHIRTransactionHelper(persistence.getTransaction());

            transactionHelper.begin();

            FHIRSearchContext searchContext = searchHelper.parseQueryParameters(resourceType, Collections.singletonMap("url", Collections.singletonList(url)));
            searchContext.setPageSize(1000);

            FHIRPersistenceContext context = FHIRPersistenceContextFactory.createPersistenceContext(null, searchContext, null);
            MultiResourceResult result = persistence.search(context, resourceType);

            if (result.isSuccess()) {
                transactionHelper.commit();
                transactionHelper = null;

                return result.getResourceResults().stream()
                        .map(rr -> rr.getResource())
                        .map(FHIRRegistryResource::from)
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "An error occurred during the underlying search interaction; returning empty", e);
        } finally {
            if (transactionHelper != null) {
                try {
                    transactionHelper.rollback();
                } catch (FHIRPersistenceException e) {
                    log.log(Level.WARNING, "An error occurred while rolling back the current transaction", e);
                }
            }
        }
        return Collections.emptyList();
    }

    private Collection<FHIRRegistryResource> getRegistryResources(Class<? extends Resource> resourceType, Map<String, List<String>> queryParameters) {
        FHIRTransactionHelper transactionHelper = null;
        try {
            FHIRPersistence persistence = persistenceHelper.getFHIRPersistenceImplementation();
            transactionHelper = new FHIRTransactionHelper(persistence.getTransaction());

            transactionHelper.begin();

            FHIRSearchContext searchContext = searchHelper.parseQueryParameters(resourceType, queryParameters);
            searchContext.setPageSize(1000);

            FHIRPersistenceContext context = FHIRPersistenceContextFactory.createPersistenceContext(null, searchContext, null);
            MultiResourceResult result = persistence.search(context, resourceType);

            if (result.isSuccess()) {
                List<FHIRRegistryResource> registryResources = new ArrayList<>(searchContext.getTotalCount() != null ? searchContext.getTotalCount() : result.getResourceResults().size());
                registryResources.addAll(result.getResourceResults().stream()
                        .map(rr -> rr.getResource())
                        .map(FHIRRegistryResource::from)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));

                int pageNumber = 1;
                int lastPageNumber = searchContext.getLastPageNumber();
                while (pageNumber < lastPageNumber) {
                    searchContext.setPageNumber(++pageNumber);
                    result = persistence.search(context, resourceType);
                    registryResources.addAll(result.getResourceResults().stream()
                            .map(rr -> rr.getResource())
                            .map(FHIRRegistryResource::from)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                }

                transactionHelper.commit();
                transactionHelper = null;

                return Collections.unmodifiableList(registryResources);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "An error occurred during a search interaction", e);
        } finally {
            if (transactionHelper != null) {
                try {
                    transactionHelper.rollback();
                } catch (FHIRPersistenceException e) {
                    log.log(Level.WARNING, "An error occurred ending the current transaction", e);
                }
            }
        }

        return Collections.emptyList();
    }
}
