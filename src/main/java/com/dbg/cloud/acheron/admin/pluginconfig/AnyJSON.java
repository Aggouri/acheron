package com.dbg.cloud.acheron.admin.pluginconfig;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AnyJSON {
    private String json;

    public String jsonValue() {
        return json;
    }
}
