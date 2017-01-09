package com.dbg.cloud.acheron.plugins.oauth2.cassandra;

import com.dbg.cloud.acheron.plugins.oauth2.OAuth2Store;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.enabled", havingValue = "true")
public class CassandraOAuth2StoreConfiguration {

    @Bean
    public OAuth2Store cassandraOAuth2Store(CassandraOperations cassandraOperations) {
        return new CassandraOAuth2Store(cassandraOperations);
    }
}
