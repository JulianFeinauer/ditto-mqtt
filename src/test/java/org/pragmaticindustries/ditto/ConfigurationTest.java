package org.pragmaticindustries.ditto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.pragmaticindustries.ditto.config.Configuration;
import org.pragmaticindustries.ditto.config.DittoConfig;

class ConfigurationTest {

    @Test
    void serialize() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        final String s = mapper.writeValueAsString(new DittoConfig());

        System.out.println(s);

        System.out.println(new JSONObject(s).toString(2));
    }

    @Test
    void read() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        final String s = mapper.writeValueAsString(new DittoConfig());

        final DittoConfig config = mapper.readValue(s, DittoConfig.class);

        System.out.println(config);
    }

    @Test
    void readWrite() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        final String s = mapper.writeValueAsString(new Configuration());

        System.out.println(new JSONObject(s).toString(2));

        final Configuration config = mapper.readValue(s, Configuration.class);

        System.out.println(config);
    }
}