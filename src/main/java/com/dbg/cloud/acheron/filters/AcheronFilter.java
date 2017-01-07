package com.dbg.cloud.acheron.filters;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AcheronFilter extends ZuulFilter {

    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * Stops request processing by throwing a runtime error. The HTTP response will have the given HTTP status code and
     * will contain the given body if it has not been already set by another filter.
     *
     * @param code HTTP Status Code
     * @param body Response text
     * @throws RuntimeException at all times
     */
    protected void throwFailure(final int code, final String body) throws RuntimeException {
        log.info("Reporting error ({}): {}", code, body);

        final RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(code);
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody(body);
            ctx.setSendZuulResponse(false);
        }
        throw new RuntimeException("Code: " + code + ", " + ctx.getResponseBody()); // to break execution
    }



    /**
     * Convenience method for checking that a boolean key is set to Boolean.TRUE in the request context.
     *
     * @param contextKey the boolean key to check
     * @return true if the given key is set to Boolean.TRUE
     */
    protected boolean isEnabled(final String contextKey) {
        return RequestContext.getCurrentContext().getBoolean(contextKey);
    }

    /**
     * Convenience method for obtaining the API consumer id.
     *
     * @return the consumer id
     */
    protected String getConsumerId() {
        return (String) RequestContext.getCurrentContext().get(AcheronRequestContextKeys.CONSUMER_ID);
    }

    /**
     * Convenience method to stop request execution and return with a 500 Internal Server Error.
     *
     * @throws RuntimeException at all times
     */
    protected void throwInternalServerError() throws RuntimeException {
        throwFailure(500, "{ \"error\": \"Internal server error\" }");
    }
}
