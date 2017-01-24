package com.dbg.cloud.acheron.filters.pre.traffic;

import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.dbg.cloud.acheron.plugins.ratelimiting.RateLimit;
import com.dbg.cloud.acheron.plugins.ratelimiting.RateLimitService;
import com.dbg.cloud.acheron.plugins.ratelimiting.RateLimitingConfig;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class RateLimitingFilter extends PreFilter {

    private final RateLimitService rateLimitService;

    @Override
    public int filterOrder() {
        return 50;
    }

    @Override
    public Object run() {
        final String routeId = getRouteId();
        final String consumerId = getConsumerId();
        final Optional<UUID> consumerUUID =
                consumerId != null ? Optional.of(UUID.fromString(consumerId)) : Optional.empty();

        final RatePolicy routePolicy = parseRouteConfig();
        final RatePolicy consumerPolicy = parseConsumerConfig();

        // Verify route rate policy
        if (routePolicy.getLimitRequestsPerWindow().isPresent()) {
            final RateLimit rateLimit = rateLimitService.consumeRate(routeId,
                    routePolicy.getLimitRequestsPerWindow().get(), routePolicy.getWindowInSeconds());
            if (!verifyRatingLimit(rateLimit)) {
                throwFailure(429, "{ error: \"Too many requests\" }");
            }
        }

        // Verify consumer rate policy
        if (consumerPolicy.getLimitRequestsPerWindow().isPresent()) {
            final RateLimit rateLimit = rateLimitService.consumeRate(routeId, consumerUUID.get(),
                    consumerPolicy.getLimitRequestsPerWindow().get(), consumerPolicy.getWindowInSeconds());

            final RequestContext context = RequestContext.getCurrentContext();
            context.addZuulResponseHeader("X-Rate-Limit", rateLimit.getLimitRequestsPerWindow().toString());
            context.addZuulResponseHeader("X-Rate-Remaining",
                    rateLimit.getRemainingRequestsInWindow().toString());

            // Rate limit should be zero if we have remaining requests
            final Long rateReset =
                    rateLimit.getRemainingRequestsInWindow() <= 0L ? rateLimit.getResetTimeInSeconds() : 0L;
            context.addZuulResponseHeader("X-Rate-Reset", rateReset.toString());

            if (!verifyRatingLimit(rateLimit)) {
                throwFailure(429, "{ error: \"Too many requests\" }");
            }
        }

        return null;
    }

    private boolean verifyRatingLimit(RateLimit rateLimit) {
        return rateLimit.getRemainingRequestsInWindow() > 0;
    }

    @Override
    public boolean shouldFilter() {
        return isEnabled("plugins.rate_limiting.enabled");
    }

    private RatePolicy parseConfig(final String key) {
        // Retrieve rate limit
        Optional<Long> limitPerWindow = Optional.empty();
        int windowInSec = 1; // 1 second by default
        final String sConfig = (String) RequestContext.getCurrentContext().get(key);
        try {
            if (sConfig != null && !sConfig.isEmpty()) {
                final ObjectMapper mapper = new ObjectMapper();
                final RateLimitingConfig config = mapper.readValue(sConfig, RateLimitingConfig.class);
                if (config.getLimitRequestsPerWindow() != null && config.getLimitRequestsPerWindow() > 0) {
                    limitPerWindow = Optional.of(config.getLimitRequestsPerWindow());
                }

                if (config.getWindowInSeconds() != null && config.getWindowInSeconds() > 0) {
                    windowInSec = config.getWindowInSeconds();
                }
            }
        } catch (JsonParseException | JsonMappingException e) {
            log.info("Configuration of Rate Limiting plugin is incorrect: " + sConfig, e);
            throwInternalServerError();
        } catch (IOException e) {
            log.error("Error occured while unmarshalling Rate Limiting plugin configuration: " + sConfig, e);
            throwInternalServerError();
        }

        return new RatePolicy(limitPerWindow, windowInSec);
    }

    private RatePolicy parseConsumerConfig() {
        return parseConfig("plugins_on_consumer.rate_limiting.config");
    }

    private RatePolicy parseRouteConfig() {
        return parseConfig("plugins.rate_limiting.config");
    }

    @AllArgsConstructor
    @Getter
    private class RatePolicy {
        final Optional<Long> limitRequestsPerWindow;
        final int windowInSeconds;
    }
}
