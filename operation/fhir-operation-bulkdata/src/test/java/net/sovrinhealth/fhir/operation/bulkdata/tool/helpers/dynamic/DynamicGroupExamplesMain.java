/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.operation.bulkdata.tool.helpers.dynamic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.generator.exception.FHIRGeneratorException;
import net.sovrinhealth.fhir.model.resource.Bundle;
import net.sovrinhealth.fhir.model.resource.Group;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.validation.FHIRValidator;
import net.sovrinhealth.fhir.validation.exception.FHIRValidationException;

/**
 * Generate the Examples as files in the Target Directory and
 * build the index file for Examples
 */
public class DynamicGroupExamplesMain {
    private static final String DIR = "target/";

    public static void main(String[] args) throws FHIRValidationException, FileNotFoundException, IOException, FHIRGeneratorException {
        List<GroupExample> groups = Arrays.asList(
            new AgeRangeBloodPressureGroup(), // Age >= 35 AND Age <= 70 which is outside the acceptable range
            new AgeSimpleGroup(), // Age >= 30
            new AgeRangeGroup(),  // Age >= 13 AND Age <= 56
            new AgeSimpleDisabledGroup(), // Age >= 30 which is disabled
            new AgeRangeWithGenderGroup(), // Age >= 13 AND Age <= 56 AND GENDER = Female
            new AgeRangeWithGenderAndExcludeGroup() // Age >= 13 AND Age <= 56 AND GENDER = Female AND NOT Pregnant
            );

        for(GroupExample groupExample : groups) {
            Group group = groupExample.group();
            List<Issue> issuesGroup = FHIRValidator.validator().validate(group);
            issuesGroup.stream().forEach(System.out::println);

            Bundle bundle = groupExample.sampleData();
            List<Issue> issuesBundle = FHIRValidator.validator().validate(bundle);
            issuesBundle.stream().forEach(System.out::println);

            System.out.println("OK         json/bulk-data/group/"+ groupExample.filename() + "-group.json");
            System.out.println("OK         json/bulk-data/group/"+ groupExample.filename() + "-example.json");
            try(FileOutputStream out1 = new FileOutputStream(new File(DIR + groupExample.filename() + "-group.json"));
                    FileOutputStream out2 = new FileOutputStream(new File(DIR + groupExample.filename() + "-example.json"))){
                FHIRGenerator.generator(Format.JSON, true).generate(group, out1);
                FHIRGenerator.generator(Format.JSON, true).generate(bundle, out2);
            }
        }
    }
}