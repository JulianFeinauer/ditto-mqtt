#!/usr/bin/env bash
export IOTDB_IP=`getent hosts ${IOTDB_HOST} | awk '{ print $1 }'`
echo "Resolved IP Adress to ${IOTDB_IP}"
java -Dspring.datasource.url=jdbc:iotdb://${IOTDB_IP}:${IOTDB_PORT} \
    -Dspring.datasource.username=${IOTDB_USER} \
    -Dspring.datasource.password=${IOTDB_PASSWORD} \
    -jar /iotdb/iotdb-grafana-0.9.1.war
