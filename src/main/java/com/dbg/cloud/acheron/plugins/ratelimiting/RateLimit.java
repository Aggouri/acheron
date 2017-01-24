package com.dbg.cloud.acheron.plugins.ratelimiting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public final class RateLimit {

    private final Long limitRequestsPerWindow;

    private final Long remainingRequestsInWindow;

    private final Integer windowInSeconds;

    private final Long resetTimeInSeconds;
}
