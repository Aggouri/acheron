package com.dbg.cloud.acheron.cluster;

import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@RemoteApplicationEventScan
public class ClusterBusConfiguration {
}
