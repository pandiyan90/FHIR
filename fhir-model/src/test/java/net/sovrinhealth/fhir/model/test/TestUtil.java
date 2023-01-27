/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.model.test;

import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import net.sovrinhealth.fhir.core.FHIRUtilities;
import net.sovrinhealth.fhir.examples.ExamplesUtil;
import net.sovrinhealth.fhir.exception.FHIRException;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.type.Reference;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;

public class TestUtil {
    private static final JsonReaderFactory JSON_READER_FACTORY = Json.createReaderFactory(null);

    private static final Map<Class<? extends Resource>, Resource> minimalExampleMap = new ConcurrentHashMap<>();

    /**
     * This is a list of pre-defined locations that we'll search in when looking for a local data file.
     */
    private static final String[] searchPaths = { "./", "src/test/resources/", "src/test/resources/testdata/", "src/main/resources/" };

    /**
     * This function finds the specified file on the searchPath,
     * then de-serializes that into a Resource.
     *
     * @implNote By default, the search path includes the following relative paths:
     * <li>./
     * <li>src/main/resources/
     * <li>src/test/resources/
     * <li>src/test/resources/testdata/
     *
     * @param fileName
     *            the name of the file containing the local resource (e.g. "testdata/Patient1.json")
     * @return the de-serialized mock resource
     * @throws Exception
     */
    public static <T extends Resource> T readLocalResource(String fileName) throws Exception {

        // Use the filename suffix to determine the format that we're reading, defaulting to JSON
        Format fmt = (fileName.endsWith(".xml") ? Format.XML : Format.JSON);

        // Deserialize the file contents.
        try (Reader reader = new InputStreamReader(resolveFileLocation(fileName), Charset.forName("UTF-8"))) {
            T resource = FHIRParser.parser(fmt).parse(reader);
            return resource;
        }
    }

    /**
     * This function reads the contents of an example resource from the specified path in fhir-examples into a Resource.
     *
     * @param fileName
     *            the name of the file containing the example resource (e.g. "json/minimal/Patient-1.json")
     * @return the de-serialized resource
     * @throws Exception
     */
    public static <T extends Resource> T readExampleResource(String fileName) throws Exception {

        // Use the filename suffix to determine the format that we're reading, defaulting to JSON
        Format fmt = (fileName.endsWith(".xml") ? Format.XML : Format.JSON);

        // Deserialize the file contents.
        try (Reader reader = ExamplesUtil.resourceReader(fileName)) {
            return FHIRParser.parser(fmt).parse(reader);
        }
    }

    public static JsonObjectBuilder getEmptyBundleJsonObjectBuilder() {
        JsonObjectBuilder bundleObject = Json.createBuilderFactory(null).createObjectBuilder();
        bundleObject.add("resourceType", "Bundle")
            .add("type", "batch");
        return bundleObject;
    }

    public static JsonObject getRequestJsonObject(String method, String url) {
        JsonObjectBuilder requestObject = Json.createBuilderFactory(null).createObjectBuilder();
        requestObject.add("method", method)
            .add("url", url);
        return requestObject.build();
    }

    /**
     * Loads an Observation resource from the specified file, then associates it with
     * the specified patient via a subject attribute.
     */
    public static Observation buildPatientObservation(String patientId, String fileName) throws Exception {
        Observation observation = readLocalResource(fileName);

        observation = observation
            .toBuilder()
            .subject(Reference.builder().reference("Patient/" + patientId).build())
            .build();
        return observation;
    }

    /**
     * Loads an Observation resource from the specified file, then associates it with
     * <ul>
     * <li>the specified patient via Observation.subject
     * <li>the specified practitioner via Observation.performer
     * </ul>
     */
    public static Observation buildPatientObservation(String patientId, String practitionerId, String fileName) throws Exception {
        Observation observation = readLocalResource(fileName);

        observation = observation
            .toBuilder()
            .subject(Reference.builder().reference("Patient/" + patientId).build())
            .performer(Reference.builder().reference("Practitioner/" + practitionerId).build())
            .build();
        return observation;
    }

    /**
     * Returns an InputStream for the specified fileName after searching in a few pre-defined locations.
     */
    public static InputStream resolveFileLocation(String fileName) throws Exception {

        // First, try to use the filename as-is.
        File f = new File(fileName);
        if (f.exists()) {
            return new FileInputStream(f);
        }

        // Otherwise, look in our configured search path, one directory at a time.
        for (String path : searchPaths) {
            f = new File(path + fileName);
            if (f.exists()) {
                return new FileInputStream(f);
            }
        }

        // If we didn't find the file yet, then look on the classpath.
        String resourceName = (fileName.startsWith("json/spec/") ? fileName : "json/spec/" + fileName);
        InputStream is = TestUtil.class.getResourceAsStream(resourceName);
        if (is != null) {
            return is;
        }

        throw new FileNotFoundException("File '" + fileName + "' was not found.");
    }


    /**
     * Asserts that the "actual" resource contains all the fields and values from the "expected" resource.
     * The comparison is performed in a "lenient" manner, meaning that we'll ensure that all the fields
     * contained in the "expected" resource are also contained in the "actual" resource, but not vice-versa.
     *
     * @param expected the known-good resource to compare against
     * @param actual the resource to be validated.
     */
    public static void assertResourceEquals(Resource expected, Resource actual) {
        assertResourceEquals(null, expected, actual);
    }

