In Dante's Inferno, the Acheron river forms the border of Hell. Following Greek mythology, Charon ferries souls across this river to Hell.

> Warning: This project is a work in progress.

# Introduction
Acheron is a configurable edge server fronting your public APIs, offering pluggable authentication, authorisation and request transformation options per API. Acheron's goal is to be lightweight, scalable and easy to configure. We'll see how that works out :-)

# Architecture

## Core
Acheron is a Spring Boot application embedding Zuul, the edge server of Netflix. This means that, at its core, it's nothing more than a glorified set of Zuul filters that are responsible for handling incoming requests.

One can see filters as plugins working together to handle HTTP requests and responses. These generally fall into the following categories:
- Edge (e.g. Logging plugin)
- Authentication (e.g. OAuth2 plugin)
- Authorisation (e.g. ACL plugin)
- Traffic Control (e.g. Request & Response rate limiting plugin)
- Analytics & Monitoring
- Request Transformation (e.g. Correlation ID plugin)
- Response Transformation

## Configuration Store
The route configuration is stored in Apache Cassandra.

An admin API is planned in the immediate future. Currently, you have to execute all CQL statements yourself.

> At the moment, the plugin configuration per route (e.g. which plugins are active for a route and their properties) is currently hardcoded in a filter. Writing code that allows configuring the plugins in Cassandra is the current priority.

## OAuth2
The OAuth2 plugin uses Hydra, a lightweight, scalable and cloud native OAuth2 authorisation server (from ORY).

# Installation / Running
This is a very crude set of commands that kinda gives an idea of what is required to run the whole thing. This section will be updated later, when the project has progressed.

## Acheron
To run Acheron, please clone the repo and either use your IDE or run ```./mvnw spring-boot:run```.

You need to export the following enviornment variables, the values of which you get from Hydra. See the configuration section below.
```
OAUTH2_CLIENT_ID=7f15f8b8-98d5-4d25-bb1b-d45614766e03
OAUTH2_CLIENT_SECRET=YCWwEAqnogn(O0uBFrqh$_9hsR
```

## Hydra
First time:
```
$ SYSTEM_SECRET=awesomesecretthatislongenoughtonotbeignored docker-compose up
```

From then on:
```
$ SYSTEM_SECRET=awesomesecretthatislongenoughtonotbeignored docker-compose start
```

## Cassandra
```
$ docker run --name acheron_cassandra -p 9042:9042 -d cassandra:3.9
```

# Configuration
## Route Configuration
Connect to cassandra:
```
$ docker exec -it acheron_cassandra /bin/bash
```

Connect to cqlsh:
```
$ cqlsh
```

Create the routing table:
```
CREATE KEYSPACE IF NOT EXISTS acheron WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE acheron;

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
```

Insert the Hydra route:
```
INSERT INTO acheron_routes (id, path, service_id, url, override_sensitive_headers, sensitive_headers) VALUES ('hydra_realm1', '/hydra/realm1/**', 'hydra_realm1', 'http://localhost:4444', true, {});
```

## Hydra Configuration
You need to create an OAuth2 client to allow Acheron to make requests to Hydra.

Connect to hydra:
```
$ docker exec -i -t hydra_hydra_1 /bin/bash
```

Create the Acheron client:
```
$ hydra clients create -n "acheron" \
-g client_credentials \
-r token

Client ID: 7f15f8b8-98d5-4d25-bb1b-d45614766e03
Client Secret: YCWwEAqnogn(O0uBFrqh$_9hsR
```
The client ID and client secret need to be exported as environment variables prior to running Acheron. See the instructions for running Acheron.

## Plugin Configuration
Hardcoded. Currently working on it.

# Play
This section has a set of requests one can execute to play with Acheron. It assumes you have an API running at ```http://localhost:10000/accounts``` and Acheron is configured to proxy requests through it.

## Set up the API route
```
INSERT INTO acheron_routes (id, path, service_id, url, override_sensitive_headers) VALUES ('accounts', '/accounts/**', 'accounts', 'http://localhost:10000/accounts', false);
```

## Create an OAuth2 client
To make calls to an OAuth2-protected API, you need to create an OAuth2 client.

```
$ docker exec -i -t hydra_hydra_1 /bin/bash
$ hydra clients create -n "dbp-client" \
-a hydra.keys.get,accounts,balances \
-c https://www.getpostman.com/oauth2/callback \
-g authorization_code,client_credentials \
-r token,code
```