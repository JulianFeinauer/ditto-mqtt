package org.pragmaticindustries.ditto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

/**
 * TODO write comment
 *
 * @author julian
 * Created by julian on 18.05.20
 */
public class Plc4XVortoDitto {

    public static void main(String[] args) throws IOException {
        // Phase 1 - Fetch Data from Vorto
        HttpClient client = new HttpClient();
        GetMethod getDittoJson = new GetMethod("https://vorto.eclipse.org/api/v1/generators/eclipseditto/models/org.apache.plc4x.examples.SimulatedPlcTwo:1.0.0?target=thingJson");
        GetMethod getMapping = new GetMethod("https://vorto.eclipse.org/api/v1/models/org.apache.plc4x.examples.SimulatedPlcTwo:1.0.0/content/simulatedplc");

        client.executeMethod(getDittoJson);

        String dittoJsonString = getDittoJson.getResponseBodyAsString();
        getDittoJson.releaseConnection();
        client.executeMethod(getMapping);
        String mappingJsonString = getMapping.getResponseBodyAsString();
        getMapping.releaseConnection();

        ObjectMapper om = new ObjectMapper();
        JsonNode dittoJson = om.reader().readTree(dittoJsonString);
        JsonNode mappingJson = om.reader().readTree(mappingJsonString);

        JsonNode mappingAttributes = getStereotypes(mappingJson).get("attributes");

        String address = mappingAttributes.get("address").asText();
        String rate = mappingAttributes.get("rate").asText();
        String url = mappingAttributes.get("url").asText();

        ObjectNode dittoConfig = (ObjectNode) dittoJson.get("features")
            .get("simulatedplctwo")
            .get("properties")
            .get("configuration");

        dittoConfig.put("address", address);
        dittoConfig.put("rate", rate);
        dittoConfig.put("url", url);

        System.out.println(dittoJson.toPrettyString());
    }

    private static JsonNode getStereotypes(JsonNode mappingJson) {
        return ((ArrayNode) getConfigProperties(mappingJson)
            .get("stereotypes")).get(0);
    }

    private static JsonNode getConfigProperties(JsonNode mappingJson) {
        return ((ArrayNode) mappingJson.get("models")
            .get("org.apache.plc4x.examples:SimulatedPlcTwo:1.0.0")
            .get("configurationProperties")).get(0);
    }
}
