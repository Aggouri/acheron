DROP KEYSPACE IF EXISTS acheron;

CREATE KEYSPACE IF NOT EXISTS acheron WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE acheron;

CREATE TABLE consumers (
    id uuid,
    name text,
    created_at timestamp,
    PRIMARY KEY(id, name)
) WITH CLUSTERING ORDER BY (name ASC);

CREATE TABLE routes (
    id text,
    http_methods Set<text>,
    path text,
    service_id text,
    url text,
    keep_prefix boolean,
    retryable boolean,
    override_sensitive_headers boolean,
    sensitive_headers Set<text>,
    created_at timestamp,
    PRIMARY KEY(id)
);

CREATE TABLE plugins (
    id uuid,
    name text,
    route_id text,
    consumer_id uuid,
    http_methods Set<text>,
    config text,
    enabled boolean,
    created_at timestamp,
    PRIMARY KEY (id, name)
) WITH CLUSTERING ORDER BY (name ASC);

CREATE INDEX plugins_route_id_idx ON plugins (route_id);
CREATE INDEX plugins_consumer_id_idx ON plugins (consumer_id);
CREATE INDEX plugins_name_idx ON plugins (name);

CREATE TABLE api_key_keys (
    id uuid,
    api_key text,
    consumer_id uuid,
    consumer_name text,
    consumer_created_at timestamp,
    created_at timestamp,
    PRIMARY KEY(id)
);

CREATE INDEX api_key_keys_api_key_idx ON api_key_keys (api_key);
CREATE INDEX api_key_keys_consumer_id_idx ON api_key_keys (consumer_id);

CREATE TABLE oauth2_clients (
    id uuid,
    client_id text,
    consumer_id uuid,
    consumer_name text,
    consumer_created_at timestamp,
    created_at timestamp,
    PRIMARY KEY(id)
);

CREATE INDEX oauth2_clients_client_id_idx ON oauth2_clients (client_id);

CREATE INDEX oauth2_clients_consumer_id_idx ON oauth2_clients (consumer_id);
