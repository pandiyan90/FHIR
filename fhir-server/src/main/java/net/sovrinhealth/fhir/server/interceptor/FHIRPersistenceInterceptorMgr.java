/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.interceptor;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceEvent;
import net.sovrinhealth.fhir.server.spi.interceptor.FHIRPersistenceInterceptor;
import net.sovrinhealth.fhir.server.spi.interceptor.FHIRPersistenceInterceptorException;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;

/**
 * This class implements the FHIR persistence interceptor framework. This framework allows users to inject business
 * logic into the REST API request processing code path at various points.
 *
 * Interceptors are discovered using the jdk's ServiceLoader class.
 *
 * To register an interceptor implementation, develop a class that implements the FHIRPersistenceInterceptor interface,
 * and then insert your implementation class name into a file called
 * META-INF/services/net.sovrinhealth.fhir.server.spi.interceptor.FHIRPersistenceInterceptor and store that file in your jar.
 * These "interceptor" jars should be stored in a common place defined by the FHIR Server.
 */
public class FHIRPersistenceInterceptorMgr {
    private static final Logger log = Logger.getLogger(FHIRPersistenceInterceptorMgr.class.getName());

    private static FHIRPersistenceInterceptorMgr instance = new FHIRPersistenceInterceptorMgr();

    // Our list of discovered interceptors.
    List<FHIRPersistenceInterceptor> interceptors = new CopyOnWriteArrayList<>();

    public static FHIRPersistenceInterceptorMgr getInstance() {
        return instance;
    }

    private FHIRPersistenceInterceptorMgr() {
        // Discover all implementations of our interceptor interface, then add them to our list of interceptors.
        ServiceLoader<FHIRPersistenceInterceptor> slList = ServiceLoader.load(FHIRPersistenceInterceptor.class);
        Iterator<FHIRPersistenceInterceptor> iter = slList.iterator();
        while (iter.hasNext()) {
            FHIRPersistenceInterceptor interceptor = iter.next();
            interceptors.add(interceptor);
        }
        log.info("Loaded the following persistence interceptors: " + interceptors);
    }

    /**
     * This method can be used to programmatically register an interceptor such that it is added
     * at the end of the list of registered interceptors.
     * @param interceptor persistence interceptor to be registered
     */
    public void addInterceptor(FHIRPersistenceInterceptor interceptor) {
        log.info("Registering persistence interceptor: " + interceptor);
        interceptors.add(interceptor);
    }

    /**
     * This method can be used to programmatically register an interceptor such that it is added
     * at the beginning of the list of registered interceptors.
     * @param interceptor persistence interceptor to be registered
     */
    public void addPrioritizedInterceptor(FHIRPersistenceInterceptor interceptor) {
        log.info("Registering prioritized persistence interceptor: " + interceptor);
        interceptors.add(0, interceptor);
    }

    /**
     * The following methods will invoke the respective interceptor methods on each registered interceptor.
     */
    public void fireBeforeCreateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeCreate(event);
        }
    }

    public void fireAfterCreateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterCreate(event);
        }
    }

    public void fireBeforeUpdateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeUpdate(event);
        }
    }

    public void fireAfterUpdateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterUpdate(event);
        }
    }

    public void fireBeforePatchEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforePatch(event);
        }
    }

    public void fireAfterPatchEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterPatch(event);
        }
    }

    public void fireBeforeDeleteEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeDelete(event);
        }
    }

    public void fireAfterDeleteEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterDelete(event);
        }
    }

    public void fireBeforeReadEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeRead(event);
        }
    }

    public void fireAfterReadEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterRead(event);
        }
    }

    public void fireBeforeVreadEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeVread(event);
        }
    }

    public void fireAfterVreadEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterVread(event);
        }
    }

    public void fireBeforeHistoryEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeHistory(event);
        }
    }

    public void fireAfterHistoryEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterHistory(event);
        }
    }

    public void fireBeforeSearchEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeSearch(event);
        }
    }

    public void fireAfterSearchEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterSearch(event);
        }
    }

    public void fireBeforeInvokeEvent(FHIROperationContext context) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeInvoke(context);
        }
    }

    public void fireAfterInvokeEvent(FHIROperationContext context) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterInvoke(context);
        }
    }
}