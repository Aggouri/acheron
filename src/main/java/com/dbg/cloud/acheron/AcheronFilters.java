package com.dbg.cloud.acheron;

import com.dbg.cloud.acheron.config.oauth2.OAuth2Properties;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import com.dbg.cloud.acheron.config.store.routing.RouteStore;
import com.dbg.cloud.acheron.filters.pre.authentication.APIKeyFilter;
import com.dbg.cloud.acheron.filters.pre.authentication.ConsumerAPIConfigFilter;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.OAuth2Filter;
import com.dbg.cloud.acheron.filters.pre.authorization.ACLFilter;
import com.dbg.cloud.acheron.filters.pre.edge.*;
import com.dbg.cloud.acheron.filters.pre.transformation.CorrelationIDFilter;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2Store;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = OAuth2Properties.class)
public class AcheronFilters {

    private final OAuth2Properties oAuth2Properties;

    public AcheronFilters(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

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
    public HttpMethodFilter httpMethodFilter(RouteStore routeStore) {
        return new HttpMethodFilter(routeStore);
    }

    @Bean
    public APIConfigFilter apiConfigFilter(PluginConfigStore pluginConfigStore) {
        return new APIConfigFilter(pluginConfigStore);
    }

    @Bean
    public APIKeyFilter apiKeyFilter() {
        return new APIKeyFilter();
    }

    @Bean
    public OAuth2Filter oAuth2Filter(RouteLocator routeLocator, OAuth2Store oAuth2Store) {
        return new OAuth2Filter(routeLocator, oAuth2Store, oAuth2Properties.getClientId(),
                oAuth2Properties.getClientSecret());
    }

    @Bean
    public ConsumerAPIConfigFilter consumerAPIConfigFilter(PluginConfigStore pluginConfigStore) {
        return new ConsumerAPIConfigFilter(pluginConfigStore);
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
