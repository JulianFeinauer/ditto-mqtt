package org.pragmaticindustries.ditto;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Crawler {

    public static void main(String[] args) throws Exception {
        // Read the Configuration
        final Map<String, String> channels = new HashMap<>();

        // Init
        channels.put("current", "");
        channels.put("speed", "");

        String deviceName = UUID.randomUUID().toString();
        String featureName = "%DN444";

        final DittoClient client = new DittoClient();

        // Check if device exists, otherwise create it
        System.out.println("Using Device ID: " + deviceName);
        if (!client.checkDeviceExists("org.pragmaticindustries", deviceName)) {
            System.out.println("Device does not exist, creating...");
            final boolean status = client.createDevice("org.pragmaticindustries", deviceName);
            if (status) {
                System.out.println("Device was created successfully!");
            }
        } else {
            System.out.println("Device exists...");
        }

        // Now create feature (or modify existing feature)
        System.out.println("Create Feature " + featureName);
        client.createFeature("org.pragmaticindustries", deviceName, featureName);

        // Now add one property for each channel
        for (Map.Entry<String, String> entry : channels.entrySet()) {
            System.out.println("Add Property " + entry.getKey());
            client.modifyFeature("org.pragmaticindustries", deviceName, featureName, entry.getKey(), "\"NaN\"");
        }

        // Output the feature once
        System.out.println("Fetch complete Thing:");
        final JSONObject device = client.getDevice("org.pragmaticindustries", deviceName);
        System.out.println(device.toString(2));

        // Now go into the event loop and fetch values from the PLC
        System.out.println("Starting Update loop...");
        final Random random = new Random();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            System.out.print(".");
            for (Map.Entry<String, String> entry : channels.entrySet()) {
                client.modifyFeature("org.pragmaticindustries", deviceName, featureName, entry.getKey(), random.nextDouble());
            }
            Thread.sleep(1_000);
        }

        client.close();
    }

}
