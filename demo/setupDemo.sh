#!/usr/bin/env bash

# Verify env vars exist and are not empty
: "${HYDRA_HOST?Need to set HYDRA_HOST}"

if [[ -z "$HYDRA_HOST" ]]; then
    echo "Need to set HYDRA_HOST" 1>&2
    exit 1
fi

if [[ -z "$HYDRA_PORT" ]]; then
    echo "Need to set HYDRA_PORT" 1>&2
    exit 1
fi

if [[ -z "$ACHERON_HOST" ]]; then
    echo "Need to set ACHERON_HOST" 1>&2
    exit 1
fi

if [[ -z "$ACHERON_PORT" ]]; then
    echo "Need to set ACHERON_PORT" 1>&2
    exit 1
fi

if [[ -z "$ADMIN_AUTH_CREDENTIALS" ]]; then
    echo "Need to set ADMIN_AUTH_CREDENTIALS" 1>&2
    exit 1
fi

# Get access token with Hydra admin credentials
# admin:demo-password
echo "Obtaining access token";

token=$(curl --fail -X POST -H "Content-Type: application/x-www-form-urlencoded" \
-H "Authorization: Basic ${ADMIN_AUTH_CREDENTIALS}" \
-d 'grant_type=client_credentials&scope=hydra' \
"http://${HYDRA_HOST}:${HYDRA_PORT}/oauth2/token" | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["'access_token'"]');

if [ $? -ne 0 ]; then
    exit 1;
fi

echo "Access token is ${token}.";

# Use access token to create OAuth2 client for Acheron
echo "Creating Acheron client.";

curl --fail -X POST -H "Content-Type: application/json" -H "Authorization: Bearer ${token}" -d '{
    "id": "acheron",
    "client_name": "acheron",
    "client_secret": "secret",
    "redirect_uris": [],
    "grant_types": [
        "client_credentials"
    ],
    "response_types": [
        "token"
    ],
    "scope": "hydra.clients",
    "owner": "",
    "policy_uri": "",
    "tos_uri": "",
    "client_uri": "",
    "logo_uri": "",
    "contacts": null,
    "public": false
}' "http://${HYDRA_HOST}:${HYDRA_PORT}/clients";

if [ $? -ne 0 ]; then
    exit 1;
fi

echo "Created Acheron client.";

# Add the policy allowing Acheron to manage clients
echo "Adding policy to allow Acheron to manage clients.";

curl --fail -X POST -H "Content-Type: application/json" -H "Authorization: Bearer ${token}" -d '{
    "id":"",
    "description":"",
    "subjects": ["acheron"],
    "effect":"allow",
    "resources": ["rn:hydra:clients", "rn:hydra:clients:\u003c.*\u003e"],
    "actions": ["create","delete"],
    "conditions":{}
}' "http://${HYDRA_HOST}:${HYDRA_PORT}/policies";

if [ $? -ne 0 ]; then
    exit 1;
fi

echo "Added policy.";

# Add the Hydra route in Acheron
echo "Adding Hydra route.";

curl -X POST -H "Content-Type: application/json" -d '{
    "id": "hydra_realm1",
    "http_methods": [
      "POST"
    ],
    "path": "/hydra/realm1/**",
    "service_id": "hydra_realm1",
    "url": "http://'${HYDRA_HOST}':'${HYDRA_PORT}'",
    "keep_prefix": false,
    "retryable": false,
    "override_sensitive_headers": true,
    "sensitive_headers": []
}' "http://${ACHERON_HOST}:${ACHERON_PORT}/admin/routes";

if [ $? -ne 0 ]; then
    exit 1;
fi

echo "Added Hydra route.";
