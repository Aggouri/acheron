package com.dbg.cloud.acheron.plugins.oauth2;

import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import lombok.NonNull;

import java.util.Optional;

public interface OAuth2Store {

    Optional<Consumer> findConsumerByClientId(@NonNull String clientId);
}
