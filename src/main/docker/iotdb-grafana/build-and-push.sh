#!/usr/bin/env bash
#if [[ ! -f ~/Develop/incubator-iotdb/grafana/target/iotdb-grafana-*.war ]]; then
#    echo "File not found, starting new build"
#    mvn clean package -DskipTests -f ~/Develop/incubator-iotdb/pom.xml
#fi
rm target/*
cp run.sh target/
cp -R ~/Develop/incubator-iotdb/grafana/target/iotdb-grafana-*.war target/
cp -R ~/Develop/incubator-iotdb/grafana/conf/application.properties target/
docker image build -t jfeinauer/iotdb-grafana:0.9.1 . \
    && docker push jfeinauer/iotdb-grafana:0.9.1