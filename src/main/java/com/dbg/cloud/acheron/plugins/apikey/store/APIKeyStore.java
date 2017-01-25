package com.dbg.cloud.acheron.plugins.apikey.store;

import com.dbg.cloud.acheron.config.consumers.Consumer;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface APIKeyStore {

    Optional<APIKey> findById(@NonNull UUID apiKeyId);

    List<APIKey> findByConsumer(@NonNull UUID consumerId);

    APIKey add(@NonNull APIKey apiKey);

    void deleteById(@NonNull UUID apiKeyId);

    Optional<Consumer> findConsumerByAPIKey(@NonNull String apiKey);
}
