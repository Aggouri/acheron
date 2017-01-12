package com.dbg.cloud.acheron.admin.pluginconfig;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AnyJSONSerializer extends JsonSerializer<AnyJSON> {
    @Override
    public void serialize(AnyJSON value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            final JsonNode node = new ObjectMapper().readTree(value.jsonValue());
            gen.writeObject(node);
        } catch (Exception e) {
            gen.writeStartObject();
            gen.writeEndObject();
        }
    }
}
