package com.dbg.cloud.acheron.config.plugins.store;

import com.dbg.cloud.acheron.config.plugins.PluginConfig;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginConfigStore {

    List<PluginConfig> findAll();

    Optional<PluginConfig> findById(@NonNull UUID pluginId);

    List<PluginConfig> findByRoute(@NonNull String routeId);

    List<PluginConfig> findByConsumer(@NonNull UUID consumerId);

    PluginConfig add(@NonNull PluginConfig pluginConfig);

    PluginConfig update(@NonNull PluginConfig pluginConfig);

    void deleteById(@NonNull UUID pluginConfigId);
}
