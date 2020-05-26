package org.pragmaticindustries.ditto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author julian
 * Created by julian on 18.05.20
 */
public class Plc4XVortoDitto {

    private static final Logger logger = LoggerFactory.getLogger(Plc4XVortoDitto.class);

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

        ArrayNode stereotypes = getConfigProperties(mappingJson);

        logger.info("========================");
        logger.info("Configuration Properties");
        logger.info("========================");

        for (JsonNode stereotype : stereotypes) {
            String name = stereotype.get("name").asText();
            String type = stereotype.get("type").asText();

            logger.info("Property {} - {}", name, type);

            Optional<JsonNode> mappingAttributes = getStereotype(stereotype);

            if (mappingAttributes.isPresent()) {
                String address = mappingAttributes.get().get("address").asText();
                long rate = Long.parseLong(mappingAttributes.get().get("rate").asText());
                String url = mappingAttributes.get().get("url").asText();

                logger.info("Mapping {} - {} - {}", url, address, rate);
            } else {
                logger.info("No mapping given, will be ignored...");
            }
        }

        ObjectNode dittoConfig = (ObjectNode) dittoJson.get("features")
            .get("simulatedplctwo")
            .get("properties")
            .get("configuration");

        logger.info(dittoJson.toPrettyString());
    }

    private static Optional<JsonNode> getStereotype(JsonNode mappingJson) {
        return Optional.ofNullable(mappingJson.get("stereotypes"))
            .map(json -> json.get(0))
            .map(json -> json.get("attributes"));
    }

    private static ArrayNode getConfigProperties(JsonNode mappingJson) {
        return mappingJson.get("models")
            .get("org.apache.plc4x.examples:SimulatedPlcTwo:1.0.0")
            .withArray("configurationProperties");
    }
}
