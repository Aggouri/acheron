package com.dbg.cloud.acheron.config.store.plugins;

import java.util.Date;
import java.util.UUID;

public interface PluginConfig {

    UUID getId();

    String getName();

    String getRouteId();

    UUID getConsumerId();

    String getConfig();

    boolean isEnabled();

    Date getCreatedAt();
}
