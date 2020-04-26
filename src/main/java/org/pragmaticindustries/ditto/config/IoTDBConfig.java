package org.pragmaticindustries.ditto.config;

public class IoTDBConfig {

    private String host = "";
    private int port = 6667;
    private String user = "";
    private String password = "";

    public IoTDBConfig() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
        return "IoTDBConfig{" +
            "host='" + host + '\'' +
            ", port=" + port +
            ", user='" + user + '\'' +
            ", password='" + password + '\'' +
            '}';
    }
}
