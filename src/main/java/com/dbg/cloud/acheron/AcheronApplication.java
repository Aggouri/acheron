package com.dbg.cloud.acheron;

import com.dbg.cloud.acheron.autoconfigure.admin.IgnoreAdminEndpointsHandlerMapping;
import com.dbg.cloud.acheron.autoconfigure.zuul.EnableAcheronZuulProxy;
import com.dbg.cloud.acheron.autoconfigure.oauth2.OAuth2Properties;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2ServerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {CassandraDataAutoConfiguration.class})
@ComponentScan(
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.dbg.cloud.acheron.adminendpoints.*"),
        })
@EnableAcheronZuulProxy
public class AcheronApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcheronApplication.class, args);
    }

    @Bean
    public OAuth2ServerProvider oAuth2ServerFactory(OAuth2Properties properties, RouteLocator routeLocator,
                                                    RestTemplateBuilder restTemplateBuilder) {
        return new OAuth2ServerProvider.Default(properties.getClientId(), properties.getClientSecret(), routeLocator,
                restTemplateBuilder);
    }

    @Configuration
    public static class WebConfig extends WebMvcConfigurationSupport {

        @Bean
        public RequestMappingHandlerMapping requestMappingHandlerMapping() {
            return super.requestMappingHandlerMapping();
        }

        @Override
        protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
            return new IgnoreAdminEndpointsHandlerMapping();
        }
    }
}
