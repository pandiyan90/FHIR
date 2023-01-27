/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.context;

import net.sovrinhealth.fhir.core.context.FHIRPagingContext;
import net.sovrinhealth.fhir.model.type.Instant;

public interface FHIRHistoryContext extends FHIRPagingContext {
    
    Instant getSince();
    void setSince(Instant since);
}
