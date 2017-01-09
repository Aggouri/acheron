package com.dbg.cloud.acheron.config.store.consumers.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.config.store.consumers.ConsumerStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class CassandraConsumerStore implements ConsumerStore {

    private static final String TABLE = "consumers";
    private final CassandraOperations cassandraOperations;

    @Override
    public Optional<Consumer> findById(final @NonNull UUID consumerId) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("id", consumerId));

        final List<CassandraConsumer> cassandraConsumers = cassandraOperations.select(select, CassandraConsumer.class);
        final Optional<Consumer> firstConsumer = cassandraConsumers.stream().findFirst().map(
                cassandraConsumer -> cassandraConsumer);

        return firstConsumer;
    }

    @Override
    public List<Consumer> findAll() {
        final Select select = QueryBuilder.select().from(TABLE);
        final List<CassandraConsumer> results = cassandraOperations.select(select, CassandraConsumer.class);
        return results.stream().map(cassandraConsumer -> cassandraConsumer).collect(Collectors.toList());
    }

    @Override
    public Consumer add(final @NonNull Consumer consumer) {
        final UUID consumerId = (consumer.getId() == null) ? UUIDs.random() : consumer.getId();
        final Date createdAt = (consumer.getCreatedAt() == null) ? new Date() : consumer.getCreatedAt();

        return cassandraOperations.insert(new CassandraConsumer(consumerId, consumer.getName(), createdAt));
    }

    @Override
    public void deleteById(final @NonNull UUID consumerId) {
        cassandraOperations.deleteById(CassandraConsumer.class, consumerId);
    }
}
