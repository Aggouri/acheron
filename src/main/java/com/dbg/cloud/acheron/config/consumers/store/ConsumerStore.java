package com.dbg.cloud.acheron.config.consumers.store;

import com.dbg.cloud.acheron.config.consumers.Consumer;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsumerStore {

    Optional<Consumer> findById(@NonNull UUID consumerId);

    List<Consumer> findAll();

    Consumer add(@NonNull Consumer consumer);

    void deleteById(@NonNull UUID consumerId);
}
