package com.dbg.cloud.acheron.config.store.plugins;

import java.util.List;
import java.util.UUID;

public interface PluginConfigStore {

    List<PluginConfig> findByRoute(String routeId);

    List<PluginConfig> findByConsumer(UUID consumerId);
}
