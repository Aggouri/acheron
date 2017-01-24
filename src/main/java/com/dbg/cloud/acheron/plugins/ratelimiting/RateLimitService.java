package com.dbg.cloud.acheron.plugins.ratelimiting;

import lombok.NonNull;

import java.util.UUID;

public interface RateLimitService {

    RateLimit consumeRate(@NonNull String routeId, @NonNull Long limitRequestsPerWindow, int windowInSeconds);

    RateLimit consumeRate(@NonNull String routeId, @NonNull UUID consumerId, @NonNull Long limitRequestsPerWindow,
                          int windowInSeconds);
}
