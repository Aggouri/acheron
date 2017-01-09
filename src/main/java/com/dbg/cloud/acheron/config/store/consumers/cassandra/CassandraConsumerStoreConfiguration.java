package com.dbg.cloud.acheron.config.store.consumers.cassandra;

import com.dbg.cloud.acheron.config.store.consumers.ConsumerStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.consumers", havingValue = "true")
public class CassandraConsumerStoreConfiguration {

    @Bean
    public ConsumerStore consumerStore(CassandraOperations cassandraOperations) {
        return new CassandraConsumerStore(cassandraOperations);
    }
}
