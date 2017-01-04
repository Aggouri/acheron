package com.dbg.cloud.acheron.config.store.plugins.cassandra;

import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.plugins", havingValue = "true")
public class CassandraPluginConfigStoreConfiguration {

    @Bean
    public PluginConfigStore pluginConfigStore(CassandraOperations cassandraOperations) {
        return new CassandraPluginConfigStore(cassandraOperations);
    }
}
