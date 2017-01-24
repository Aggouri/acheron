package com.dbg.cloud.acheron.plugins.ratelimiting.store.cassandra;

import com.dbg.cloud.acheron.plugins.ratelimiting.store.RateLimitStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.enabled", havingValue = "true")
public class CassandraRateLimitStoreConfiguration {

    @Bean
    public RateLimitStore rateLimitStore(CassandraOperations cassandraOperations) {
        return new CassandraRateLimitStore(cassandraOperations);
    }
}
