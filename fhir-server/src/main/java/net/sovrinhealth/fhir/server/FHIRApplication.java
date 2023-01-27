/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Application;

import net.sovrinhealth.fhir.config.FHIRConfigHelper;
import net.sovrinhealth.fhir.config.FHIRConfiguration;
import net.sovrinhealth.fhir.provider.FHIRJsonPatchProvider;
import net.sovrinhealth.fhir.provider.FHIRJsonProvider;
import net.sovrinhealth.fhir.provider.FHIRProvider;
import net.sovrinhealth.fhir.server.resources.Batch;
import net.sovrinhealth.fhir.server.resources.Capabilities;
import net.sovrinhealth.fhir.server.resources.Create;
import net.sovrinhealth.fhir.server.resources.Delete;
import net.sovrinhealth.fhir.server.resources.History;
import net.sovrinhealth.fhir.server.resources.Operation;
import net.sovrinhealth.fhir.server.resources.Patch;
import net.sovrinhealth.fhir.server.resources.Read;
import net.sovrinhealth.fhir.server.resources.Search;
import net.sovrinhealth.fhir.server.resources.Update;
import net.sovrinhealth.fhir.server.resources.VRead;
import net.sovrinhealth.fhir.server.resources.WellKnown;
import net.sovrinhealth.fhir.server.resources.filters.FHIRVersionRequestFilter;
import net.sovrinhealth.fhir.server.resources.filters.OriginalRequestFilter;

public class FHIRApplication extends Application {
    private static final Logger log = Logger.getLogger(FHIRApplication.class.getName());

    private Set<Object> singletons = null;
    private Set<Class<?>> classes = null;

    public FHIRApplication() {
        log.entering(this.getClass().getName(), "ctor");

        FHIRBuildIdentifier buildInfo = new FHIRBuildIdentifier();
        log.info("FHIR Server version " + buildInfo.getBuildVersion() + " build id '" + buildInfo.getBuildId() + "' starting.");

        log.exiting(this.getClass().getName(), "ctor");
    }

    @Override
    public Set<Class<?>> getClasses() {
        log.entering(this.getClass().getName(), "getClasses");
        try {
            if (classes == null) {
                classes = new HashSet<Class<?>>();
                classes.add(Batch.class);
                classes.add(Capabilities.class);
                classes.add(Create.class);
                classes.add(Delete.class);
                classes.add(History.class);
                classes.add(Operation.class);
                classes.add(Patch.class);
                classes.add(Read.class);
                classes.add(Search.class);
                classes.add(Update.class);
                classes.add(VRead.class);
                classes.add(FHIRVersionRequestFilter.class);
                classes.add(OriginalRequestFilter.class);
                if (FHIRConfigHelper.getBooleanProperty(FHIRConfiguration.PROPERTY_SECURITY_OAUTH_SMART_ENABLED, false)) {
                    classes.add(WellKnown.class);
                }
            }
            return classes;
        } finally {
            log.exiting(this.getClass().getName(), "getClasses");
        }
    }

    @Override
    public Set<Object> getSingletons() {
        log.entering(this.getClass().getName(), "getSingletons");
        try {
            if (singletons == null) {
                singletons = new HashSet<Object>();
                singletons.add(new FHIRProvider(RuntimeType.SERVER));
                singletons.add(new FHIRJsonProvider(RuntimeType.SERVER));
                singletons.add(new FHIRJsonPatchProvider(RuntimeType.SERVER));
            }
            return singletons;
        } finally {
            log.exiting(this.getClass().getName(), "getSingletons");
        }
    }
}
