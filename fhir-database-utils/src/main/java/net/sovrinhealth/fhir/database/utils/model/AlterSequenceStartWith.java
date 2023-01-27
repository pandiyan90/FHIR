/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.model;

import java.util.Set;

import net.sovrinhealth.fhir.database.utils.api.ISchemaAdapter;
import net.sovrinhealth.fhir.database.utils.api.SchemaApplyContext;

/**
 * Modify an existing sequence to start with a higher value
 */
public class AlterSequenceStartWith extends BaseObject {
    // the value we want the sequence to start with
    private final long startWith;
    
    // caching sequence values for tuning
    private final int cache;
    
    private final int incrementBy;

    /**
     * Public constructor
     * 
     * @param schemaName
     * @param sequenceName
     * @param version
     * @param startWith
     * @param cache
     */
    public AlterSequenceStartWith(String schemaName, String sequenceName, int version, long startWith, int cache, int incrementBy) {
        super(schemaName, sequenceName, DatabaseObjectType.SEQUENCE, version);
        this.startWith = startWith;
        this.cache = cache;
        this.incrementBy = incrementBy;
    }

    @Override
    public void apply(ISchemaAdapter target, SchemaApplyContext context) {
        target.alterSequenceRestartWith(getSchemaName(), getObjectName(), startWith, this.cache, this.incrementBy);
    }

    @Override
    public void apply(Integer priorVersion, ISchemaAdapter target, SchemaApplyContext context) {
        apply(target, context);
    }

    @Override
    public void drop(ISchemaAdapter target) {
        // NOP. Sequence will be dropped by the object initially creating it
    }

    @Override
    protected void grantGroupPrivileges(ISchemaAdapter target, Set<Privilege> group, String toUser) {
        target.grantSequencePrivileges(getSchemaName(), getObjectName(), group, toUser);
    }

    @Override
    public void visit(DataModelVisitor v) {
        v.visited(this);
    }

    @Override
    public void visitReverse(DataModelVisitor v) {
        v.visited(this);
    }
}
