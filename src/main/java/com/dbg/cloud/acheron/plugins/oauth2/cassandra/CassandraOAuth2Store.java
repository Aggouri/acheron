package com.dbg.cloud.acheron.plugins.oauth2.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2Store;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public final class CassandraOAuth2Store implements OAuth2Store {

    private final CassandraOperations cassandraOperations;

    @Override
    public Optional<Consumer> findConsumerByClientId(final @NonNull String clientId) {
        UUID uuidClientId = null;
        try {
            uuidClientId = UUID.fromString(clientId);
        } catch (Exception e) {
            log.error("Passed apiKey is not a UUID.");
        }

        if (uuidClientId == null) {
            return Optional.ofNullable(null);
        }

        final Select select = QueryBuilder.select().from("oauth2_clients");
        select.where(QueryBuilder.eq("client_id", uuidClientId));

        final List<CassandraOAuth2Client> clients = cassandraOperations.select(select, CassandraOAuth2Client.class);
        final Optional<CassandraOAuth2Client> firstClient = clients.stream().findFirst();

        final Optional<Consumer> consumer;
        if (firstClient.isPresent()) {
            final CassandraOAuth2Client client = firstClient.get();
            consumer = Optional.of(new Consumer.Smart(client.getConsumerId(), client.getConsumerName(), client
                    .getConsumerCreatedAt()));
        } else {
            consumer = Optional.ofNullable(null);
        }

        return consumer;
    }
}
