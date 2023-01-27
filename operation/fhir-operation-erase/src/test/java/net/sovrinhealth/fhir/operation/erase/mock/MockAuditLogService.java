/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.operation.erase.mock;

import net.sovrinhealth.fhir.audit.AuditLogService;
import net.sovrinhealth.fhir.audit.beans.AuditLogEntry;
import net.sovrinhealth.fhir.config.PropertyGroup;

/**
 * Mock Audit Log Service
 */
public class MockAuditLogService implements AuditLogService {
    boolean throwOnLog = false;
    boolean enabled = true;
    public MockAuditLogService() {
        super();
    }

    public MockAuditLogService(boolean throwEx, boolean enabled) {
        super();
        this.throwOnLog = throwEx;
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void initialize(PropertyGroup auditLogProperties) throws Exception {
        // No Operation
    }

    @Override
    public void stop(PropertyGroup auditLogProperties) throws Exception {
        // No Operation
    }

    @Override
    public void logEntry(AuditLogEntry logEntry) throws Exception {
        // No Operation
        // @implNote if we want to get down to it, this is where we'd store the auditLogEntry and
        // add static methods to get the auditLogEntry
        if (throwOnLog) {
            throw new Exception("Ughh.");
        }
    }
}