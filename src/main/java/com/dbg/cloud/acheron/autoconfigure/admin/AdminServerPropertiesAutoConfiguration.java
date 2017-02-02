package com.dbg.cloud.acheron.autoconfigure.admin;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(ServerPropertiesAutoConfiguration.class)
@EnableConfigurationProperties
public class AdminServerPropertiesAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AdminServerProperties adminServerProperties() {
        return new AdminServerProperties();
    }

    // In case security auto configuration hasn't been included
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
    public SecurityProperties securityProperties() {
        return new SecurityProperties();
    }

    // In case server auto configuration hasn't been included
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public ServerProperties serverProperties() {
        return new ServerProperties();
    }
}
