package com.dbg.cloud.acheron.config.store.consumers;

import java.util.Optional;
import java.util.UUID;

public interface ConsumerStore {

    Optional<Consumer> findById(UUID consumerId);
}
