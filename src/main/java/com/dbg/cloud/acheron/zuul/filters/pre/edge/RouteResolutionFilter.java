package com.dbg.cloud.acheron.zuul.filters.pre.edge;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.zuul.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

@Slf4j
public final class RouteResolutionFilter extends PreFilter {

    private final RouteLocator routeLocator;

    public RouteResolutionFilter(final RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public int filterOrder() {
        return 12;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();

        final String requestURI = new UrlPathHelper().getPathWithinApplication(context.getRequest());
        final Route matchingRoute = routeLocator.getMatchingRoute(requestURI);

        if (matchingRoute != null) {
            context.set(AcheronRequestContextKeys.ROUTE_ID, matchingRoute.getId());
            log.info("Resolved route id: {}", matchingRoute.getId());
        } else {
            log.error("Could not resolve route. Should raise an error!");
            throwBadRequest();
        }

        return null;
    }
}
