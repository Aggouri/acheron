package com.dbg.cloud.acheron.admin.pluginconfig;

import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
final class PluginConfigTO {

    public PluginConfigTO(final PluginConfig pluginConfig) {
        this(safeUUIDString(pluginConfig.getId()), pluginConfig.getName(), pluginConfig.getRouteId(),
                safeUUIDString(pluginConfig.getConsumerId()), pluginConfig.getConfig(), pluginConfig.isEnabled(),
                pluginConfig.getHttpMethods(), pluginConfig.getCreatedAt());
    }

    private static String safeUUIDString(final UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @JsonView({View.Merge.class, View.Replace.class})
    private final String id;

    @JsonView(View.Create.class)
    private final String name;

    @JsonView(View.Create.class)
    @JsonProperty("route_id")
    private final String routeId;

    @JsonView(View.Create.class)
    @JsonProperty("consumer_id")
    private final String consumerId;

    @JsonView(View.Create.class)
    private final String config;

    @JsonView({View.Create.class, View.Merge.class})
    private final Boolean enabled;

    @JsonView({View.Create.class, View.Merge.class})
    @JsonProperty("http_methods")
    private final Set<String> httpMethods;

    @JsonProperty("created_at")
    private final Date createdAt;
}
