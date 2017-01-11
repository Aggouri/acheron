package com.dbg.cloud.acheron.config.store.routing;

import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            final List<ZuulProperties.ZuulRoute> dbRoutes = routeStore.get().findAll().stream().map(
                    route -> {
                        final ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute(
                                route.getId(),
                                route.getPath(),
                                route.getServiceId(),
                                route.getUrl(),
                                !route.isKeepPrefix(), // attention: this is the opposite
                                route.isRetryable(),
                                null
                        );

                        if (route.isOverrideSensitiveHeaders()) {
                            zuulRoute.setSensitiveHeaders(route.getSensitiveHeaders());
                        }

                        return zuulRoute;
                    }).collect(Collectors.toList());

            for (ZuulProperties.ZuulRoute route : dbRoutes) {
                routes.put(route.getPath(), route);
            }
        }

        return routes;
    }
}
