package com.dbg.cloud.acheron.filters.pre.transformation;

import com.dbg.cloud.acheron.AcheronHeaders;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.dbg.cloud.acheron.plugins.correlationid.CorrelationIdConfig;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public final class CorrelationIDFilter extends PreFilter {
    @Override
    public int filterOrder() {
        return 60;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();

        // Retrieve custom header name
        Optional<String> customHeaderName = Optional.empty();
        final String sConfig = (String) context.get("plugins.correlation_id.config");
        try {
            if (sConfig != null && !sConfig.isEmpty()) {
                final ObjectMapper mapper = new ObjectMapper();
                final CorrelationIdConfig config = mapper.readValue(sConfig, CorrelationIdConfig.class);
                if (config.getCustomHeaderName() != null && !config.getCustomHeaderName().isEmpty()) {
                    customHeaderName = Optional.of(config.getCustomHeaderName());
                    // TODO: Disallow some sensitive header names
                }
            }
        } catch (JsonParseException | JsonMappingException e) {
            log.info("Configuration of Correlation ID plugin is incorrect: " + sConfig, e);
            throwInternalServerError();
        } catch (IOException e) {
            log.error("Error occured while unmarshalling Correlation ID plugin configuration: " + sConfig, e);
            throwInternalServerError();
        }

        final String correlationIDHeaderName = customHeaderName.orElse(AcheronHeaders.CORRELATION_ID);
        final UUID correlationId = UUID.randomUUID();

        // this also removes any previously set header
        context.addZuulRequestHeader(correlationIDHeaderName, correlationId.toString());

        log.info("Adding correlation id: {} in header: {}", correlationId.toString(), correlationIDHeaderName);

        return null;
    }

    @Override
    public boolean shouldFilter() {
        return isEnabled("plugins.correlation_id.enabled");
    }
}
