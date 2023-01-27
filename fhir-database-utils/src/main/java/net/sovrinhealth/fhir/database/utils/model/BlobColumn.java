/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.model;

import net.sovrinhealth.fhir.database.utils.api.IDatabaseTypeAdapter;

/**
 * Binary Large OBject (BLOB) Column
 */
public class BlobColumn extends ColumnBase {

    private final long size;

    private final int inlineSize;

    /**
     * @param name
     */
    public BlobColumn(String name, long size, int inlineSize, boolean nullable) {
        super(name, nullable);
        this.size = size;
        this.inlineSize = inlineSize;
    }

    public long getSize() {
        return size;
    }

    public int getInlineSize() {
        return inlineSize;
    }

    @Override
    public String getTypeInfo(IDatabaseTypeAdapter adapter) {
        return adapter.blobClause(size, inlineSize);
    }

}
