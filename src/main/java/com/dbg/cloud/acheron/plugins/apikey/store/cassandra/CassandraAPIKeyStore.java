package com.dbg.cloud.acheron.plugins.apikey.store.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.dbg.cloud.acheron.config.consumers.Consumer;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKey;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKeyStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public final class CassandraAPIKeyStore implements APIKeyStore {

    private static final String TABLE = "api_key_keys";
    private final CassandraOperations cassandraOperations;

    @Override
    public Optional<APIKey> findById(final @NonNull UUID apiKeyId) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("id", apiKeyId));

        final List<CassandraAPIKey> cassandraAPIKeys = cassandraOperations.select(select, CassandraAPIKey.class);
        final Optional<APIKey> firstAPIKey = cassandraAPIKeys.stream().findFirst().map(
                cassandraAPIKey -> cassandraAPIKey);

        return firstAPIKey;
    }

    @Override
    public List<APIKey> findByConsumer(final @NonNull UUID consumerId) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("consumer_id", consumerId));

        final List<CassandraAPIKey> results = cassandraOperations.select(select, CassandraAPIKey.class);
        return results.stream().map(cassandraAPIKey -> cassandraAPIKey).collect(Collectors.toList());
    }

    @Override
    public APIKey add(final @NonNull APIKey apiKey) {
        final UUID apiKeyId = (apiKey.getId() == null) ? UUIDs.random() : apiKey.getId();
        final String apiKeyKey = (apiKey.getAPIKey() == null ? UUIDs.random().toString() : apiKey.getAPIKey());
        final Date createdAt = (apiKey.getCreatedAt() == null) ? new Date() : apiKey.getCreatedAt();

        return cassandraOperations.insert(new CassandraAPIKey(apiKeyId, apiKeyKey, apiKey.getConsumerId(),
                apiKey.getConsumerName(), apiKey.getConsumerCreatedAt(), createdAt));
    }

    @Override
    public void deleteById(final @NonNull UUID apiKeyId) {
        cassandraOperations.deleteById(CassandraAPIKey.class, apiKeyId);
    }

    @Override
    public Optional<Consumer> findConsumerByAPIKey(final @NonNull String apiKey) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("api_key", apiKey));

        final List<CassandraAPIKey> apiKeys = cassandraOperations.select(select, CassandraAPIKey.class);
        final Optional<CassandraAPIKey> firstAPIKey = apiKeys.stream().findFirst();

        final Optional<Consumer> consumer;
        if (firstAPIKey.isPresent()) {
            final CassandraAPIKey key = firstAPIKey.get();
            consumer = Optional.of(new Consumer.Smart(key.getConsumerId(), key.getConsumerName(), key
                    .getConsumerCreatedAt()));
        } else {
            consumer = Optional.ofNullable(null);
        }

        return consumer;
    }
}
