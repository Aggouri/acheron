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

/*
docker run --name acheron_cassandra -p 9042:9042 -d cassandra:3.9

docker exec -it acheron_cassandra /bin/bash

cqlsh

CREATE KEYSPACE IF NOT EXISTS acheron WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE acheron;

DROP TABLE acheron_routes;

CREATE TABLE acheron_routes (
    id text,
    path text,
    service_id text,
    url text,
    keep_prefix boolean,
    retryable boolean,
    override_sensitive_headers boolean,
    sensitive_headers Set<text>,
    PRIMARY KEY(id)
);

INSERT INTO acheron_routes (id, path, service_id, url, override_sensitive_headers, sensitive_headers) VALUES
('hydra_realm1', '/hydra/realm1/**', 'hydra_realm1', 'http://localhost:4444', true, {});

INSERT INTO acheron_routes (id, path, service_id, url, override_sensitive_headers) VALUES ('balances',
'/balances/**', 'balances', 'http://localhost:10000/balances', false);

INSERT INTO acheron_routes (id, path, service_id, url, override_sensitive_headers) VALUES ('accounts',
'/accounts/**', 'accounts', 'http://localhost:10000/accounts', false);

*/
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CassandraRouteStore implements RouteStore {

    private static final String TABLE_NAME = "acheron_routes";

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
