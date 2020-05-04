package org.pragmaticindustries.ditto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.json.JSONObject;
import org.pragmaticindustries.ditto.config.Configuration;
import org.pragmaticindustries.ditto.config.PlcConfig;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Plc4XMqttProducer2 {

    public static void main(String[] args) throws Exception {
        // Load Configuration
        final ObjectMapper mapper = new ObjectMapper();
        final Configuration configuration = mapper.readValue(new File("src/main/resources/config-2.json"), Configuration.class);
//        final Configuration configuration = mapper.readValue(new File("/opt/service/config.json"), Configuration.class);

        // Read the Configuration
        final String namespace = configuration.getDitto().getNamespace();
        String thingId = configuration.getDitto().getThingId();
        String featureName = configuration.getDitto().getFeatureName();

        // Create a Ditto Client
        final DittoClient client = new DittoClient(getMqttClient(configuration));

        // Check if device exists, otherwise create it
        System.out.println("Using Device ID: " + thingId);
        if (!client.checkDeviceExists(namespace, thingId)) {
            System.out.println("Device does not exist... exiting");
            return;
        } else {
            System.out.println("Device exists...");
        }

        final JSONObject feature = client.getFeature(namespace, thingId, featureName);

        boolean hasConfig = false;
        if (feature.has("value")) {
            final JSONObject val = feature.getJSONObject("value");
            if (val.has("definition")) {
                final JSONObject definition = val.getJSONObject("definition");
                // Do something...
                final JSONObject fields = definition.getJSONObject("plcFields");
                final Map<String, String> fieldMap = fields.keySet().stream()
                    .collect(Collectors.toMap(
                        Function.identity(),
                        fields::getString
                    ));
                final PlcConfig plcConfig = new PlcConfig(definition.getInt("scrapeRateMs"), definition.getString("connectionString"), fieldMap);
                System.out.println("Using PLC Config:");
                System.out.println(plcConfig);
                configuration.setPlc(plcConfig);
                hasConfig = true;
            }
        }

        if (!hasConfig) {
            throw new RuntimeException("Unable to load PLC Config...");
        }

        // Now create feature (or modify existing feature)
        // System.out.println("Create Feature " + featureName);
        // client.createFeature(namespace, thingId, featureName);

//        // Now add one property for each channel
//        for (Map.Entry<String, String> entry : channels.entrySet()) {
//            System.out.println("Add Property " + entry.getKey());
//            client.modifyFeature(namespace, thingId, featureName, entry.getKey(), "0.0");
//        }

        // Fill em in!
        final Map<String, String> channels = configuration.getPlc().getPlcFields();

        // Output the feature once
        System.out.println("Fetch complete Thing:");
        final JSONObject device = client.getDevice(namespace, thingId);
        System.out.println(device.toString(2));

        // Now go into the event loop and fetch values from the PLC
        System.out.println("Starting Update loop...");
        // Start PLC Connection
        for (int retries = 0; retries < Integer.MAX_VALUE; retries++) {
            System.out.print("x");
            try (final PlcConnection connection = new PlcDriverManager().getConnection(configuration.getPlc().getConnectionString())) {
                final Random random = new Random();
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    System.out.print(".");
                    final PlcReadRequest.Builder builder = connection.readRequestBuilder();
                    for (Map.Entry<String, String> entry : channels.entrySet()) {
                        // Read via PLC4X
                        builder.addItem(entry.getKey(), entry.getValue());
                    }
                    // Execute
                    final PlcReadResponse response = builder.build()
                        .execute()
                        .get(5, TimeUnit.SECONDS);

                    // Assemble values
                    final List<String> fieldNames = new ArrayList<>(response.getFieldNames());
                    final List<Object> vals = fieldNames.stream()
                        .map(response::getObject)
                        .collect(Collectors.toList());

                    // Send...
                    client.modifyMultipleProperties2(namespace, thingId, featureName, fieldNames, vals);

                    // Sleep
                    Thread.sleep(configuration.getPlc().getScrapeRateMs());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        client.close();
    }

    private static Mqtt3BlockingClient getMqttClient(Configuration configuration) {
        return Mqtt3Client.builder()
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
            .buildBlocking();
    }

}
