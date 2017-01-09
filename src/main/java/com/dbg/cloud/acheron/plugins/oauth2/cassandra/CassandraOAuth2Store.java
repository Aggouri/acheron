package com.dbg.cloud.acheron.plugins.oauth2.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2Store;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public final class CassandraOAuth2Store implements OAuth2Store {

    private final CassandraOperations cassandraOperations;

    @Override
    public Optional<Consumer> findConsumerByClientId(final @NonNull String clientId) {
        final Select select = QueryBuilder.select().from("oauth2_clients");
        select.where(QueryBuilder.eq("id", clientId));

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
