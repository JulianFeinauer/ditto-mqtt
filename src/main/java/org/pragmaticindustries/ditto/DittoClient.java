package org.pragmaticindustries.ditto;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.json.JSONObject;

import java.util.UUID;
import java.util.function.Function;

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

    public static final String MODIFY_SINGLE_FEATURES =
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

    private final Mqtt3BlockingClient client;

    public DittoClient() throws InterruptedException {
        client = Mqtt3Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("farmer.cloudmqtt.com")
            .serverPort(23081)
            .sslConfig(MqttClientSslConfig.builder().build())
            .simpleAuth(Mqtt3SimpleAuth.builder().username("ditto").password("ditto".getBytes()).build())
            .buildBlocking();

        client.connect();

//        final String deviceId = UUID.randomUUID().toString();
//        System.out.println("Device: " + deviceId);
//
//        if (!checkDeviceExists("org.pragmaticindustries", deviceId)) {
//            System.out.println("Device does not exist, creating...");
//            final boolean status = createDevice("org.pragmaticindustries", deviceId);
//
//            System.out.println("Status: " + status);
//        } else {
//            System.out.println("Device exists...");
//        }
//
//        // Create feature
//        System.out.println("Create Feature");
//        createFeature("org.pragmaticindustries", deviceId, "machine-part");
//        System.out.println(getDevice("org.pragmaticindustries", deviceId));
//        modifyFeature("org.pragmaticindustries", deviceId, "machine-part", "volume", 13);
//        System.out.println(getDevice("org.pragmaticindustries", deviceId));
//        modifyFeature("org.pragmaticindustries", deviceId, "machine-part", "volume-2", 13);
//        System.out.println(getDevice("org.pragmaticindustries", deviceId));
//        createFeature("org.pragmaticindustries", deviceId, "machine-part-2");
//        System.out.println(getDevice("org.pragmaticindustries", deviceId));
//        modifyFeature("org.pragmaticindustries", deviceId, "machine-part-2", "volume", 13);
//        System.out.println(getDevice("org.pragmaticindustries", deviceId));
//
//        System.out.println(getDevice("org.pragmaticindustries", deviceId));
//
//        System.out.println("Disconnecting");
//        client.disconnect();
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

    public boolean modifyFeature(String namespace, String thingId, String featureName, String propertyName, Object val) throws InterruptedException {
        final JSONObject jsonObject = requestReply(correlationId -> {
            final String s = String.format(MODIFY_SINGLE_FEATURES, namespace, thingId, correlationId, featureName, propertyName, val);
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

            Mqtt3Publish msg = publishes
                .receive();

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

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.disconnect();
        }
    }
}
