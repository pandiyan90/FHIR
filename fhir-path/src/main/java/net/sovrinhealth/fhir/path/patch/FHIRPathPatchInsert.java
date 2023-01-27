/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.patch;

import static net.sovrinhealth.fhir.model.type.String.string;

import java.util.Objects;

import net.sovrinhealth.fhir.model.patch.exception.FHIRPatchException;
import net.sovrinhealth.fhir.model.resource.Parameters.Parameter;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.Element;
import net.sovrinhealth.fhir.path.exception.FHIRPathException;
import net.sovrinhealth.fhir.path.util.FHIRPathUtil;

class FHIRPathPatchInsert extends FHIRPathPatchOperation {
    String fhirPath;
    Element value;
    int index;

    public FHIRPathPatchInsert(String fhirPath, Element value, Integer index) {
        this.fhirPath = Objects.requireNonNull(fhirPath);
        this.value = Objects.requireNonNull(value);
        this.index = Objects.requireNonNull(index);
    }

    @Override
    public <T extends Resource> T apply(T resource) throws FHIRPatchException {
        try {
            return FHIRPathUtil.insert(resource, fhirPath, index, value);
        } catch (FHIRPathException e) {
            throw new FHIRPatchException("Error executing fhirPath", fhirPath);
        }
    }

    @Override
    public Parameter toParameter() {
        return Parameter.builder()
                .name(string(OPERATION))
                .part(Parameter.builder()
                    .name(string(TYPE))
                    .value(Code.of(FHIRPathPatchType.INSERT.value()))
                    .build())
                .part(Parameter.builder()
                    .name(string(PATH))
                    .value(string(fhirPath))
                    .build())
                .part(Parameter.builder()
                    .name(string(INDEX))
                    .value(net.sovrinhealth.fhir.model.type.Integer.of(index))
                    .build())
                .part(Parameter.builder()
                    .name(string(VALUE))
                    .value(value)
                    .build())
                .build();
    }
}