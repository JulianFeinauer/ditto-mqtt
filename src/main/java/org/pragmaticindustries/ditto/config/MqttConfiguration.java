package org.pragmaticindustries.ditto.config;

public class MqttConfiguration {

    private String server = "";
    private int port = 1883;
    private String user = "";
    private String password = "";

    public MqttConfiguration() {
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "MqttConfiguration{" +
            "server='" + server + '\'' +
            ", port=" + port +
            ", user='" + user + '\'' +
            ", password='" + password + '\'' +
            '}';
    }
}
