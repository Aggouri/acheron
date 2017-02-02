package com.dbg.cloud.acheron.plugins.ratelimiting.service;

import com.dbg.cloud.acheron.plugins.ratelimiting.RateLimit;
import com.dbg.cloud.acheron.plugins.ratelimiting.store.RateLimitStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
final class RateLimitServiceImpl implements RateLimitService {

    private final RateLimitStore rateLimitStore;

    @Override
    public RateLimit consumeRate(final @NonNull String routeId, final @NonNull Long limitRequestsPerWindow,
                                 final int windowInSeconds) {

        // Route limits
        final long currentRequestsInWindow = rateLimitStore.countConsumptionOfRoute(routeId);
        final long remainingInWindow = limitRequestsPerWindow - currentRequestsInWindow;

        if (remainingInWindow > 0) {
            // Register request
            rateLimitStore.addConsumptionToRoute(routeId, windowInSeconds);
        }

        // Calculate reset time
        final Optional<Long> earliestEntry = rateLimitStore.retrieveTimeOfEarliestConsumptionOfRoute(routeId);
        final long reset = calculateResetTime(earliestEntry, windowInSeconds);

        return new RateLimit(limitRequestsPerWindow, Math.max(remainingInWindow - 1, 0), windowInSeconds, reset);
    }

    @Override
    public RateLimit consumeRate(final String routeId, final @NonNull UUID consumerId,
                                 final @NonNull Long limitRequestsPerWindow,
                                 final int windowInSeconds) {

        // Consumer limits
        final long currentRequestsInWindow = rateLimitStore.countConsumptionOfConsumer(routeId, consumerId);
        final long remainingInWindow = limitRequestsPerWindow - currentRequestsInWindow;

        if (remainingInWindow > 0) {
            // Register request
            rateLimitStore.addConsumptionToConsumer(routeId, consumerId, windowInSeconds);
        }

        // Calculate reset time
        final Optional<Long> earliestEntry = rateLimitStore.retrieveTimeOfEarliestConsumptionOfConsumer(
                routeId, consumerId);
        final long reset = calculateResetTime(earliestEntry, windowInSeconds);

        return new RateLimit(limitRequestsPerWindow, Math.max(remainingInWindow - 1, 0), windowInSeconds,
                Math.abs(reset));
    }

    private long calculateResetTime(final Optional<Long> earliestEntry, int windowInSeconds) {
        final long reset;
        if (earliestEntry.isPresent()) {
            final long elapsed = System.currentTimeMillis() - earliestEntry.get();
            reset = Math.max((windowInSeconds * 1000) - elapsed, 0);
        } else {
            reset = 0;
        }

        double resetInSec = Math.ceil(reset / 1000.0);
        return (long) resetInSec;
    }
}
