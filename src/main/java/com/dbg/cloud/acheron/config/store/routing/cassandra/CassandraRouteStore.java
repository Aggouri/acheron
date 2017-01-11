package com.dbg.cloud.acheron.config.store.routing.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.routing.Route;
import com.dbg.cloud.acheron.config.store.routing.RouteStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public final class CassandraRouteStore implements RouteStore {

    private static final String TABLE = "routes";
    private final CassandraOperations cassandraOperations;

    @Override
    public List<Route> findAll() {
        final Select select = QueryBuilder.select().from(TABLE);
        final List<CassandraRoute> results = cassandraOperations.select(select, CassandraRoute.class);
        return results.stream().map(cassandraRoute -> cassandraRoute).collect(Collectors.toList());
    }

    @Override
    public Optional<Route> findById(final @NonNull String routeId) {
        final Select select = QueryBuilder.select().from(TABLE);
        select.where(QueryBuilder.eq("id", routeId));

        final List<CassandraRoute> cassandraRoutes = cassandraOperations.select(select, CassandraRoute.class);
        final Optional<Route> firstRoute = cassandraRoutes.stream().findFirst().map(
                cassandraRoute -> cassandraRoute);

        return firstRoute;
    }

    @Override
    public Set<String> findHttpMethodsByRouteId(final @NonNull String routeId) {
        final Optional<Route> route = findById(routeId);
        return route.isPresent() ? route.get().getHttpMethods() : new HashSet<>();
    }

    @Override
    public Route add(final @NonNull Route route) {
        final Date createdAt = (route.getCreatedAt() == null) ? new Date() : route.getCreatedAt();

        return cassandraOperations.insert(new CassandraRoute(
                route.getId(),
                route.getHttpMethods(),
                route.getPath(),
                route.getServiceId(),
                route.getUrl(),
                route.isKeepPrefix(),
                route.isRetryable(),
                route.isOverrideSensitiveHeaders(),
                route.getSensitiveHeaders(),
                createdAt
        ));
    }

    @Override
    public void deleteById(final @NonNull String routeId) {
        cassandraOperations.deleteById(CassandraRoute.class, routeId);
    }
}
