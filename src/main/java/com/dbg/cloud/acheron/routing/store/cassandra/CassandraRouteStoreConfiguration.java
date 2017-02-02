package com.dbg.cloud.acheron.routing.store.cassandra;

import com.dbg.cloud.acheron.routing.store.RouteStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.routing", havingValue = "true")
public class CassandraRouteStoreConfiguration {

    @Bean
    public RouteStore cassandraRouteStore(CassandraOperations cassandraOperations) {
        return new CassandraRouteStore(cassandraOperations);
    }
}
