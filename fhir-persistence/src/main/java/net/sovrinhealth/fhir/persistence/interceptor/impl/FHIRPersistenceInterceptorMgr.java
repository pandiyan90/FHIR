/*
 * (C) Copyright IBM Corp. 2016, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.interceptor.impl;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sovrinhealth.fhir.core.FHIRUtilities;
import net.sovrinhealth.fhir.persistence.context.FHIRPersistenceEvent;
import net.sovrinhealth.fhir.persistence.interceptor.FHIRPersistenceInterceptor;
import net.sovrinhealth.fhir.persistence.interceptor.FHIRPersistenceInterceptorException;

/**
 * This class implements the FHIR persistence interceptor framework. This framework allows users to inject business
 * logic into the REST API request processing code path at various points.
 *
 * Interceptors are discovered using the jdk's ServiceProvider class.
 *
 * To register an interceptor implementation, develop a class that implements the FHIRPersistenceInterceptor interface,
 * and then insert your implementation class name into a file called
 * META-INF/services/net.sovrinhealth.fhir.persistence.FHIRPersistenceInterceptor and store that file in your jar.
 * These "interceptor" jars should be stored in a common place defined by the FHIR Server.
 * @deprecated moved to net.sovrinhealth.fhir.server.interceptor in fhir-server
 */
@Deprecated
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
        if (iter.hasNext()) {
            log.fine("Discovered the following persistence interceptors:");
            while (iter.hasNext()) {
                FHIRPersistenceInterceptor interceptor = iter.next();
                if (log.isLoggable(Level.FINE)) {
                    log.fine(">>> " + interceptor.getClass().getName() + '@' + FHIRUtilities.getObjectHandle(interceptor));
                }
                interceptors.add(interceptor);
            }
        } else {
            log.fine("No persistence interceptors found...");
        }
    }

    /**
     * This method can be used to programmatically register an interceptor such that it is added
     * at the end of the list of registered interceptors.
     * @param interceptor persistence interceptor to be registered
     */
    public void addInterceptor(FHIRPersistenceInterceptor interceptor) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Registering persistence interceptor: " + interceptor.getClass().getName() + '@' + FHIRUtilities.getObjectHandle(interceptor));
        }
        interceptors.add(interceptor);
    }

    /**
     * This method can be used to programmatically register an interceptor such that it is added
     * at the beginning of the list of registered interceptors.
     * @param interceptor persistence interceptor to be registered
     */
    public void addPrioritizedInterceptor(FHIRPersistenceInterceptor interceptor) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Registering persistence interceptor: " + interceptor.getClass().getName() + '@' + FHIRUtilities.getObjectHandle(interceptor));
        }
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
}