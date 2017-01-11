package com.dbg.cloud.acheron.config.store.plugins;

import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface PluginConfigStore {

    List<PluginConfig> findByRoute(@NonNull String routeId);

    List<PluginConfig> findByConsumer(@NonNull UUID consumerId);
}
