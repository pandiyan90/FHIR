#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021, 2022
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -o errexit
set -o nounset
set -o pipefail

DIST="${WORKSPACE}/build/notifications/kafka/workarea/volumes/dist"

# pre_integration
pre_integration(){
    cleanup
    config
    bringup
}

# config - update configuration
config(){
    mkdir -p ${DIST}/userlib
    mkdir -p ${DIST}/
    mkdir -p ${WORKSPACE}/build/notifications/kafka/workarea/output

    touch ${WORKSPACE}/build/notifications/kafka/workarea/output/fhir_notifications-messages.log
    chmod +rwx ${WORKSPACE}/build/notifications/kafka/workarea/output/fhir_notifications-messages.log
    chmod -R 777 ${WORKSPACE}/build/notifications/kafka/workarea/output/

    echo "Copying fhir configuration files..."
    cp -r ${WORKSPACE}/fhir-server-webapp/src/main/liberty/config/config ${DIST}
    cp -r ${WORKSPACE}/fhir-server-webapp/src/test/liberty/config/config/* ${DIST}/config

    echo "Copying test artifacts to install location..."
    USERLIB="${DIST}/userlib"
    mkdir -p $USERLIB
    find ${WORKSPACE}/conformance -iname 'fhir-ig*.jar' -not -iname 'fhir*-tests.jar' -not -iname 'fhir*-test-*.jar' -exec cp -f {} ${USERLIB} \;
    cp ${WORKSPACE}/operation/fhir-operation-test/target/fhir-operation-*.jar ${USERLIB}
    cp ${WORKSPACE}/term/operation/fhir-operation-term-cache/target/fhir-operation-*.jar ${USERLIB}
    echo "Finished copying fhir-server dependencies..."

    # Move over the test configurations
    cp -r ${WORKSPACE}/build/notifications/kafka/resources/* ${WORKSPACE}/build/notifications/kafka/workarea/volumes/dist/config/default/
    bash ${WORKSPACE}/build/common/update-server-registry-resource.sh ${WORKSPACE}/build/notifications/kafka/workarea/volumes/dist/config/default/fhir-server-config-notifications-cicd.json
    mv ${WORKSPACE}/build/notifications/kafka/workarea/volumes/dist/config/default/fhir-server-config-notifications-cicd.json ${WORKSPACE}/build/notifications/kafka/workarea/volumes/dist/config/default/fhir-server-config.json
}

# cleanup - cleanup existing docker
cleanup(){
    # Stand up a docker container running the fhir server configured for integration tests
    echo "Bringing down any containers that might already be running as a precaution"
    docker-compose kill
    docker-compose rm -f
}

# bringup
bringup(){
    echo "Bringing up containers"
    docker-compose up --remove-orphans -d
    echo ">>> Current time: " $(date)

    (docker-compose logs --timestamps --follow fhir-server & P=$! && sleep 60 && kill $P)

    # Gather up all the server logs so we can trouble-shoot any problems during startup
    cd -
    pre_it_logs=${WORKSPACE}/pre-it-logs
    zip_file=${WORKSPACE}/pre-it-logs.zip
    rm -rf ${pre_it_logs} 2>/dev/null
    mkdir -p ${pre_it_logs}
    rm -f ${zip_file}

    echo "
    Docker container status:"
    docker ps -a

    containerId=$(docker ps -a | grep kafka_fhir-server_1 | cut -d ' ' -f 1)
    if [[ -z "${containerId}" ]]; then
        echo "Warning: Could not find the fhir container!!!"
    else
        echo "fhir container id: ${containerId}"

        # Grab the container's console log
        docker logs ${containerId} > ${pre_it_logs}/docker-console.txt

        echo "Gathering pre-test server logs from docker container: ${containerId}"
        docker cp -L ${containerId}:/logs ${pre_it_logs}
    fi

    # Wait until the fhir server is up and running...
    echo "Waiting for fhir-server to complete initialization..."
    healthcheck_url='https://localhost:9443/fhir-server/api/v4/$healthcheck'
    tries=0
    status=0
    while [ $status -ne 200 -a $tries -lt 30 ]; do
        tries=$((tries + 1))

        set +o errexit
        cmd="curl -k -o ${WORKSPACE}/health.json --max-time 5 -I -w "%{http_code}" -u fhiruser:change-password $healthcheck_url"
        echo "Executing[$tries]: $cmd"
        status=$($cmd)
        set -o errexit

        echo "Status code: $status"
        if [ $status -ne 200 ]
        then
            echo "Sleeping 30 secs..."
            sleep 30
        fi
    done

    if [ $status -ne 200 ]
    then
        echo "Could not establish a connection to the fhir-server within $tries REST API invocations!"
        exit 1
    fi

    echo "The fhir-server appears to be running..."

    # Create the FHIR_notifications topic
    docker-compose -f build/notifications/kafka/docker-compose.yml exec kafka-1 bash /bin/kafka-topics \
        --bootstrap-server kafka-1:19092,kafka-2:29092 --command-config /etc/kafka/secrets/client-ssl.properties \
        --create --topic FHIR_NOTIFICATIONS --partitions 10 --replication-factor 2
    [ $? -eq 0 ] || exit 9

    echo "Topic is created 'FHIR_NOTIFICATIONS'"
    exit 0
}

# is_ready_to_run - is this ready to run?
is_ready_to_run(){
    echo "Preparing environment for fhir-server integration tests..."
    if [ -z "${WORKSPACE}" ]
    then
        echo "ERROR: WORKSPACE environment variable not set!"
        exit 1
    fi
}

###############################################################################
is_ready_to_run

cd build/notifications/kafka
pre_integration

# EOF
###############################################################################
