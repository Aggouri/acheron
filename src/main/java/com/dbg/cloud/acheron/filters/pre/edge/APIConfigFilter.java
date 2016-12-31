package com.dbg.cloud.acheron.filters.pre.edge;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;

public final class APIConfigFilter extends PreFilter {

    @Override
    public int filterOrder() {
        return 13;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();

        final String routeId = (String) context.get(AcheronRequestContextKeys.ROUTE_ID);

        // simulate realm concept
        // TODO Come up with multi-realm concept
        context.set(AcheronRequestContextKeys.REALM_ID, "realm1");

        // TODO Get config properly
        switch (routeId) {
            case "balances":
                context.set("api_key.config.enabled");
//                context.set("oauth2.config.enabled");
                context.set("correlation_id.config.enabled");
                break;
            case "accounts":
                context.set("api_key.config.enabled");
                context.set("oauth2.config.enabled");
                context.set("correlation_id.config.enabled");
                break;
            case "hydra_realm1":
                break;
            default:
        }

        return null;
    }
}
