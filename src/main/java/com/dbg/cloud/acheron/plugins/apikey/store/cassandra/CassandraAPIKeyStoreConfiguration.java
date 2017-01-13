package com.dbg.cloud.acheron.plugins.apikey.store.cassandra;

import com.dbg.cloud.acheron.plugins.apikey.store.APIKeyStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.enabled", havingValue = "true")
public class CassandraAPIKeyStoreConfiguration {

    @Bean
    public APIKeyStore cassandraAPIKeyStore(CassandraOperations cassandraOperations) {
        return new CassandraAPIKeyStore(cassandraOperations);
    }
}
