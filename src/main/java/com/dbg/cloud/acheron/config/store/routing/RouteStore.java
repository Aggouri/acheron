package com.dbg.cloud.acheron.config.store.routing;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.List;
import java.util.Set;

public interface RouteStore {

    List<ZuulProperties.ZuulRoute> findAll();

    Set<String> findHttpMethodsByRouteId(final String routeId);
}
