package org.pragmaticindustries.ditto.config;

import java.util.HashMap;
import java.util.Map;

public class PlcConfig {

    private int scrapeRateMs = 1000;
    private String connectionString = "";
    private Map<String, String> plcFields = new HashMap<>();

    public PlcConfig() {
    }

    public PlcConfig(int scrapeRateMs, String connectionString, Map<String, String> plcFields) {
        this.scrapeRateMs = scrapeRateMs;
        this.connectionString = connectionString;
        this.plcFields = plcFields;
    }

    public int getScrapeRateMs() {
        return scrapeRateMs;
    }

    public void setScrapeRateMs(int scrapeRateMs) {
        this.scrapeRateMs = scrapeRateMs;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public Map<String, String> getPlcFields() {
        return plcFields;
    }

    public void setPlcFields(Map<String, String> plcFields) {
        this.plcFields = plcFields;
    }

    @Override
    public String toString() {
        return "PlcConfig{" +
            "scrapeRateMs=" + scrapeRateMs +
            ", connectionString='" + connectionString + '\'' +
            ", plcFields=" + plcFields +
            '}';
    }
}
