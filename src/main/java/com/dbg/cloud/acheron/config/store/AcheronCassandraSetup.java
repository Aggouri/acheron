package com.dbg.cloud.acheron.config.store;

import com.datastax.driver.core.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.enabled", havingValue = "true")
@EnableConfigurationProperties(CassandraProperties.class)
public class AcheronCassandraSetup {

    private final CassandraProperties cassandraProperties;

    @Autowired
    public AcheronCassandraSetup(final CassandraProperties cassandraProperties) {
        this.cassandraProperties = cassandraProperties;
    }

    @Bean
    public Cluster cluster() {
        return Cluster.builder()
                .addContactPoints(cassandraProperties.getContactPoints())
                .withPort(cassandraProperties.getPort())
                .build();
    }

    @Bean
    public CassandraOperations cassandraTemplate(Cluster cluster) {
        return new CassandraTemplate(cluster.connect("acheron"));
    }
}
