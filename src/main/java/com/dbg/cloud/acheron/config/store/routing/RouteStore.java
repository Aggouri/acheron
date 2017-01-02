package com.dbg.cloud.acheron.config.store.routing;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.List;

public interface RouteStore {

    List<ZuulProperties.ZuulRoute> findAll();
}
