package com.dbg.cloud.acheron.config.store;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(prefix = "store", value = "cassandra.enabled", havingValue = "true")
@EnableConfigurationProperties(CassandraProperties.class)
@Slf4j
public class AcheronCassandraSetup {

    private final CassandraProperties cassandraProperties;
    private final int retryCount;
    private final int waitTimeBetweenRetriesInSec;

    public AcheronCassandraSetup(final CassandraProperties cassandraProperties) {
        this.cassandraProperties = cassandraProperties;
        this.retryCount = cassandraProperties.getInitialConnectionRetryCount() != null ?
                cassandraProperties.getInitialConnectionRetryCount() : 0;
        this.waitTimeBetweenRetriesInSec = cassandraProperties.getWaitTimeBeforeRetriesInSec() != null ?
                cassandraProperties.getWaitTimeBeforeRetriesInSec() : 5;
    }

    @Bean
    public CassandraOperations cassandraTemplate() {
        Session session = null;
        RuntimeException lastException = new IllegalStateException();

        int remainingTries = 1 + retryCount;

        while (session == null && remainingTries > 0) {
            try {
                session = cluster().connect("acheron");
            } catch (final NoHostAvailableException e) {
                lastException = e;
                log.info("Cassandra is not yet accepting connections. Retrying in {} seconds.",
                        waitTimeBetweenRetriesInSec);
            } catch (final InvalidQueryException e) {
                lastException = e;
                log.info("Acheron DDL is not yet applied. Retrying in {} seconds.", waitTimeBetweenRetriesInSec);
            } finally {
                remainingTries--;

                if (session == null && remainingTries > 0) {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(waitTimeBetweenRetriesInSec));
                    } catch (final InterruptedException e) {
                        //
                    }
                }
            }
        }

        if (session == null) {
            throw lastException;
        }

        return new CassandraTemplate(session);
    }

    private Cluster cluster() {
        return Cluster.builder()
                .addContactPoints(cassandraProperties.getContactPoints())
                .withPort(cassandraProperties.getPort())
                .build();
    }
}
