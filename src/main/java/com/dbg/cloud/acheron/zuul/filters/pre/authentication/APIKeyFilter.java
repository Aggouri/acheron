package com.dbg.cloud.acheron.zuul.filters.pre.authentication;

import com.dbg.cloud.acheron.consumers.Consumer;
import com.dbg.cloud.acheron.zuul.filters.pre.PreFilter;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKeyStore;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public final class APIKeyFilter extends PreFilter {

    public static final String HEADER_API_KEY = "API_KEY";

    private final APIKeyStore apiKeyStore;

    @Override
    public int filterOrder() {
        return 30;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        final String apiKey = context.getRequest().getHeader(HEADER_API_KEY);

        final Optional<String> consumerId = resolveConsumerId(apiKey);

        if (!consumerId.isPresent()) {
            throwFailure(401, "{ error: \"Invalid API key\" }");
        }

        setUniqueConsumerIdOrFail(consumerId.get());

        // TODO Decide whether to keep the header
        removeRequestHeader(HEADER_API_KEY);

        return null;
    }

    @Override
    public boolean shouldFilter() {
        return isEnabled("plugins.api_key.enabled");
    }

    private Optional<String> resolveConsumerId(final String apiKey) {
        final String consumerId;
        if (apiKey != null) {
            final Optional<Consumer> optionalConsumer = apiKeyStore.findConsumerByAPIKey(apiKey);

            if (optionalConsumer.isPresent() && optionalConsumer.get().getId() != null) {
                final Consumer consumer = optionalConsumer.get();
                consumerId = optionalConsumer.get().getId().toString();
                log.info("Determined caller is consumer with name = {} and id = {}", consumer.getName(),
                        consumer.getId());
            } else {
                consumerId = null;
                log.info("API key does not correspond to any consumer");
            }
        } else {
            consumerId = null;
        }

        return Optional.ofNullable(consumerId);
    }
}
