package com.dbg.cloud.acheron.autoconfigure.zuul;

import com.dbg.cloud.acheron.autoconfigure.oauth2.OAuth2Properties;
import com.dbg.cloud.acheron.pluginconfig.service.PluginConfigService;
import com.dbg.cloud.acheron.routing.service.RouteService;
import com.dbg.cloud.acheron.zuul.filters.pre.authentication.APIKeyFilter;
import com.dbg.cloud.acheron.zuul.filters.pre.authentication.ConsumerAPIConfigFilter;
import com.dbg.cloud.acheron.zuul.filters.pre.authentication.OAuth2Filter;
import com.dbg.cloud.acheron.zuul.filters.pre.authorization.ACLFilter;
import com.dbg.cloud.acheron.zuul.filters.pre.edge.*;
import com.dbg.cloud.acheron.zuul.filters.pre.traffic.RateLimitingFilter;
import com.dbg.cloud.acheron.zuul.filters.pre.transformation.CorrelationIDFilter;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKeyStore;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2ServerProvider;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Store;
import com.dbg.cloud.acheron.plugins.ratelimiting.service.RateLimitService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = OAuth2Properties.class)
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
    public HttpMethodFilter httpMethodFilter(RouteService routeService) {
        return new HttpMethodFilter(routeService);
    }

    @Bean
    public APIConfigFilter apiConfigFilter(PluginConfigService pluginConfigService) {
        return new APIConfigFilter(pluginConfigService);
    }

    @Bean
    public APIKeyFilter apiKeyFilter(APIKeyStore apiKeyStore) {
        return new APIKeyFilter(apiKeyStore);
    }

    @Bean
    public OAuth2Filter oAuth2Filter(OAuth2ServerProvider oAuth2ServerProvider, OAuth2Store oAuth2Store) {
        return new OAuth2Filter(oAuth2ServerProvider, oAuth2Store);
    }

    @Bean
    public ConsumerAPIConfigFilter consumerAPIConfigFilter(PluginConfigService pluginConfigService) {
        return new ConsumerAPIConfigFilter(pluginConfigService);
    }

    @Bean
    public ACLFilter aclFilter() {
        return new ACLFilter();
    }

    @Bean
    public RateLimitingFilter rateLimitingFilter(RateLimitService rateLimitService) {
        return new RateLimitingFilter(rateLimitService);
    }

    @Bean
    public CorrelationIDFilter correlationIDFilter() {
        return new CorrelationIDFilter();
    }
}
