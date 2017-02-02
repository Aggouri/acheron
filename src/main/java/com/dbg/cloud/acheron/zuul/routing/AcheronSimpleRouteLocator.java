package com.dbg.cloud.acheron.zuul.routing;

import com.dbg.cloud.acheron.routing.service.RouteService;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AcheronSimpleRouteLocator extends SimpleRouteLocator {

    private final RouteService routeService;

    public AcheronSimpleRouteLocator(final String servletPath, final ZuulProperties properties,
                                     final RouteService routeService) {
        super(servletPath, properties);
        this.routeService = routeService;
    }

    @Override
    protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
        final Map<String, ZuulProperties.ZuulRoute> routes = new LinkedHashMap<>();

        routes.putAll(super.locateRoutes());

        final List<ZuulProperties.ZuulRoute> dbRoutes = routeService.getAllRoutes().stream().map(
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

        return routes;
    }
}
