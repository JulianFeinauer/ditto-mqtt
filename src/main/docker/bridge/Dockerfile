FROM openjdk:8-jre
MAINTAINER Julian

CMD mkdir -p /opt/service/
COPY maven/*.jar /opt/service/

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "/opt/service/*", "org.pragmaticindustries.ditto.DittoMqttIoTDBBridge"]
