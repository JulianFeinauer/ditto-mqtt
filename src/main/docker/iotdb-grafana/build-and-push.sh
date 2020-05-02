#!/usr/bin/env bash
#if [[ ! -f ~/Develop/incubator-iotdb/grafana/target/iotdb-grafana-*.war ]]; then
#    echo "File not found, starting new build"
#    mvn clean package -DskipTests -f ~/Develop/incubator-iotdb/pom.xml
#fi
rm target/*
cp ~/Develop/incubator-iotdb/grafana/target/iotdb-grafana-*.war target/iotdb-grafana.war
cp ~/Develop/incubator-iotdb/grafana/src/main/resources/application.properties target/
docker image build -t jfeinauer/iotdb-grafana:0.9.3 . \
    && docker push jfeinauer/iotdb-grafana:0.9.3