package com.dbg.cloud.acheron.adminendpoints;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AdminEndpointHandlerMapping extends RequestMappingHandlerMapping {

    private final Set<AdminEndpoint> endpoints;
    private final CorsConfiguration corsConfiguration;

    public AdminEndpointHandlerMapping(final Collection<? extends AdminEndpoint> endpoints, final CorsConfiguration
            corsConfiguration) {
        this.endpoints = new HashSet<>(endpoints);
        this.corsConfiguration = corsConfiguration;

        setOrder(-100);
        setUseSuffixPatternMatch(false);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        for (final AdminEndpoint endpoint : this.endpoints) {
            detectHandlerMethods(endpoint);
        }
    }

    @Override
    protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingInfo mappingInfo) {
        return this.corsConfiguration;
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        // must be AdminEndpoint
        return super.isHandler(beanType) && AdminEndpoint.class.isAssignableFrom(beanType);
    }
}
