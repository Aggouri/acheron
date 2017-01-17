package com.dbg.cloud.acheron.cluster;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
final class ClusterEventBusImpl implements ClusterEventBus {

    private final ApplicationContext applicationContext;

    @Override
    public void refreshRoutes() {
        applicationContext.publishEvent(new RefreshRoutesEvent(this, applicationContext.getId()));
    }
}
