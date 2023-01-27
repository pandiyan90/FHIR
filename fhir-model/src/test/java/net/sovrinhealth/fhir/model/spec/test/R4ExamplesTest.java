/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.spec.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sovrinhealth.fhir.examples.Index;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.visitor.CopyingVisitor;

/**
 * Exercise the examples driver, which will process each entry in the test
 * resources directory
 */
public class R4ExamplesTest {
    private R4ExamplesDriver driver;

    @BeforeClass
    public void setup() {
//        FHIRModelConfig.setCheckReferenceTypes(false);
        driver = new R4ExamplesDriver();
    }

    @Test
    public void serializationTest() throws Exception {
        driver.setProcessor(new SerializationProcessor());
        String index = System.getProperty(this.getClass().getName()
            + ".index", Index.ALL_XML.name());
        driver.processIndex(Index.valueOf(index));
    }

    @Test
    public void copyTest() throws Exception {
        driver.setProcessor(new CopyProcessor(new CopyingVisitor<Resource>()));
        String index = System.getProperty(this.getClass().getName()
            + ".index", Index.ALL_XML.name());
        driver.processIndex(Index.valueOf(index));
    }

    /**
     * Main method only used for driving ad-hoc testing
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        R4ExamplesTest self = new R4ExamplesTest();
        self.setup();
        // self.driver.setProcessor(new SerializationProcessor());
        self.driver.setProcessor(new CopyProcessor(new CopyingVisitor<Resource>()));
        self.driver.processExample("json/spec/medicinalproductdefinition-example.json", Format.JSON, Expectation.OK);
    }
}
