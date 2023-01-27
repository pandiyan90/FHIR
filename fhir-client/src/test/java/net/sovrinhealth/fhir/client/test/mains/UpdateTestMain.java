/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.client.test.mains;

import static net.sovrinhealth.fhir.model.type.String.string;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.generator.FHIRGenerator;
import net.sovrinhealth.fhir.model.resource.Observation;
import net.sovrinhealth.fhir.model.resource.Observation.Component;
import net.sovrinhealth.fhir.model.type.Decimal;
import net.sovrinhealth.fhir.model.type.Quantity;
import net.sovrinhealth.fhir.provider.FHIRProvider;

public class UpdateTestMain {
    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newBuilder()
                .register(new FHIRProvider(RuntimeType.CLIENT))
                .build();
        
        WebTarget target = client.target("http://localhost:9080/fhir-server/api/v4");
        Response response = target.path("Observation/14").request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        
        Observation observation = response.readEntity(Observation.class);
        
        FHIRGenerator.generator( Format.JSON, false).generate(observation, System.out);
        
                
        List <Component> newCompList = new ArrayList<Component>();
        int i = 0;
        for (Component component: observation.getComponent()) {
            // change first component only
            if ( i == 0 ) {
                component = component.toBuilder()
                        .value(Quantity.builder().value(Decimal.of(120)).unit(string("mmHg")).build()).build(); 
            } 
            newCompList.add(component);
            i++;
        }
        
        observation = observation.toBuilder().component(newCompList).build();
        
        Entity<Observation> observationEntity = Entity.entity(observation, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = target.path("Observation/14").request().put(observationEntity);
        System.out.println(response.getStatusInfo().getReasonPhrase());
        System.out.println("location: " + response.getLocation());
    }
}
