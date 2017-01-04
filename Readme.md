In Dante's Inferno, the Acheron river forms the border of Hell. Following Greek mythology, Charon ferries souls across this river to Hell.

> Warning: This project is a work in progress.

# Introduction
Acheron is a configurable API gateway and management server, offering pluggable authentication, authorisation and request transformation options per API and consumer. Acheron's goal is to be lightweight, scalable and easy to configure. We'll see how that works out :-)

The project is heavily inspired by Kong, Apigee, Mashery and similar platforms.

![Acheron high level image](docs/readme/images/acheron.png)

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

## OAuth2
The OAuth2 plugin uses Hydra, a lightweight, scalable and cloud native OAuth2 authorisation server (from ORY).

# Running Acheron, Cassandra and Hydra
This is a very crude set of commands that kinda gives an idea of what is required to run the whole thing. This section will be updated later, when the project has progressed.

## Acheron
To run Acheron, please clone the repo and either use your IDE or run ```./mvnw spring-boot:run```.

You need to export the following enviornment variables, the values of which you get from Hydra. See the configuration section below.
```
OAUTH2_CLIENT_ID=7f15f8b8-98d5-4d25-bb1b-d45614766e03
OAUTH2_CLIENT_SECRET=YCWwEAqnogn(O0uBFrqh$_9hsR
```

By default, Acheron runs on port 8080, but you can change this by exporting the ```SERVER_PORT``` environment variable, e.g.:

```
SERVER_PORT=9000 ./mvnw spring-boot:run
```

## Cassandra
```
$ docker run --name acheron_cassandra -p 9042:9042 -d cassandra:3.9
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

# Initial Configuration
## Acheron Configuration
Acheron stores configuration in Apache Cassandra. Currently, the store is not automatically created, so you need to do it yourself, using the instructions in this section.

Connect to Cassandra:
```
$ docker exec -it acheron_cassandra /bin/bash
```

Connect to cqlsh:
```
$ cqlsh
```

Create the routing and plugin config tables:
```
CREATE KEYSPACE IF NOT EXISTS acheron WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE acheron;

CREATE TABLE routes (
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

CREATE TABLE plugins (
    id uuid,
    name text,
    route_id text,
    consumer_id uuid,
    config text,
    enabled boolean,
    created_at timestamp,
    PRIMARY KEY (id, name)
) WITH CLUSTERING ORDER BY (name ASC);

CREATE INDEX plugins_api_id_idx ON plugins (route_id);
CREATE INDEX plugins_consumer_id_idx ON plugins (consumer_id);
CREATE INDEX plugins_name_idx ON plugins (name);
```

Insert the Hydra route:
```
INSERT INTO routes (id, path, service_id, url, override_sensitive_headers, sensitive_headers) VALUES ('hydra_realm1', '/hydra/realm1/**', 'hydra_realm1', 'http://localhost:4444', true, {});
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

# Play
This section is a short tutorial allowing you to play with Acheron. It assumes you have an API running at ```http://localhost:10000/accounts```.

## Preparation
First, we are going to create the "accounts" route and enable the following plugins for that route:

- API Key auth
- OAuth2

This set up effectively means that in order to call the API we need to provide an API Key and an OAuth2 access token. 

Since no admin interface currently exists, we need to execute all steps manually, via cqlsh in Cassandra and the hydra CLI in Hydra.

### Set up the route
Connect to Cassandra (see Acheron Configuration section above) and execute the following statements:

```
USE acheron;

INSERT INTO routes (id, path, service_id, url, override_sensitive_headers) VALUES ('accounts', '/accounts/**', 'accounts', 'http://localhost:10000/accounts', false);

INSERT INTO plugins (id, name, route_id, consumer_id, config, enabled, created_at) 
     VALUES (uuid(), 'oauth2', 'accounts', null, '', true, dateOf(now()));

INSERT INTO plugins (id, name, route_id, consumer_id, config, enabled, created_at) 
     VALUES (uuid(), 'api_key', 'accounts', null, '', true, dateOf(now()));
```

### Create an OAuth2 client
To make calls to an OAuth2-protected API, you need to create an OAuth2 client with at least a scope that is equal to the route name, i.e. scopes must contain 'accounts'. This will probably change at a later date, when we get the concepts right.

```
$ docker exec -i -t hydra_hydra_1 /bin/bash
$ hydra clients create -n "dbp-client" \
-a hydra.keys.get,accounts \
-c https://www.getpostman.com/oauth2/callback \
-g authorization_code,client_credentials \
-r token,code
```

Take a note of the returned client ID and client secret. They are used in the next section.

## Call the API
We assume Acheron runs on port 8080. 

Your accounts API is available via Acheron at http://localhost:8080/accounts. Since we have enabled OAuth2 and API Key auth, the API needs to be called with an API Key and an OAuth2 access token.

### Calling the API without an API Key
The following call will fail, since the request does not have an API Key:
```
$ curl -X GET http://localhost:8080/accounts
{ error: "Invalid API key" }%
```

### Calling the API with an API Key
Adding an API Key is a first step to the right direction, but since we don't have an OAuth2 access token, the following request will fail:
```
$ curl -X GET -H "API_KEY: SECRET_7881b8d6-36d3-4360-b38b-a4f4ee96b1f1" "http://localhost:8080/accounts"
{ "error": "Invalid access token" }%
```

> Note that at this stage, the API Key is hardcoded to be ```SECRET_<somestring>```. When OAuth2 is also enabled, ```<somestring>``` must be equal to the OAuth2 client ID. This will definitely change.

### Calling the API with an OAuth2 token
To obtain an access token for the accounts route, we are going to use Hydra and the OAuth2 client information we created above.

For simplicity, in this example, we are getting a bearer token via the OAuth2 client_credentials grant. The Authorization header string after the ```Basic``` keyword is the result of Base64 encoding of the string ```<Client ID>:<Client Secret>```, according to the Basic Auth spec.

You can use online tools such as https://www.base64encode.org in order to encode your own client ID and client secret.

```
$ curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -H "Authorization: Basic Nzg4MWI4ZDYtMzZkMy00MzYwLWIzOGItYTRmNGVlOTZiMWYxOlpwbyk8dmo0V1pFVi1rIUQvbE9YNXdxKEVw" -d 'grant_type=client_credentials&scope=accounts' "http://localhost:8080/hydra/realm1/oauth2/token
{"access_token":"YIoXNgdA3aKkkyNz1Q69mQN7-ftVsOsstVbekdY1Oj4.dCtHcIfmvGlxtfuwxExvAfnspK8qzkr198fGXh2tPew","expires_in":"3599","scope":"accounts","token_type":"bearer"}%  
```

The access token above can then be used to make a successful call to the API.
```
$ curl -X GET -H "Authorization: Bearer YIoXNgdA3aKkkyNz1Q69mQN7-ftVsOsstVbekdY1Oj4.dCtHcIfmvGlxtfuwxExvAfnspK8qzkr198fGXh2tPew" -H "API_KEY: SECRET_7881b8d6-36d3-4360-b38b-a4f4ee96b1f1" "http://localhost:8080/accounts"
```