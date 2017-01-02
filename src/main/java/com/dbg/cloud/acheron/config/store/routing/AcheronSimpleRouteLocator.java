package com.dbg.cloud.acheron.config.store.routing;

import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AcheronSimpleRouteLocator extends SimpleRouteLocator {

    private final Optional<RouteStore> routeStore;

    public AcheronSimpleRouteLocator(final String servletPath, final ZuulProperties properties,
                                     final Optional<RouteStore> routeStore) {
        super(servletPath, properties);
        this.routeStore = routeStore;
    }

    @Override
    protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
        final Map<String, ZuulProperties.ZuulRoute> routes = new LinkedHashMap<>();

        routes.putAll(super.locateRoutes());

        if (routeStore.isPresent()) {
            final List<ZuulProperties.ZuulRoute> dbRoutes = routeStore.get().findAll();
            for (ZuulProperties.ZuulRoute route : dbRoutes) {
                routes.put(route.getPath(), route);
            }
        }

        return routes;
    }
}
