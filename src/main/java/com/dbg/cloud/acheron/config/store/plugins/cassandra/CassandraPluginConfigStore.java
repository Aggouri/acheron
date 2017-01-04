package com.dbg.cloud.acheron.config.store.plugins.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import lombok.AllArgsConstructor;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CassandraPluginConfigStore implements PluginConfigStore {

    private final CassandraOperations cassandraOperations;

    @Override
    public List<PluginConfig> findByRoute(String routeId) {
        final Select select = QueryBuilder.select().from("plugins");
        select.where(QueryBuilder.eq("route_id", routeId));

        List<CassandraPluginConfig> results = cassandraOperations.select(select, CassandraPluginConfig.class);
        return results.stream().map(CassandraPluginConfigStore::toPluginConfig).collect(Collectors.toList());
    }

    @Override
    public List<PluginConfig> findByConsumer(UUID consumerId) {
        final Select select = QueryBuilder.select().from("plugins");
        select.where(QueryBuilder.eq("consumer_id", consumerId));

        List<CassandraPluginConfig> results = cassandraOperations.select(select, CassandraPluginConfig.class);
        return results.stream().map(CassandraPluginConfigStore::toPluginConfig).collect(Collectors.toList());
    }

    private static PluginConfig toPluginConfig(CassandraPluginConfig cassandraPluginConfig) {
        return cassandraPluginConfig;
    }
}