package com.dbg.cloud.acheron.plugins.oauth2.store.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.dbg.cloud.acheron.config.consumers.Consumer;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Client;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Store;
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
public final class CassandraOAuth2Store implements OAuth2Store {

    private final String TABLE = "oauth2_clients";
    private final CassandraOperations cassandraOperations;

    @Override
    public Optional<OAuth2Client> findById(final @NonNull UUID id) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("id", id));

        final List<CassandraOAuth2Client> clients = cassandraOperations.select(select, CassandraOAuth2Client.class);
        final Optional<OAuth2Client> first = clients.stream().findFirst().map(client -> client);

        return first;
    }

    @Override
    public List<OAuth2Client> findByConsumer(final @NonNull UUID consumerId) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("consumer_id", consumerId));

        final List<CassandraOAuth2Client> results = cassandraOperations.select(select, CassandraOAuth2Client.class);
        return results.stream().map(client -> client).collect(Collectors.toList());
    }

    @Override
    public OAuth2Client add(final @NonNull OAuth2Client client) {
        final UUID clientId = (client.getId() == null) ? UUIDs.random() : client.getId();
        final String clientClientId = (client.getClientId() == null ? UUIDs.random().toString() : client.getClientId());
        final Date createdAt = (client.getCreatedAt() == null) ? new Date() : client.getCreatedAt();

        return cassandraOperations.insert(new CassandraOAuth2Client(clientId, clientClientId, client.getConsumerId(),
                client.getConsumerName(), client.getConsumerCreatedAt(), createdAt));
    }

    @Override
    public void deleteById(final @NonNull UUID id) {
        cassandraOperations.deleteById(CassandraOAuth2Client.class, id);
    }

    @Override
    public Optional<Consumer> findConsumerByClientId(final @NonNull String clientId) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("client_id", clientId));

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
