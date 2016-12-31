package com.dbg.cloud.acheron.filters.pre;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.filters.AcheronFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PreFilter extends AcheronFilter {

    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * Convenience method for adding headers to the request.
     *
     * @param name  header name
     * @param value header value
     */
    protected void addRequestHeader(@NonNull final String name, final String value) {
        RequestContext.getCurrentContext().addZuulRequestHeader(name, value);
    }

    /**
     * Convenience method for removing headers from the request.
     *
     * @param name header name
     */
    protected void removeRequestHeader(final String name) {
        RequestContext.getCurrentContext().getZuulRequestHeaders().remove(name);
    }

    /**
     * Convenience method for setting the API consumer id and fail if a different one is already set.
     *
     * @param consumerId the consumer id
     */
    protected void setUniqueConsumerIdOrFail(@NonNull final String consumerId) throws RuntimeException {
        final String alreadySet = getConsumerId();
        if (alreadySet != null && !alreadySet.equals(consumerId)) {
            log.info("Attempted to set a consumer id that is not the same as the one already set.");
            throwFailure(400, "{ \"error\": \"Bad request\" }");
        } else {
            RequestContext.getCurrentContext().set(AcheronRequestContextKeys.CONSUMER_ID, consumerId);
        }
    }
}
