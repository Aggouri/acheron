package com.dbg.cloud.acheron.filters.pre.edge;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.config.store.routing.RouteStore;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Stops request if HTTP method is not allowed on current route.
 */
@AllArgsConstructor
@Slf4j
public class HttpMethodFilter extends PreFilter {

    private final RouteStore routeStore;

    @Override
    public int filterOrder() {
        return 13;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        final String routeId = (String) context.get(AcheronRequestContextKeys.ROUTE_ID);
        final String method = context.getRequest().getMethod();

        if (!isMethodAllowedOnRoute(method, routeId)) {
            log.info("Method {} not allowed on route {}", method, routeId);
            throwBadRequest();
        }

        return null;
    }

    private boolean isMethodAllowedOnRoute(final @NonNull String method, final @NonNull String routeId) {
        return retrieveAllowedMethodsOnRoute(routeId).stream().anyMatch(
                allowedMethod ->
                        methodMatchesAllMethods(allowedMethod) || method.equals(allowedMethod));
    }

    private boolean methodMatchesAllMethods(final String method) {
        return "*".equals(method);
    }

    private Set<String> retrieveAllowedMethodsOnRoute(final String routeId) {
        return routeStore.findHttpMethodsByRouteId(routeId);
    }
}
