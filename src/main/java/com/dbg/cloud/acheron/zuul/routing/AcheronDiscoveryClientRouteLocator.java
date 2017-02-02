package com.dbg.cloud.acheron.zuul.routing;

import com.dbg.cloud.acheron.routing.service.RouteService;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;

import java.util.LinkedHashMap;

public class AcheronDiscoveryClientRouteLocator extends DiscoveryClientRouteLocator {

    private final AcheronSimpleRouteLocator simpleLocator;

    public AcheronDiscoveryClientRouteLocator(final String servletPath, final DiscoveryClient discovery,
                                              final ZuulProperties properties,
                                              final RouteService routeService) {
        super(servletPath, discovery, properties);
        simpleLocator = new AcheronSimpleRouteLocator(servletPath, properties, routeService);
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
