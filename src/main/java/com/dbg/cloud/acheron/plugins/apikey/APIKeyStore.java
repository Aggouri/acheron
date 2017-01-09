package com.dbg.cloud.acheron.plugins.apikey;

import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import lombok.NonNull;

import java.util.Optional;

public interface APIKeyStore {

    Optional<Consumer> findConsumerByAPIKey(@NonNull String apiKey);
}
