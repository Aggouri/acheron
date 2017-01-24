package com.dbg.cloud.acheron.plugins.ratelimiting.store.cassandra;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.dbg.cloud.acheron.plugins.ratelimiting.store.RateLimitStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public final class CassandraRateLimitStore implements RateLimitStore {

    private static final String TABLE_ROUTE_EXEC = "rate_limiting_route_requests";
    private static final String TABLE_CONSUMER_ROUTE_EXEC = "rate_limiting_consumer_route_requests";
    private final CassandraOperations cassandraOperations;


    @Override
    public void addConsumptionToRoute(final @NonNull String routeId, final int windowInSeconds) {
        final WriteOptions options = new WriteOptions();
        options.setTtl(windowInSeconds); // in seconds
        cassandraOperations.insert(new RouteRequest(new RouteRequestPK(routeId, UUIDs.timeBased())), options);
    }

    @Override
    public long countConsumptionOfRoute(final @NonNull String routeId) {
        final Select select = QueryBuilder.select().countAll().from(TABLE_ROUTE_EXEC);
        final Clause route_id = QueryBuilder.eq("route_id", routeId);

        select.where(route_id);

        return cassandraOperations.queryForObject(select, Long.class);
    }

    public Optional<Long> retrieveTimeOfEarliestConsumptionOfRoute(final @NonNull String routeId) {
        final Select select = QueryBuilder.select().from(TABLE_CONSUMER_ROUTE_EXEC).limit(1);
        select.where(QueryBuilder.eq("route_id", routeId));

        final List<RouteRequest> results = cassandraOperations.select(select, RouteRequest.class);
        final Optional<RouteRequest> first = results.stream().findFirst();

        if (first.isPresent()) {
            return Optional.of(UUIDs.unixTimestamp(first.get().getPk().getCreatedAt()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void addConsumptionToConsumer(final @NonNull String routeId, final @NonNull UUID consumerId,
                                         int windowInSeconds) {
        final WriteOptions options = new WriteOptions();
        options.setTtl(windowInSeconds); // in seconds
        cassandraOperations.insert(new ConsumerRouteRequest(new ConsumerRouteRequestPK(routeId, UUIDs.timeBased()),
                consumerId), options);
    }

    @Override
    public long countConsumptionOfConsumer(final @NonNull String routeId, final @NonNull UUID consumerId) {
        final Select select = QueryBuilder.select().countAll().from(TABLE_CONSUMER_ROUTE_EXEC);

        final Clause route_id = QueryBuilder.eq("route_id", routeId);
        final Clause consumer_id = QueryBuilder.eq("consumer_id", consumerId);

        select.where(route_id).and(consumer_id);

        return cassandraOperations.queryForObject(select, Long.class);
    }

    public Optional<Long> retrieveTimeOfEarliestConsumptionOfConsumer(final @NonNull String routeId,
                                                                      final @NonNull UUID consumerId) {
        final Select select = QueryBuilder.select().from(TABLE_CONSUMER_ROUTE_EXEC).limit(1);
        select.where(QueryBuilder.eq("route_id", routeId)).and(QueryBuilder.eq("consumer_id", consumerId));

        final List<ConsumerRouteRequest> results = cassandraOperations.select(select, ConsumerRouteRequest.class);
        final Optional<ConsumerRouteRequest> first = results.stream().findFirst();

        if (first.isPresent()) {
            return Optional.of(UUIDs.unixTimestamp(first.get().getPk().getCreatedAt()));
        } else {
            return Optional.empty();
        }
    }
}
