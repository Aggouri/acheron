package com.dbg.cloud.acheron.admin.pluginconfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

final class AnyJSONDeserializer extends JsonDeserializer<AnyJSON> {

    @Override
    public AnyJSON deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final TreeNode tree = jp.getCodec().readTree(jp);
        return new AnyJSON(tree.toString());
    }
}
