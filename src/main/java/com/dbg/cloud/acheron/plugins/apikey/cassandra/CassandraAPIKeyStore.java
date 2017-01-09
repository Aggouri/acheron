package com.dbg.cloud.acheron.plugins.apikey.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.plugins.apikey.APIKeyStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public final class CassandraAPIKeyStore implements APIKeyStore {

    private final CassandraOperations cassandraOperations;

    @Override
    public Optional<Consumer> findConsumerByAPIKey(final @NonNull String apiKey) {
        UUID uuidAPIKey = null;
        try {
            uuidAPIKey = UUID.fromString(apiKey);
        } catch (Exception e) {
            log.error("Passed apiKey is not a UUID.");
        }

        if (uuidAPIKey == null) {
            return Optional.ofNullable(null);
        }

        final Select select = QueryBuilder.select().from("api_key_keys");
        select.where(QueryBuilder.eq("api_key", uuidAPIKey));

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
