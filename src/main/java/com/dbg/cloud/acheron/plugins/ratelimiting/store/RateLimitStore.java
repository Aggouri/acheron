package com.dbg.cloud.acheron.plugins.ratelimiting.store;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface RateLimitStore {

    void addConsumptionToRoute(@NonNull String routeId, int windowInSeconds);

    long countConsumptionOfRoute(@NonNull String routeId);

    Optional<Long> retrieveTimeOfEarliestConsumptionOfRoute(@NonNull String routeId);

    void addConsumptionToConsumer(@NonNull String routeId, @NonNull UUID consumerId, int windowInSeconds);

    long countConsumptionOfConsumer(@NonNull String routeId, @NonNull UUID consumerId);

    Optional<Long> retrieveTimeOfEarliestConsumptionOfConsumer(@NonNull String routeId, @NonNull UUID consumerId);
}