    /**
     * Asserts that the "actual" resource contains all the fields and values from the "expected" resource.
     * The comparison is performed in a "lenient" manner, meaning that we'll ensure that all the fields
     * contained in the "expected" resource are also contained in the "actual" resource, but not vice-versa.
     *
     * @param msg a string to be included in any error messages
     * @param expected the known-good resource to compare against
     * @param actual the resource to be validated.
     */
    public static void assertResourceEquals(String msg, Resource expected, Resource actual) {
        String cleanMsg = (msg != null ? msg : "");
        if (!expected.getClass().equals(actual.getClass())) {
            fail(cleanMsg + ": resource type mismatch, expected resource of type: " + expected.getClass() +
                    ", but was of type: " + actual.getClass());
        }

        // Serialize the 'expected' resource.
        String jsonExpected = null;
        try {
            jsonExpected = FHIRUtilities.stripNewLineWhitespaceIfPresentInDiv(
                                   FHIRUtilities.stripNamespaceIfPresentInDiv(writeResource(expected, Format.JSON, true)));
        } catch (Throwable t) {
            fail(cleanMsg + ": error serializing expected resource: " + t);
        }

        // Serialize the 'actual' resource.
        String jsonActual = null;
        try {
            jsonActual = FHIRUtilities.stripNewLineWhitespaceIfPresentInDiv(
                                FHIRUtilities.stripNamespaceIfPresentInDiv(writeResource(actual, Format.JSON, true)));
        } catch (Throwable t) {
            fail(cleanMsg + ": error serializing actual resource: " + t);
        }

        // Finally, make sure the expected and actual resources are equivalent.
        // Note that we'll do a "lenient" comparison which means we'll make sure
        // that all the fields contained in "expected" also exist in "actual".
        // The "actual" resource in the REST API response will likely contain a couple of additional
        // fields (e.g. "id" and "meta") so we can't do a "strict" comparison.
        try {
            JSONAssert.assertEquals(jsonExpected, jsonActual, false);
        } catch (JSONException e) {
            fail(cleanMsg + ": expected resource: \n" + jsonExpected + "\nbut actual resource was:\n" + jsonActual);
        }
    }

    /**
     * Reads a properties file containing test related properties used by subclasses.
     * @return Properties - A Properties object containing the contents of the test.properties file.
     * @throws Exception
     */
    public static Properties readTestProperties(String fileName) throws Exception {
        Properties properties = new Properties();
        try (InputStream is = resolveFileLocation(fileName)) {
            properties.load(is);
            return properties;
        }
    }

    /**
     * Serializes the specified resource according to 'fmt' (JSON/XML).
     */
    public static <T extends Resource> String writeResource(T resource, Format fmt, boolean prettyPrint) throws FHIRException {
        StringWriter sw = new StringWriter();
        FHIRGenerator.generator(fmt, prettyPrint).generate(resource, sw);
        return sw.toString();
    }

    /**
     * Reads a JSON object from the specified file.
     */
    public static JsonObject readJsonObject(String fileName) throws Exception {
        try (JsonReader jsonReader = JSON_READER_FACTORY.createReader(resolveFileLocation(fileName))) {
            return jsonReader.readObject();

        }
    }

    /**
     * If the {@code resourceToFind} is contained in the list of resources this method returns true.
     * Otherwise it returns false.
     * And This function will fail the test if we either:
     * A. find the same resource twice; or
     * B. find different versions of the same resource.
     * @param resourceToFind
     * @param resources
     */
    public static boolean isResourceInResponse(Resource resourceToFind, List<? extends Resource> resources) {
        boolean alreadyFound = false;
        int count = 0;

        String resourceTypeToFind = resourceToFind.getClass().getSimpleName();
        String idToFind = resourceToFind.getId();
        String versionToFind = resourceToFind.getMeta().getVersionId().getValue();

        for (Resource r : resources) {
            String resourceType = resourceToFind.getClass().getSimpleName();
            String id = r.getId();
            String version = r.getMeta().getVersionId().getValue();
            if (idToFind.equals(id) && resourceTypeToFind.equals(resourceType)) {
                if (versionToFind.equals(version)) {
                    count++;
                    if (alreadyFound) {
                        System.out.println("found resource with id " + id + " " + count + " times.");
                        fail("Resource with id '" + id + "' was returned multiple times in the search.");
                    }
                    alreadyFound = true;
                } else {
                    fail("Search has returned historical resource for resource id '" + id + "'.\n"
                            + "Expected: version " + resourceToFind.getMeta().getVersionId().getValue() + "\n"
                            + "Actual: version " + version);
                }
            }
        }
        return alreadyFound;
    }

    /**
     * Get a minimal resource of the specified type. Only required fields are present and all data is absent.
     *
     * @param type
     *            the type of the resource to be returned
     * @return the resource
     * @throws Exception
     * @implNote this function stores the created resources in a map and re-uses them. if an entry doesn't exist yet,
     *            it creates the resource via {@link #getMinimalResource(Class<T>, Format)} using the JSON format
     */
    public static <T extends Resource> T getMinimalResource(Class<T> type) throws Exception {
        Resource minimalResource = minimalExampleMap.get(type);

        if (minimalResource == null) {
            minimalResource = getMinimalResource(type, Format.JSON);
            minimalExampleMap.put(type, minimalResource);
        }

        return minimalResource.as(type);
    }

    /**
     * This function reads the contents of a minimal example resource file of the specified type and format from
     * fhir-examples, and returns a resource of the specified resource type and format.
     *
     * @param type
     *            the type of the resource to be returned
     * @param format
     *            the format of the resource to be returned (XML or JSON)
     * @return the de-serialized resource
     * @throws Exception
     */
    public static <T extends Resource> T getMinimalResource(Class<T> type, Format format) throws Exception {

        // Build filename
        String formatString = format.toString().toLowerCase();
        String fileName = formatString + "/minimal/" + type.getSimpleName() + "-1." + formatString;

        // Deserialize the file contents.
        try (Reader reader = ExamplesUtil.resourceReader(fileName)) {
            return FHIRParser.parser(format).parse(reader);
        }
    }
}
