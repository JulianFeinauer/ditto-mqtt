FROM openjdk:11-jre-slim
LABEL maintainer="j.feinauer@pragmaticminds.de"
RUN mkdir /iotdb
ADD target/* /iotdb/
EXPOSE 8888
EXPOSE 4000
WORKDIR /iotdb/
ENTRYPOINT ["/iotdb/run.sh"]