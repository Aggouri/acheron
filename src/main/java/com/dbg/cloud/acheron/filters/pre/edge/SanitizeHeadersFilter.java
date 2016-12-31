package com.dbg.cloud.acheron.filters.pre.edge;

import com.dbg.cloud.acheron.AcheronHeaders;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;

/**
 * Removes all incoming headers that will potentially be set by our filters, i.e. prevents header injections.
 */
public class SanitizeHeadersFilter extends PreFilter {

    @Override
    public int filterOrder() {
        return 11;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        removeRequestHeader(AcheronHeaders.CORRELATION_ID);
        removeRequestHeader(AcheronHeaders.OAUTH2_SUBJECT);
        removeRequestHeader(AcheronHeaders.OAUTH2_CLIENT_ID);

        return null;
    }
}
