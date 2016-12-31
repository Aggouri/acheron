package com.dbg.cloud.acheron.filters.pre.authentication;

import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class APIKeyFilter extends PreFilter {

    public static final String HEADER_API_KEY = "API_KEY";

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
        return isEnabled("api_key.config.enabled");
    }

    private Optional<String> resolveConsumerId(final String apiKey) {
        // FIXME Implement proper API Key logic
        String consumerId = null;

        if (apiKey != null) {
            final String pattern = "SECRET_(?<CONSUMER>.+)";
            final Matcher matcher = Pattern.compile(pattern).matcher(apiKey);
            consumerId = matcher.matches() ? matcher.group("CONSUMER") : null;
        }

        return Optional.ofNullable(consumerId);
    }
}
