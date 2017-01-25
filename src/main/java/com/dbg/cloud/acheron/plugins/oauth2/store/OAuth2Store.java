package com.dbg.cloud.acheron.plugins.oauth2.store;

import com.dbg.cloud.acheron.config.consumers.Consumer;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OAuth2Store {

    Optional<OAuth2Client> findById(@NonNull UUID id);

    List<OAuth2Client> findByConsumer(@NonNull UUID consumerId);

    OAuth2Client add(@NonNull OAuth2Client client);

    void deleteById(@NonNull UUID id);

    Optional<Consumer> findConsumerByClientId(@NonNull String clientId);
}
