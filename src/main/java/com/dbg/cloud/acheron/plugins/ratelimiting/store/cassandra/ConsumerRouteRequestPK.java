package com.dbg.cloud.acheron.plugins.ratelimiting.store.cassandra;

import lombok.*;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.UUID;

@PrimaryKeyClass
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
final class ConsumerRouteRequestPK implements Serializable {

    @PrimaryKeyColumn(name = "route_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String routeId;

    @PrimaryKeyColumn(name = "created_at", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private UUID createdAt;
}
