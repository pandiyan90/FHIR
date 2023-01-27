/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.model;

import java.util.Set;

import net.sovrinhealth.fhir.database.utils.api.ISchemaAdapter;
import net.sovrinhealth.fhir.database.utils.api.SchemaApplyContext;

/**
 * Sequence related to the SQL sequence
 */
public class Sequence extends BaseObject {
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
     * @param incrementBy
     */
    public Sequence(String schemaName, String sequenceName, int version, long startWith, int cache) {
        this(schemaName, sequenceName, version, startWith, cache, 1);
    }

    public Sequence(String schemaName, String sequenceName, int version, long startWith, int cache, int incrementBy) {
        super(schemaName, sequenceName, DatabaseObjectType.SEQUENCE, version);
        this.startWith = startWith;
        this.cache = cache;
        this.incrementBy = incrementBy;
    }

    @Override
    public void apply(ISchemaAdapter target, SchemaApplyContext context) {
        target.createSequence(getSchemaName(), getObjectName(), this.startWith, this.cache, this.incrementBy);
    }

    @Override
    public void apply(Integer priorVersion, ISchemaAdapter target, SchemaApplyContext context) {
        if (priorVersion != null && priorVersion > 0 && this.version > priorVersion) {
            throw new UnsupportedOperationException("Upgrading sequences is not supported");
        }

        // Only if VERSION1 then we want to apply, else fall through
        // Re-creating a sequence can have unintended consequences.
        if (this.version == 1 && (priorVersion == null || priorVersion == 0)) {
            apply(target, context);
        }
    }

    @Override
    public void drop(ISchemaAdapter target) {
        target.dropSequence(getSchemaName(), getObjectName());
    }

    @Override
    protected void grantGroupPrivileges(ISchemaAdapter target, Set<Privilege> group, String toUser) {
        target.grantSequencePrivileges(getSchemaName(), getObjectName(), group, toUser);
    }

    /* (non-Javadoc)
     * @see net.sovrinhealth.fhir.database.utils.model.IDatabaseObject#visit(net.sovrinhealth.fhir.database.utils.model.DataModelVisitor)
     */
    @Override
    public void visit(DataModelVisitor v) {
        v.visited(this);
    }

    /* (non-Javadoc)
     * @see net.sovrinhealth.fhir.database.utils.model.IDatabaseObject#visitReverse(net.sovrinhealth.fhir.database.utils.model.DataModelVisitor)
     */
    @Override
    public void visitReverse(DataModelVisitor v) {
        v.visited(this);
    }

}
