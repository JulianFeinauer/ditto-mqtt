package org.pragmaticindustries.ditto.config;

public class Configuration {

    private PlcConfig plc = new PlcConfig();
    private DittoConfig ditto = new DittoConfig();
    private MqttConfiguration mqtt = new MqttConfiguration();
    private IoTDBConfig iotdb = new IoTDBConfig();

    public Configuration() {
    }

    public PlcConfig getPlc() {
        return plc;
    }

    public void setPlc(PlcConfig plc) {
        this.plc = plc;
    }

    public DittoConfig getDitto() {
        return ditto;
    }

    public void setDitto(DittoConfig ditto) {
        this.ditto = ditto;
    }

    public MqttConfiguration getMqtt() {
        return mqtt;
    }

    public void setMqtt(MqttConfiguration mqtt) {
        this.mqtt = mqtt;
    }

    public IoTDBConfig getIotdb() {
        return iotdb;
    }

    public void setIotdb(IoTDBConfig iotdb) {
        this.iotdb = iotdb;
    }

    @Override
    public String toString() {
        return "Configuration{" +
            "plc=" + plc +
            ", ditto=" + ditto +
            ", mqtt=" + mqtt +
            ", iotdb=" + iotdb +
            '}';
    }
}
