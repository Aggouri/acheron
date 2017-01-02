package com.dbg.cloud.acheron.config;

import com.dbg.cloud.acheron.config.store.routing.AcheronDiscoveryClientRouteLocator;
import com.dbg.cloud.acheron.config.store.routing.RouteStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.ZuulProxyConfiguration;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class AcheronZuulConfiguration extends ZuulProxyConfiguration {

    @Autowired(required = false)
    private RouteStore routeStore;

    @Autowired
    private ServerProperties server;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DiscoveryClient discoveryClient;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ZuulProperties zuulProperties;

    @Override
    public DiscoveryClientRouteLocator routeLocator() {
        return new AcheronDiscoveryClientRouteLocator(server.getServletPath(), discoveryClient, zuulProperties,
                Optional.ofNullable(routeStore));
    }
}
