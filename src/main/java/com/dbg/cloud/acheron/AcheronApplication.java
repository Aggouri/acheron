package com.dbg.cloud.acheron;

import com.dbg.cloud.acheron.config.EnableAcheronZuulProxy;
import com.dbg.cloud.acheron.config.oauth2.OAuth2Properties;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2ServerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {CassandraDataAutoConfiguration.class})
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
}
