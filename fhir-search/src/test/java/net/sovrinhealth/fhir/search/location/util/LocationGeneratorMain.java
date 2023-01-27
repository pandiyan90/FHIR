/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.search.location.util;

import static net.sovrinhealth.fhir.model.type.String.string;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.generator.exception.FHIRGeneratorException;
import net.sovrinhealth.fhir.model.parser.exception.FHIRParserException;
import net.sovrinhealth.fhir.model.resource.Location;
import net.sovrinhealth.fhir.model.type.Address;
import net.sovrinhealth.fhir.model.type.Code;
import net.sovrinhealth.fhir.model.type.CodeableConcept;
import net.sovrinhealth.fhir.model.type.Coding;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.Identifier;
import net.sovrinhealth.fhir.model.type.code.LocationMode;
import net.sovrinhealth.fhir.model.type.code.LocationStatus;
import net.sovrinhealth.fhir.path.exception.FHIRPathException;
import net.sovrinhealth.fhir.validation.FHIRValidator;
import net.sovrinhealth.fhir.validation.exception.FHIRValidationException;

/**
 * This class is used to generate Location Resources to support Synthetic Mass Tests
 */
public class LocationGeneratorMain {
    private static final String DIR = "target/";

    private static int countOfLocationType = 0;
    private static Map<String,String> locationTypes = new HashMap<String,String>(){
        private static final long serialVersionUID = -2746048558467523545L;
        {
            put("DX", "Diagnostics or therapeutics unit");
            put("CVDX", "Cardiovascular diagnostics or therapeutics unit");
            put("RNEU", "Neuroradiology unit");
        }
    };

    private static List<String> organizedKeyLocationTypes = locationTypes.keySet().stream().sorted().collect(Collectors.toList());

    public static void main(String... args) throws FHIRParserException, FHIRPathException, IOException, URISyntaxException, FHIRGeneratorException, FHIRValidationException {
        List<String> lines =
                Files.lines(Paths.get(LocationGeneratorMain.class.getResource("/testdata/location/location-data.csv").toURI())).collect(Collectors.toList());
        for (String line : lines) {
            String[] parts = line.split(",");
            String address = parts[0];
            String city = parts[1];
            String state = parts[2]; // Shouldn't be anything other than MA
            String zipCode = parts[3];
            String country = parts[4];
            String latitude = parts[5];
            String longitude = parts[6];
            Location location = buildLocation(address, city, state, zipCode, country, latitude, longitude);

            FHIRValidator.validator().validate(location);

            System.out.println("OK         json/bulk-data/location/"+ "synthetic-mass-" + countOfLocationType + ".json");
            try(FileOutputStream out = new FileOutputStream(new File(DIR + "synthetic-mass-" + countOfLocationType + ".json"))){
                FHIRGenerator.generator(Format.JSON, true).generate(location, out);
            }
        }
    }

    public static CodeableConcept getLocationType() {
        int current = countOfLocationType % 3;
        String key = organizedKeyLocationTypes.get(current);

        CodeableConcept.Builder builder = CodeableConcept.builder();
        builder.coding(Coding.builder()
            .code(Code.of(key))
            .system(net.sovrinhealth.fhir.model.type.Uri.of("http://terminology.hl7.org/CodeSystem/v3-RoleCode"))
            .display(net.sovrinhealth.fhir.model.type.String.of(locationTypes.get(key))).build())
        .text(net.sovrinhealth.fhir.model.type.String.of(locationTypes.get(key)))
        .build();
        return builder.build();
    }

    public static Location buildLocation(String address, String city, String state, String zipCode,
        String country, String latitude, String longitude) {

        countOfLocationType++;
        Identifier identifier = Identifier.builder()
                .id(UUID.randomUUID().toString())
                .value(string("Synthethic Mass - " + countOfLocationType))
                .build();

        Address addr = Address.builder().city(string(city))
                .country(string("USA")).line(string(address)).postalCode(string(zipCode)).state(string(state)).build();

        Double lng = Double.parseDouble(longitude);
        Double lat = Double.parseDouble(latitude);
        Location.Position position = Location.Position.builder()
                .longitude(Decimal.of(lng))
                .latitude(Decimal.of(lat))
                .build();

        CodeableConcept physicalType =
                CodeableConcept.builder()
                    .coding(Coding.builder()
                        .code(Code.of("bu"))
                        .system(net.sovrinhealth.fhir.model.type.Uri.of("http://terminology.hl7.org/CodeSystem/location-physical-type"))
                        .display(string("Building")).build())
                    .text(string("Any Building or structure. This may contain rooms, corridors, wings, etc. It might not have walls, or a roof, but is considered a defined/allocated space."))
                    .build();
        return Location.builder()
                .identifier(identifier)
                .status(LocationStatus.ACTIVE)
                .name(string("Synthetic Mass - Location - " + countOfLocationType))
                .description(string("A Synthetic Mass Location"))
                .mode(LocationMode.INSTANCE)
                .type(getLocationType())
                .address(addr)
                .physicalType(physicalType)
                .position(position)
                .build();
    }
}