package org.pragmaticindustries.ditto.config;

public class DittoConfig {

    private String namespace = "";
    private String thingId = "";
    private String featureName = "";

    public DittoConfig() {
        // For Jackson
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getThingId() {
        return thingId;
    }

    public void setThingId(String thingId) {
        this.thingId = thingId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    @Override
    public String toString() {
        return "DittoConfig{" +
            "namespace='" + namespace + '\'' +
            ", thingId='" + thingId + '\'' +
            ", featureName='" + featureName + '\'' +
            '}';
    }
}
