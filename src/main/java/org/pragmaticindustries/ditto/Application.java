package org.pragmaticindustries.ditto;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.json.JSONObject;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * TODO write comment
 *
 * @author julian
 * Created by julian on 24.04.20
 */
public class Application {

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

    public static void main(String[] args) throws InterruptedException {
        Mqtt3BlockingClient client = Mqtt3Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("farmer.cloudmqtt.com")
            .serverPort(23081)
            .sslConfig(MqttClientSslConfig.builder().build())
            .simpleAuth(Mqtt3SimpleAuth.builder().username("schleuninger").password("PragMinds2k17".getBytes()).build())
            .buildBlocking();

        client.connect();


//        String.format(CHECK_EXISTS, "org.pragmaticindustries", "device", correlationId)
//
//        String responseId = json.getJSONObject("headers").getString("correlation-id");
//
//        if (responseId.equals(correlationId)) {
//            System.out.println("Got a valid response... ");
//        }
//
//        System.out.println(s);
        for (int i = 0; i < 3; i++) {
            requestReply(client, correlationId -> String.format(CHECK_EXISTS, "org.pragmaticindustries", "device", correlationId), json -> {
                System.out.println(json);
            });
        }

        System.out.println("Disconnecting");
        client.disconnect();
        System.out.println("Disconncted");
    }

    private static void requestReply(Mqtt3BlockingClient client, Function<String, String> requestHandler, Consumer<JSONObject> responseHandler) throws InterruptedException {
        String correlationId = UUID.randomUUID().toString();

        client.subscribeWith()
            .topicFilter("ditto/replies/" + correlationId)
            .qos(MqttQos.AT_LEAST_ONCE)
            .send();

        client.publishWith()
            .topic("ditto/incoming")
            .payload(requestHandler.apply(correlationId).getBytes())
            .send();

        Mqtt3Publish msg = client.publishes(MqttGlobalPublishFilter.SUBSCRIBED)
            .receive();

        String s = new String(msg.getPayloadAsBytes());

        JSONObject json = new JSONObject(s);

        String responseId = json.getJSONObject("headers").getString("correlation-id");

        if (responseId.equals(correlationId)) {
            System.out.println("Got a valid response... ");
            responseHandler.accept(json);
        } else {
            System.out.println("Something bad happened...");
        }

        client.unsubscribeWith()
            .addTopicFilter("ditto/replies/" + correlationId)
            .send();
    }
}
