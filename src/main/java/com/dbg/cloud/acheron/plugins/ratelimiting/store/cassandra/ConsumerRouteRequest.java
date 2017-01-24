package com.dbg.cloud.acheron.plugins.ratelimiting.store.cassandra;

import lombok.*;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

@Table(value = "rate_limiting_consumer_route_requests")
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
final class ConsumerRouteRequest {

    @PrimaryKey
    private ConsumerRouteRequestPK pk;

    @Column(value = "consumer_id")
    private UUID consumerId;
}
