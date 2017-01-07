package com.dbg.cloud.acheron.config.store.routing.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.routing.RouteStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Slf4j
public final class CassandraRouteStore implements RouteStore {

    private final CassandraOperations cassandraOperations;

    @Override
    public List<ZuulProperties.ZuulRoute> findAll() {
        final Select query = QueryBuilder.select().from("routes");
        return cassandraOperations.query(query, (row, i) -> {
            final ZuulProperties.ZuulRoute route = new ZuulProperties.ZuulRoute(
                    row.getString("id"),
                    row.getString("path"),
                    row.getString("service_id"),
                    row.getString("url"),
                    !row.getBool("keep_prefix"),
                    row.getBool("retryable"),
                    null
            );

            if (row.getBool("override_sensitive_headers")) {
                Set<String> sensitiveHeaders = row.getSet("sensitive_headers", String.class);
                route.setSensitiveHeaders(sensitiveHeaders);
            }

            return route;
        });
    }

    @Override
    public Set<String> findHttpMethodsByRouteId(final String routeId) {
        final Select select = QueryBuilder.select().from("routes");
        select.where(QueryBuilder.eq("id", routeId));


        final List<CassandraRoute> cassandraRoutes = cassandraOperations.select(select, CassandraRoute.class);
        final Optional<CassandraRoute> firstRoute = cassandraRoutes.stream().findFirst();

        return firstRoute.isPresent() ? firstRoute.get().getHttpMethods() : new HashSet<>();
    }
}
