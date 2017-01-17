package com.dbg.cloud.acheron.cluster;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
@Slf4j
final class ClusterMessageListener {

    private final ZuulHandlerMapping zuulHandlerMapping;

    @EventListener
    public void onRefreshRoutes(RefreshRoutesEvent event) {
        log.info("Refreshing local routes", event);
        zuulHandlerMapping.setDirty(true);
    }
}
