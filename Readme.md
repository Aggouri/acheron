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

## Message Broker
Acheron instances communicate with each other through a RabbitMQ message broker. The message broker is required when scaling Acheron instances up.

## OAuth2
The OAuth2 plugin uses Hydra, a lightweight, scalable and cloud native OAuth2 authorisation server (from ORY).

# 3. Getting Started
If you want to start playing immediately, it is recommended that you use a pre-configured environment that is set up through Docker Compose. To do so, you just need to execute the following commands in the project's root:

```
$ ./mvnw package docker:build -DskipTests
$ docker-compose up --build
```

Acheron runs on [http://localhost:8080](http://localhost:8080). Skip to section *Play* for a short tutorial.

# 4. Building Acheron
To build Acheron, you can run the following command:
```
$ ./mvnw package
```

If you want to build the Docker image, you can do so using the following command:
```
$ ./mvnw package docker:build
```

# 5. Manual configuration
Check the [docs](https://github.com/Aggouri/acheron/wiki/Manual-setup).

# 6. Play
This section is a short tutorial allowing you to play with Acheron. It assumes Acheron runs on ```http://localhost:8080``` and you have an API running at ```http://localhost:10000/accounts```. 

> Note that if you are using Docker, replace the API URL's ```localhost``` with your machine's IP address. Acheron running the container can access your API running locally **only through the machine's IP address**. In other words, in the following set of instructions, whenever you see ```http://localhost:10000```, please read ```http://<machine_ip>:10000```.

## Preparation
First, we are going to create the "accounts" route and enable the following plugins for that route:

- API Key auth
- OAuth2

This set up effectively means that in order to call the API we need to provide an API Key and an OAuth2 access token.

### Create the route
Execute the following request on the ```/admin/routes``` endpoint. This creates a new route.
```
$ curl -i -X POST -H "Content-Type: application/json" -d '{
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
$ curl -i -X POST -H "Content-Type: application/json" -d '{
"name": "api_key",
"route_id": "accounts",
"http_methods": [
  "*"
 ]
}' "http://localhost:8080/admin/plugin-configs"

$ curl -i -X POST -H "Content-Type: application/json" -d '{
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
$ curl -i -X POST -H "Content-Type: application/json" -d '{
	"name": "Awesome Consumer"
}' "http://localhost:8080/admin/consumers"
```
Take a note of the returned Consumer ID (```id``` column of the returned JSON). You will use it to map the OAuth2 credentials to the consumer.

### Generate/Register API key in Acheron
To make calls with an API key, a consumer needs to have one. Replace ```<consumer_id>``` with the consumer ID returned in the consumer creation step and execute the following request against the ```/admin/consumers/<consumer_id>/api-keys``` endpoint:

```
$ curl -i -X POST -H "Content-Type: application/json" -d '{
	"api_key": "faed995a-c797-479f-9352-a7b2bf1748ad"
}' "http://localhost:8080/admin/consumers/<consumer_id>/api-keys"
```

> Here we are forcing the API Key to be a specific string. Normally, you would let Acheron generate one for you.

### Register OAuth2 client in Acheron
To make calls to an OAuth2-protected API, you need to create an OAuth2 client with at least a scope that is equal to the route name, i.e. scopes must contain 'accounts'. This will probably change at a later date, when we get the concepts right.

Replace ```<consumer_id>``` with the consumer ID returned in the consumer creation step and execute the following request against the ```/admin/consumers/<consumer_id>/oauth2-clients``` endpoint:
```
$ curl -i -X POST -H "Content-Type: application/json" -d '{
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
$ curl -i -X GET -H "API_KEY: faed995a-c797-479f-9352-a7b2bf1748ad" "http://localhost:8080/accounts"
{ "error": "Invalid access token" }%
```

### Calling the API with an OAuth2 token
To obtain an access token for the accounts route, we are going to use Hydra and the OAuth2 client information we created above.

For simplicity, in this example, we are getting a bearer token via the OAuth2 client_credentials grant. The Authorization header string after the ```Basic``` keyword is the result of Base64 encoding of the string ```<Client ID>:<Client Secret>```, according to the Basic Auth spec.

You can use online tools such as https://www.base64encode.org in order to encode your own client ID and client secret.

```
$ curl -i -X POST -H "Content-Type: application/x-www-form-urlencoded" -H "Authorization: Basic Nzg4MWI4ZDYtMzZkMy00MzYwLWIzOGItYTRmNGVlOTZiMWYxOlpwbyk8dmo0V1pFVi1rIUQvbE9YNXdxKEVw" -d 'grant_type=client_credentials&scope=accounts' "http://localhost:8080/hydra/realm1/oauth2/token"
{"access_token":"YIoXNgdA3aKkkyNz1Q69mQN7-ftVsOsstVbekdY1Oj4.dCtHcIfmvGlxtfuwxExvAfnspK8qzkr198fGXh2tPew","expires_in":"3599","scope":"accounts","token_type":"bearer"}%  
```

The access token above can then be used to make a successful call to the API.
```
$ curl -X GET -H "Authorization: Bearer YIoXNgdA3aKkkyNz1Q69mQN7-ftVsOsstVbekdY1Oj4.dCtHcIfmvGlxtfuwxExvAfnspK8qzkr198fGXh2tPew" -H "API_KEY: SECRET_7881b8d6-36d3-4360-b38b-a4f4ee96b1f1" "http://localhost:8080/accounts"
```

# 7. Documentation
Check the [Wiki](https://github.com/Aggouri/acheron/wiki).