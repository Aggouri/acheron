package com.dbg.cloud.acheron.plugins.ratelimiting.store.cassandra;

import lombok.*;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "rate_limiting_route_requests")
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
final class RouteRequest {

    @PrimaryKey
    private RouteRequestPK pk;
}
