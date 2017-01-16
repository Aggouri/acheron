In Dante's Inferno, the Acheron river forms the border of Hell. Following Greek mythology, Charon ferries souls across this river to Hell.

> Warning: This project is a work in progress. Everything is in flux and is super-insecure.

# 1. Introduction
Acheron is a configurable API gateway and management server, offering pluggable authentication, authorisation and request transformation options per API and consumer. Acheron's goal is to be lightweight, scalable and easy to configure. We'll see how that works out :-)

The project is heavily inspired by Kong, Apigee, Mashery and similar platforms.

![Acheron high level image](docs/readme/images/acheron.png)

# 2. Architecture

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
The configuration is stored in Apache Cassandra. A non-secure and basic admin REST API is available for most configuration operations. Nevertheless, you may have to execute some CQL statements yourself.

## OAuth2
The OAuth2 plugin uses Hydra, a lightweight, scalable and cloud native OAuth2 authorisation server (from ORY).

# 3. Running Acheron, Cassandra and Hydra
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

# 4. Initial Configuration
## Acheron Configuration
Acheron stores configuration in Apache Cassandra. To create the store, simply run the following command in the project's root:

```
$ docker exec acheron_cassandra cqlsh --request-timeout=3600 -e "$(cat ddl/cassandra/ddl.cql)"
```

Add the Hydra route via the ```/admin/routes``` endpoint, making sure you replace the URLs should you have a different set up:
```
$ curl -X POST -H "Content-Type: application/json" -d '{
    "id": "hydra_realm1",
    "http_methods": [
      "POST"
    ],
    "path": "/hydra/realm1/**",
    "service_id": "hydra_realm1",
    "url": "http://localhost:4444",
    "keep_prefix": false,
    "retryable": false,
    "override_sensitive_headers": true,
    "sensitive_headers": []
}' "http://localhost:8080/admin/routes"
```

> At the moment, route changes are taken only for the Acheron instance that processes the configuration request. As a result, if you run multiple Acheron instances in front of a load balancer, you should refresh the routes manually. There are two ways to refresh the routes:
> - Restart all Acheron instances
> - Call the ```/routes``` endpoint (POST request) on all Acheron instances (e.g. ```curl -X POST "http://localhost:8080/routes"```)
>
> When things are done properly, there will either be a message bus or some inter-instance communication paradigm that will remove the need for this manual step.

## Hydra Configuration
You need to create an OAuth2 client to allow Acheron to make requests to Hydra.

Connect to Hydra:
```
$ docker exec -i -t hydra_hydra_1 /bin/bash
```

Create the Acheron client:
```
$ hydra clients create -n "acheron" \
-g client_credentials \
-r token \
-a hydra.clients

Client ID: 991635e3-c17a-4960-90f1-9908d4a8b333
Client Secret: ihLMuaB59cGkAqqD,pxKd&4LBL
```
The client ID and client secret need to be exported as environment variables prior to running Acheron. See the instructions for running Acheron.

Create a policy that gives the permission to Acheron to create OAuth2 clients. Replace ```<client_id>``` with the client ID obtained in the previous step:
```
$ hydra policies create --allow \
-a "create,delete" \
-r "rn:hydra:clients,rn:hydra:clients:<.*>" \
-s "<client_id>"
```

# 5. Play
This section is a short tutorial allowing you to play with Acheron. It assumes Acheron runs on ```http://localhost:8080``` and you have an API running at ```http://localhost:10000/accounts```.

## Preparation
First, we are going to create the "accounts" route and enable the following plugins for that route:

- API Key auth
- OAuth2

This set up effectively means that in order to call the API we need to provide an API Key and an OAuth2 access token.

### Create the route
Execute the following request on the ```/admin/routes``` endpoint. This creates a new route.
```
$ curl -X POST -H "Content-Type: application/json" -d '{
    "id": "accounts",
    "http_methods": [
      "*"
    ],
    "path": "/accounts/**",
    "service_id": "accounts",
    "url": "http://localhost:10000/accounts",
    "keep_prefix": false,
    "retryable": false,
    "override_sensitive_headers": false,
    "sensitive_headers": []
}' "http://localhost:8080/admin/routes"
```

