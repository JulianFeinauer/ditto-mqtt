# How to start
Necessary Environment Variables are:
* IOTDB_HOST 
* IOTDB_PORT
* IOTDB_USER
* IOTDB_PASSWORD
with no defaults. So to run it against your local installation do
```
 docker run -d -e IOTDB_HOST=localhost -e IOTDB_PORT=6667 -e IOTDB_USER=root -e IOTDB_PASSWORD=root --net=host --name iotdb-grafana jfeinauer/iotdb-grafana:0.9.1  
```