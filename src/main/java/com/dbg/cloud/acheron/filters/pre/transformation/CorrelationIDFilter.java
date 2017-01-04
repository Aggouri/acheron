package com.dbg.cloud.acheron.filters.pre.transformation;

import com.dbg.cloud.acheron.AcheronHeaders;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public final class CorrelationIDFilter extends PreFilter {
    @Override
    public int filterOrder() {
        return 50;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        final String customHeaderName = (String) context.get("plugins.correlation_id.config.custom_header_name");

        final String correlationIDHeaderName = customHeaderName != null ?
                customHeaderName : AcheronHeaders.CORRELATION_ID;
        final UUID correlationId = UUID.randomUUID();

        // this also removes any previously set header
        context.addZuulRequestHeader(correlationIDHeaderName, correlationId.toString());

        log.info("Adding correlation id: {}", correlationId.toString());

        return null;
    }

    @Override
    public boolean shouldFilter() {
        return isEnabled("plugins.correlation_id.enabled");
    }
}
