package org.pragmaticindustries.ditto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.pragmaticindustries.ditto.config.MqttConfiguration;

class MqttConfigurationTest {

    @Test
    void readWrite() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        final String s = mapper.writeValueAsString(new MqttConfiguration());

        System.out.println(s);

        final MqttConfiguration config = mapper.readValue(s, MqttConfiguration.class);

        System.out.println(config);
    }
}