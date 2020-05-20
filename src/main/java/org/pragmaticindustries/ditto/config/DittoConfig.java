package org.pragmaticindustries.ditto.config;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DittoConfig {

    private String namespace = "";
    private String thingId = "";
    private String featureName = "";

    public DittoConfig() {
        // For Jackson
    }

    public DittoConfig(String namespace, String thingId, String featureName) {
        this.namespace = namespace;
        this.thingId = thingId;
        this.featureName = featureName;
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
