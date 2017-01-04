package com.dbg.cloud.acheron.config.store.routing.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dbg.cloud.acheron.config.store.routing.RouteStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CassandraRouteStore implements RouteStore {

    private static final String TABLE_NAME = "routes";

    private final CassandraOperations cassandraOperations;
    private final String keySpace;
    private final String table;

    public CassandraRouteStore(final CassandraOperations cassandraOperations) {
        this(cassandraOperations, null, TABLE_NAME);
    }

    @Override
    public List<ZuulProperties.ZuulRoute> findAll() {
        final Select query = QueryBuilder.select().from(keySpace, table);
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
}