### Configure OAuth2 and API Key auth on the route
Execute the following requests on the ```/admin/plugin-configs``` endpoint. This activates API Key auth and OAuth2 for the new route. 

```
$ curl -X POST -H "Content-Type: application/json" -d '{
"name": "api_key",
"route_id": "accounts",
"http_methods": [
  "*"
 ]
}' "http://localhost:8080/admin/plugin-configs"

$ curl -X POST -H "Content-Type: application/json" -d '{
"name": "oauth2",
"route_id": "accounts",
"http_methods": [
  "*"
 ]
}' "http://localhost:8080/admin/plugin-configs"
```

### Create a consumer for our API
Execute the following request on the ```/admin/consumers``` endpoint. This creates a consumer, which represents the caller of the API:

```
$ curl -X POST -H "Content-Type: application/json" -d '{
	"name": "Awesome Consumer"
}' "http://localhost:8080/admin/consumers"
```
Take a note of the returned Consumer ID (```id``` column of the returned JSON). You will use it to map the OAuth2 credentials to the consumer.

### Generate/Register API key in Acheron
To make calls with an API key, a consumer needs to have one. Replace ```<consumer_id>``` with the consumer ID returned in the consumer creation step and execute the following request against the ```/admin/consumers/<consumer_id>/api-keys``` endpoint:

```
$ curl -X POST -H "Content-Type: application/json" -d '{
	"api_key": "faed995a-c797-479f-9352-a7b2bf1748ad"
}' "http://localhost:8080/admin/consumers/<consumer_id>/api-keys"
```

> Here we are forcing the API Key to be a specific string. Normally, you would let Acheron generate one for you.

### Register OAuth2 client in Acheron
To make calls to an OAuth2-protected API, you need to create an OAuth2 client with at least a scope that is equal to the route name, i.e. scopes must contain 'accounts'. This will probably change at a later date, when we get the concepts right.

Replace ```<consumer_id>``` with the consumer ID returned in the consumer creation step and execute the following request against the ```/admin/consumers/<consumer_id>/oauth2-clients``` endpoint:
```
$ curl -X POST -H "Content-Type: application/json" -d '{
	"scope": "accounts",
	"grant_types": ["client_credentials", "authorization_code"]
}' "http://localhost:8080/admin/consumers/<consumer_id>/oauth2-clients"
```

Take a note of the returned client ID and client secret. They are used in the next section.

## Call the API
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
$ curl -X GET -H "API_KEY: faed995a-c797-479f-9352-a7b2bf1748ad" "http://localhost:8080/accounts"
{ "error": "Invalid access token" }%
```

### Calling the API with an OAuth2 token
To obtain an access token for the accounts route, we are going to use Hydra and the OAuth2 client information we created above.

For simplicity, in this example, we are getting a bearer token via the OAuth2 client_credentials grant. The Authorization header string after the ```Basic``` keyword is the result of Base64 encoding of the string ```<Client ID>:<Client Secret>```, according to the Basic Auth spec.

You can use online tools such as https://www.base64encode.org in order to encode your own client ID and client secret.

```
$ curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -H "Authorization: Basic Nzg4MWI4ZDYtMzZkMy00MzYwLWIzOGItYTRmNGVlOTZiMWYxOlpwbyk8dmo0V1pFVi1rIUQvbE9YNXdxKEVw" -d 'grant_type=client_credentials&scope=accounts' "http://localhost:8080/hydra/realm1/oauth2/token"
{"access_token":"YIoXNgdA3aKkkyNz1Q69mQN7-ftVsOsstVbekdY1Oj4.dCtHcIfmvGlxtfuwxExvAfnspK8qzkr198fGXh2tPew","expires_in":"3599","scope":"accounts","token_type":"bearer"}%  
```

The access token above can then be used to make a successful call to the API.
```
$ curl -X GET -H "Authorization: Bearer YIoXNgdA3aKkkyNz1Q69mQN7-ftVsOsstVbekdY1Oj4.dCtHcIfmvGlxtfuwxExvAfnspK8qzkr198fGXh2tPew" -H "API_KEY: SECRET_7881b8d6-36d3-4360-b38b-a4f4ee96b1f1" "http://localhost:8080/accounts"
```