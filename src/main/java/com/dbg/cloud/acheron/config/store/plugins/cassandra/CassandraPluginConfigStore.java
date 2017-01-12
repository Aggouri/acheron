package com.dbg.cloud.acheron.config.store.plugins.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class CassandraPluginConfigStore implements PluginConfigStore {

    private static final String TABLE = "plugins";
    private final CassandraOperations cassandraOperations;

    @Override
    public List<PluginConfig> findAll() {
        final Select select = QueryBuilder.select().from(TABLE);
        final List<CassandraPluginConfig> results = cassandraOperations.select(select, CassandraPluginConfig.class);
        return results.stream().map(cassandraPluginConfig -> cassandraPluginConfig).collect(Collectors.toList());
    }

    @Override
    public Optional<PluginConfig> findById(final @NonNull UUID pluginConfigId) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("id", pluginConfigId));

        final List<CassandraPluginConfig> cassandraPluginConfigs =
                cassandraOperations.select(select, CassandraPluginConfig.class);
        final Optional<PluginConfig> firstPluginConfig = cassandraPluginConfigs.stream().findFirst().map(
                cassandraConsumer -> cassandraConsumer);

        return firstPluginConfig;
    }

    @Override
    public List<PluginConfig> findByRoute(String routeId) {
        final Select select = QueryBuilder.select().from("plugins");
        select.where(QueryBuilder.eq("route_id", routeId));

        final List<CassandraPluginConfig> results = cassandraOperations.select(select, CassandraPluginConfig.class);
        return results.stream().map(cassandraPluginConfig -> cassandraPluginConfig).collect(Collectors.toList());
    }

    @Override
    public List<PluginConfig> findByConsumer(UUID consumerId) {
        final Select select = QueryBuilder.select().from("plugins");
        select.where(QueryBuilder.eq("consumer_id", consumerId));

        final List<CassandraPluginConfig> results = cassandraOperations.select(select, CassandraPluginConfig.class);
        return results.stream().map(cassandraPluginConfig -> cassandraPluginConfig).collect(Collectors.toList());
    }

    @Override
    public PluginConfig add(final @NonNull PluginConfig pluginConfig) {
        final UUID pluginConfigId = (pluginConfig.getId() == null) ? UUIDs.random() : pluginConfig.getId();
        final Date createdAt = (pluginConfig.getCreatedAt() == null) ? new Date() : pluginConfig.getCreatedAt();

        return cassandraOperations.insert(new CassandraPluginConfig(
                new CassandraPluginConfigPK(pluginConfigId, pluginConfig.getName()),
                pluginConfig.getRouteId(),
                pluginConfig.getConsumerId(),
                pluginConfig.getHttpMethods(),
                pluginConfig.getConfig(),
                pluginConfig.isEnabled(),
                createdAt));
    }

    @Override
    public PluginConfig update(final @NonNull PluginConfig pluginConfig) {
        final Date createdAt = (pluginConfig.getCreatedAt() == null) ? new Date() : pluginConfig.getCreatedAt();

        return cassandraOperations.update(new CassandraPluginConfig(
                new CassandraPluginConfigPK(pluginConfig.getId(), pluginConfig.getName()),
                pluginConfig.getRouteId(),
                pluginConfig.getConsumerId(),
                pluginConfig.getHttpMethods(),
                pluginConfig.getConfig(),
                pluginConfig.isEnabled(),
                createdAt));
    }

    @Override
    public void deleteById(final @NonNull UUID pluginConfigId) {
        final Optional<PluginConfig> pluginConfig = findById(pluginConfigId);
        if (pluginConfig.isPresent()) {
            cassandraOperations.deleteById(CassandraPluginConfig.class, new CassandraPluginConfigPK(
                    pluginConfigId, pluginConfig.get().getName()));
        }
    }
}