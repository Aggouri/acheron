package com.dbg.cloud.acheron.config.store.routing;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;

import java.util.LinkedHashMap;
import java.util.Optional;

public class AcheronDiscoveryClientRouteLocator extends DiscoveryClientRouteLocator {

    private final AcheronSimpleRouteLocator simpleLocator;

    public AcheronDiscoveryClientRouteLocator(final String servletPath, final DiscoveryClient discovery,
                                              final ZuulProperties properties, final Optional<RouteStore> routeStore) {
        super(servletPath, discovery, properties);
        simpleLocator = new AcheronSimpleRouteLocator(servletPath, properties, routeStore);
    }

    @Override
    protected LinkedHashMap<String, ZuulProperties.ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulProperties.ZuulRoute> routesMap = new LinkedHashMap<String, ZuulProperties
                .ZuulRoute>();

        routesMap.putAll(super.locateRoutes());
        routesMap.putAll(simpleLocator.locateRoutes());

        return routesMap;
    }
}
