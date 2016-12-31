package com.dbg.cloud.acheron;

import com.dbg.cloud.acheron.filters.pre.authentication.APIKeyFilter;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.OAuth2Filter;
import com.dbg.cloud.acheron.filters.pre.authorization.ACLFilter;
import com.dbg.cloud.acheron.filters.pre.edge.APIConfigFilter;
import com.dbg.cloud.acheron.filters.pre.edge.LogPreFilter;
import com.dbg.cloud.acheron.filters.pre.edge.RouteResolutionFilter;
import com.dbg.cloud.acheron.filters.pre.edge.SanitizeHeadersFilter;
import com.dbg.cloud.acheron.filters.pre.transformation.CorrelationIDFilter;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcheronFilters {

    @Bean
    public LogPreFilter logPreFilter() {
        return new LogPreFilter();
    }

    @Bean
    public SanitizeHeadersFilter sanitizeHeadersFilter() {
        return new SanitizeHeadersFilter();
    }

    @Bean
    public RouteResolutionFilter routeResolutionFilter(RouteLocator routeLocator) {
        return new RouteResolutionFilter(routeLocator);
    }

    @Bean
    public APIConfigFilter apiConfigFilter() {
        return new APIConfigFilter();
    }

    @Bean
    public APIKeyFilter apiKeyFilter() {
        return new APIKeyFilter();
    }

    @Bean
    public OAuth2Filter oAuth2Filter(RouteLocator routeLocator) {
        return new OAuth2Filter(routeLocator);
    }

    @Bean
    public ACLFilter aclFilter() {
        return new ACLFilter();
    }

    @Bean
    public CorrelationIDFilter correlationIDFilter() {
        return new CorrelationIDFilter();
    }
}
