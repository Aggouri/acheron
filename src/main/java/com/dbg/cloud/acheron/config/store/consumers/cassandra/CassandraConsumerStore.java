package com.dbg.cloud.acheron.config.store.consumers.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.config.store.consumers.ConsumerStore;
import lombok.AllArgsConstructor;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class CassandraConsumerStore implements ConsumerStore {

    private final CassandraOperations cassandraOperations;

    @Override
    public Optional<Consumer> findById(final UUID consumerId) {
        final Select select = QueryBuilder.select().from("consumers");
        select.where(QueryBuilder.eq("id", consumerId));

        final List<Consumer> cassandraConsumers = cassandraOperations.select(select, Consumer.class);
        final Optional<Consumer> firstConsumer = cassandraConsumers.stream().findFirst();

        return firstConsumer;
    }
}
