package com.dbg.cloud.acheron.pluginconfig.endpoints;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AnyJSON {
    private String json;

    public String jsonValue() {
        return json;
    }
}
