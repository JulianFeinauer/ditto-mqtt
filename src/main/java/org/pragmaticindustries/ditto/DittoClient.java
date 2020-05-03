package org.pragmaticindustries.ditto;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DittoClient implements AutoCloseable {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DittoClient.class);

    public static final String CHECK_EXISTS =
        "{\n" +
            "    \"topic\": \"%s/%s/things/twin/commands/retrieve\",\n" +
            "    \"path\": \"/\",\n" +
            "    \"headers\": {\n" +
            "        \"response-required\": true,\n" +
            "        \"reply-to\": \"ditto/replies/%s\",\n" +
            "        \"correlation-id\": \"%3$s\"\n" +
            "    }\n" +
            "}";

    public static final String RETRIEVE_FEATURE =
        "{\n" +
            "    \"topic\": \"%s/%s/things/twin/commands/retrieve\",\n" +
            "    \"path\": \"/features/%s/properties\",\n" +
            "    \"headers\": {\n" +
            "        \"response-required\": true,\n" +
            "        \"reply-to\": \"ditto/replies/%s\",\n" +
            "        \"correlation-id\": \"%4$s\"\n" +
            "    }\n" +
            "}";

    public static final String CREATE =
        "{\n" +
            "    \"topic\": \"%s/%s/things/twin/commands/create\",\n" +
            "    \"path\": \"/\",\n" +
            "    \"headers\": {\n" +
            "        \"response-required\": true,\n" +
            "        \"reply-to\": \"ditto/replies/%s\",\n" +
            "        \"correlation-id\": \"%3$s\"\n" +
            "    },\n" +
            "   \"value\": {}\n" +
            "}";

    public static final String MODIFY_FEATURES =
        "{\n" +
            "  \"topic\": \"%s/%s/things/twin/commands/modify\",\n" +
            "    \"headers\": {\n" +
            "        \"response-required\": true,\n" +
            "        \"reply-to\": \"ditto/replies/%3$s\",\n" +
            "        \"correlation-id\": \"%s\"\n" +
            "    },\n" +
            "  \"path\": \"/features/%s\",\n" +
            "  \"value\": {\n" +
            "        \"properties\": {}\n" +
            "  }\n" +
            "}";

    public static final String MODIFY_SINGLE_PROPERTY =
        "{\n" +
            "  \"topic\": \"%s/%s/things/twin/commands/modify\",\n" +
            "    \"headers\": {\n" +
            "        \"response-required\": true,\n" +
            "        \"reply-to\": \"ditto/replies/%3$s\",\n" +
            "        \"correlation-id\": \"%s\"\n" +
            "    },\n" +
            "  \"path\": \"/features/%s/properties/%s\",\n" +
            "  \"value\": %s\n" +
            "}";

    public static final String MODIFY_MULTIPLE_PROPERTIES =
        "{\n" +
            "  \"topic\": \"%s/%s/things/twin/commands/modify\",\n" +
            "    \"headers\": {\n" +
            "        \"response-required\": true,\n" +
            "        \"reply-to\": \"ditto/replies/%3$s\",\n" +
            "        \"correlation-id\": \"%s\"\n" +
            "    },\n" +
            "  \"path\": \"/features/%s/properties\",\n" +
            "  \"value\": {" +
            "    %s" +
            "  }\n" +
            "}";

    public static final String MODIFY_MULTIPLE_PROPERTIES_NEW =
        "{\n" +
            "  \"topic\": \"%s/%s/things/twin/commands/modify\",\n" +
            "    \"headers\": {\n" +
            "        \"response-required\": true,\n" +
            "        \"reply-to\": \"ditto/replies/%3$s\",\n" +
            "        \"correlation-id\": \"%s\"\n" +
            "    },\n" +
            "  \"path\": \"/features/%s/properties/values\",\n" +
            "  \"value\": {" +
            "    %s" +
            "  }\n" +
            "}";

    private final Mqtt3BlockingClient client;

    public DittoClient() throws InterruptedException {
        this(Mqtt3Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("farmer.cloudmqtt.com")
            .serverPort(23081)
            .sslConfig(MqttClientSslConfig.builder().build())
            .simpleAuth(Mqtt3SimpleAuth.builder().username("ditto").password("ditto".getBytes()).build())
            .buildBlocking());
    }

    public DittoClient(Mqtt3BlockingClient mqttClient) {
        this.client = mqttClient;
        mqttClient.connect();
    }

    public boolean createFeature(String namespace, String thingId, String featureName) throws InterruptedException {
        final JSONObject jsonObject = requestReply(correlationId -> {
            final String s = String.format(MODIFY_FEATURES, namespace, thingId, correlationId, featureName);
            return s;
        }, json -> {
            logger.trace("Response {}", json.toString());
            return json;
        });

        int status = jsonObject.getInt("status");

        if (status == 201 || status == 204) {
            return true;
        } else {
            final JSONObject value = jsonObject.getJSONObject("value");
            final String message = value.getString("message");

            throw new RuntimeException(message);
        }
    }

    public boolean modifyMultipleProperties(String namespace, String thingId, String featureName, List<String> properties, List<Object> vals) throws InterruptedException {
        final JSONObject jsonObject = requestReply(correlationId -> {

            // Merge map
            final String map = IntStream.range(0, properties.size())
                .mapToObj(i -> {
                    return String.format("\"%s\": %s", properties.get(i), normalize(vals.get(i)));
                })
                .collect(Collectors.joining(",\n"));

            final String s = String.format(MODIFY_MULTIPLE_PROPERTIES, namespace, thingId, correlationId, featureName, map);
            return s;
        }, json -> {
            logger.trace("Response {}", json.toString());
            return json;
        });

        int status = jsonObject.getInt("status");

        if (status == 201 || status == 204) {
            return true;
        } else {
            final JSONObject value = jsonObject.getJSONObject("value");
            final String message = value.getString("message");

            throw new RuntimeException(message);
        }
    }

    public boolean modifyMultipleProperties2(String namespace, String thingId, String featureName, List<String> properties, List<Object> vals) throws InterruptedException {
        final JSONObject jsonObject = requestReply(correlationId -> {

            // Merge map
            final String map = IntStream.range(0, properties.size())
                .mapToObj(i -> {
                    return String.format("\"%s\": { \"v\": %s, \"t\": %d }", properties.get(i), normalize(vals.get(i)), Instant.now().toEpochMilli());
                })
                .collect(Collectors.joining(",\n"));

            final String s = String.format(MODIFY_MULTIPLE_PROPERTIES_NEW, namespace, thingId, correlationId, featureName, map);
            return s;
        }, json -> {
            logger.trace("Response {}", json.toString());
            return json;
        });

        int status = jsonObject.getInt("status");

        if (status == 201 || status == 204) {
            return true;
        } else {
            final JSONObject value = jsonObject.getJSONObject("value");
            final String message = value.getString("message");

            throw new RuntimeException(message);
        }
    }

    public boolean modifyFeature(String namespace, String thingId, String featureName, String propertyName, Object val) throws InterruptedException {
        final JSONObject jsonObject = requestReply(correlationId -> {
            final String s = String.format(MODIFY_SINGLE_PROPERTY, namespace, thingId, correlationId, featureName, propertyName, val);
            return s;
        }, json -> {
            logger.trace("Response {}", json.toString());
            return json;
        });

        int status = jsonObject.getInt("status");

        if (status == 201 || status == 204) {
            return true;
        } else {
            final JSONObject value = jsonObject.getJSONObject("value");
            final String message = value.getString("message");

            throw new RuntimeException(message);
        }
    }

    public boolean createDevice(String namespace, String thingId) throws InterruptedException {
        final JSONObject jsonObject = requestReply(correlationId -> {
            return String.format(CREATE, namespace, thingId, correlationId);
        }, json -> {
            logger.trace("Response {}", json.toString());
            return json;
        });

        int status = jsonObject.getInt("status");

        if (status == 201) {
            return true;
        } else {
            final JSONObject value = jsonObject.getJSONObject("value");
            final String message = value.getString("message");

            throw new RuntimeException(message);
        }
    }

    public JSONObject getDevice(String namespace, String thingId) throws InterruptedException {
        JSONObject jsonObject = requestReply(correlationId -> String.format(CHECK_EXISTS, namespace, thingId, correlationId),
            json -> {
                logger.trace("Response {}", json.toString());
                return json;
            });

        if (jsonObject.getInt("status") != 200) {
            throw new RuntimeException("");
        }

        return jsonObject;
    }

    public JSONObject getFeature(String namespace, String thingId, String featureName) throws InterruptedException {
        JSONObject jsonObject = requestReply(correlationId -> String.format(RETRIEVE_FEATURE, namespace, thingId, featureName, correlationId),
            json -> {
                logger.trace("Response {}", json.toString());
                return json;
            });

        if (jsonObject.getInt("status") != 200) {
            throw new RuntimeException("");
        }

        return jsonObject;
    }

    public boolean checkDeviceExists(String namespace, String thingId) throws InterruptedException {
        Integer status = requestReply(correlationId -> String.format(CHECK_EXISTS, namespace, thingId, correlationId),
            json -> {
                logger.trace("Response {}", json.toString());
                return json.getInt("status");
            });

        if (status == null) {
            return false;
        }

        return status == 200;
    }

    private  <T> T requestReply(Function<String, String> requestHandler, Function<JSONObject, T> responseHandler) throws InterruptedException {
        String correlationId = UUID.randomUUID().toString();

        try (final Mqtt3BlockingClient.Mqtt3Publishes publishes = client.publishes(MqttGlobalPublishFilter.SUBSCRIBED)) {

            client.subscribeWith()
                .topicFilter("ditto/replies/" + correlationId)
                .qos(MqttQos.AT_LEAST_ONCE)
                .send();

            client.publishWith()
                .topic("ditto/incoming")
                .payload(requestHandler.apply(correlationId).getBytes())
                .send();

            Optional<Mqtt3Publish> msgOptional = publishes
                .receive(10, TimeUnit.SECONDS);

            if (!msgOptional.isPresent()) {
                throw new RuntimeException("Time Out while waiting for Response from Ditto...");
            }

            Mqtt3Publish msg = msgOptional.get();

            String s = new String(msg.getPayloadAsBytes());

            JSONObject json = new JSONObject(s);

            String responseId = json.getJSONObject("headers").getString("correlation-id");

            if (responseId.equals(correlationId)) {
                logger.debug("Got a valid response... ");
                return responseHandler.apply(json);
            } else {
                logger.debug("Something bad happened...");
                return null;
            }
        } finally {
            client.unsubscribeWith()
                .addTopicFilter("ditto/replies/" + correlationId)
                .send();
        }
    }

    private static String normalize(Object v) {
        if (v instanceof String) {
            return "\"" + v + "\"";
//            if ("true".equals(v)) {
//                return "1";
//            } else {
//                return "0";
//            }
        }
        return v.toString();
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.disconnect();
        }
    }
}
