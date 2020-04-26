package org.pragmaticindustries.ditto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.apache.iotdb.service.rpc.thrift.TSStatus;
import org.apache.iotdb.session.IoTDBSessionException;
import org.apache.iotdb.session.Session;
import org.json.JSONObject;
import org.pragmaticindustries.ditto.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DittoMqttIoTDBBridge {

    private static final Logger logger = LoggerFactory.getLogger(DittoMqttIoTDBBridge.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException, IoTDBSessionException, IOException {
        // Load Configuration
        final ObjectMapper mapper = new ObjectMapper();
//        final Configuration configuration = mapper.readValue(new File("src/main/resources/config.json"), Configuration.class);
        final Configuration configuration = mapper.readValue(new File("/opt/service/config.json"), Configuration.class);

        final Mqtt3AsyncClient client = Mqtt3Client.builder()
            .identifier("ditto-mqtt-iotdb-bridge-" + UUID.randomUUID().toString())
            .serverHost(configuration.getMqtt().getServer())
            .serverPort(configuration.getMqtt().getPort())
            .sslConfig(MqttClientSslConfig.builder().build())
            .simpleAuth(
                Mqtt3SimpleAuth.builder()
                    .username(configuration.getMqtt().getUser())
                    .password(configuration.getMqtt().getPassword().getBytes())
                    .build())
            .automaticReconnectWithDefaultConfig()
            .buildAsync();

        client.connect();

        // Start a Session
        final Session session = new Session(configuration.getIotdb().getHost(), configuration.getIotdb().getPort(), configuration.getIotdb().getUser(), configuration.getIotdb().getPassword());
        session.open();

        // Register Subscription
        client.subscribeWith()
            .topicFilter("ditto/events/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(publish -> {
                final JSONObject json = new JSONObject(new String(publish.getPayloadAsBytes()));
                logger.trace(json.toString(2));

                // Get Feature
                final String topic = json.getString("topic");
                final String path = json.getString("path");
                final Object value = json.get("value");
                final String timestamp = json.getString("timestamp");

                // Get Namespace / Thing
                Pattern pattern = Pattern.compile("^(.*?)/(.*?)/.*/(.*?)$");
                Matcher matcher = pattern.matcher(topic);
                if (!matcher.matches()) {
                    // Something fishy? Report log
                    logger.warn("Unable to match topic {} for message {}", topic, json.toString(2));
                    return;
                }
                final String namnespace = matcher.group(1);
                final String thingId = matcher.group(2);
                final String action = matcher.group(3);

                logger.debug("Action: {}", action);

                // Get Feature / Property
                pattern = Pattern.compile("/features/(.*)/properties/(.*)");
                matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    final String featureName = matcher.group(1);
                    final String property = matcher.group(2);

                    logger.info("{} - {} - {} - {} - {}", namnespace, thingId, featureName, property, value);

                    final Instant ts = Instant.parse(timestamp);

                    // Now we should do an IoTDB Query with it
                    final String stmt = String.format("INSERT INTO root.%s.%s.%s (timestamp, %s) VALUES (%d, %s)", namnespace, normalize(thingId), featureName, property, ts.toEpochMilli(), value);

                    logger.debug("Executing Statement (via Session API): ‘{}‘", stmt);

                    final String device = String.format("root.%s.%s.%s", normalize(namnespace), normalize(thingId), normalize(featureName));
                    try {
                        // Push using Session API
                        final TSStatus status = session.insert(device, ts.toEpochMilli(), Collections.singletonList(normalize(property)), Collections.singletonList(printValue(value)));

                        logger.debug("Result: {}", status);
                        if (status.statusType.code != 200) {
                            logger.warn("Unnable to inserst, probebly there is an issue?");
                        }
                    } catch (IoTDBSessionException e) {
                        e.printStackTrace();
                        // Try to reconnect
                        try {
                            session.close();
                            session.open();
                        } catch (IoTDBSessionException ioTDBSessionException) {
                            ioTDBSessionException.printStackTrace();
                        }
                    }
                }

                // Try to read whole feature
                pattern = Pattern.compile("^/features/(.*)/properties$");
                matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    final String featureName = matcher.group(1);

                    final JSONObject valueJson = ((JSONObject) value);

                    final List<String> names = new ArrayList<>(valueJson.keySet());

                    final List<String> valueList = names.stream()
                        .map(key -> valueJson.get(key).toString())
                        .collect(Collectors.toList());

                    final List<String> normalizedNames = names.stream()
                        .map(s -> normalize(s))
                        .collect(Collectors.toList());

                    logger.info("{} - {} - {} - {}", namnespace, thingId, featureName, value);

                    logger.info("Names: {}", names);
                    logger.info("Values: {}", valueList);

                    final Instant ts = Instant.parse(timestamp);

                    // Now we should do an IoTDB Query with it
                    final String device = String.format("root.%s.%s.%s", normalize(namnespace), normalize(thingId), normalize(featureName));
                    try {
                        // Push using Session API
                        final TSStatus status = session.insert(device, ts.toEpochMilli(), normalizedNames, valueList);

                        logger.debug("Result: {}", status);
                        if (status.statusType.code != 200) {
                            logger.warn("Unnable to inserst, probebly there is an issue?");
                        }
                    } catch (IoTDBSessionException e) {
                        e.printStackTrace();
                        // Try to reconnect
                        try {
                            session.close();
                            session.open();
                        } catch (IoTDBSessionException ioTDBSessionException) {
                            ioTDBSessionException.printStackTrace();
                        }
                    }
                }
            })
            .send()
            .get();

        // Register Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.disconnect();
            try {
                session.close();
            } catch (IoTDBSessionException e) {
                e.printStackTrace();
            }
        }));
    }

    private static String printValue(Object value) {
        return value.toString();
    }

    private static String normalize(String expression) {
        return expression
            .replace("-", "_")
            .replace("%", "_");
    }
}
