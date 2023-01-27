/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.cassandra;


/**
 * Contact point for a Cassandra database
 */
public class ContactPoint {

    private final String host;
    private final int port;
    
    /**
     * Public constructor
     * @param host
     * @param port
     */
    public ContactPoint(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
}
