package com.dbg.cloud.acheron.filters.pre.authorization;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

/**
 * This filter requires an authentication filter to be set before it.
 */
@Slf4j
public class ACLFilter extends PreFilter {
    @Override
    public int filterOrder() {
        return 40;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        final String routeId = (String) context.get(AcheronRequestContextKeys.ROUTE_ID);
        final String consumerId = getConsumerId();

        // Must know who the API consumer is
        if (consumerId == null || consumerId.trim().isEmpty()) {
            // We need to know who called us --> this is done with authentication filters
            log.info("Probably a configuration error. Consumer id is unknown.");
            throwFailure(500, "{ \"error\": \"Internal server error\" }");
        }

        // API consumer must be in required role
        if (!isConsumerInRequiredRole(consumerId, routeId)) {
            log.info("Consumer is not in required role");
            throwFailure(403, "{ \"error\": \"Access is forbidden\" }");
        }

        return null;
    }

    @Override
    public boolean shouldFilter() {
        return isEnabled("plugins.acl.enabled");
    }

    protected boolean isConsumerInRequiredRole(final String consumerId, final String routeId) {
        // TODO Implement proper ACL checks
        return true;
    }
}
