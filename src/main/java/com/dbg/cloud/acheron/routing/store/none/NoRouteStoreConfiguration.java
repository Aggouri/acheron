package com.dbg.cloud.acheron.routing.store.none;

import com.dbg.cloud.acheron.routing.store.RouteStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoRouteStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RouteStore noRouteStore() {
        return new RouteStore.NoRouteStore();
    }
}
