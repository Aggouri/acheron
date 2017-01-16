package com.dbg.cloud.acheron.plugins.oauth2.rest;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.Client;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Client;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Set;

@Getter
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
final class OAuth2ClientTO implements Client {

    public OAuth2ClientTO(final OAuth2Client client) {
        this(client, null);
    }

    public OAuth2ClientTO(final OAuth2Client client, final String clientSecret) {
        this(client.getId() != null ? client.getId().toString() : null,
                null,
                client.getClientId(),
                clientSecret,
                client.getConsumerId() != null ? client.getConsumerId().toString() : null,
                client.getConsumerName(),
                client.getConsumerCreatedAt(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                client.getCreatedAt());
    }

    @JsonView(value = View.Read.class)
    private final String id;

    @JsonProperty("client_name")
    private final String clientName;

    @JsonView(value = View.Read.class)
    @JsonProperty("client_id")
    private final String clientId;

    @JsonView(value = View.Read.class)
    @JsonProperty("client_secret")
    private final String clientSecret;

    @JsonView(value = View.Read.class)
    @JsonProperty("consumer_id")
    private final String consumerId;

    @JsonView(value = View.Read.class)
    @JsonProperty("consumer_name")
    private final String consumerName;

    @JsonView(value = View.Read.class)
    @JsonProperty("consumer_created_at")
    private final Date consumerCreatedAt;

    @JsonView(value = View.Register.class)
    @JsonProperty("redirect_uris")
    private final Set<String> redirectURIs;

    @JsonView(value = View.Register.class)
    @JsonProperty("grant_types")
    private final Set<String> grantTypes;

    @JsonView(value = View.Register.class)
    @JsonProperty("response_types")
    private final Set<String> responseTypes;

    @JsonView(value = View.Register.class)
    private final String scope;

    @JsonView(value = View.Register.class)
    private final String owner;

    @JsonView(value = View.Register.class)
    @JsonProperty("policy_uri")
    private final String policyURI;

    @JsonView(value = View.Register.class)
    @JsonProperty("tos_uri")
    private final String tosURI;

    @JsonView(value = View.Register.class)
    @JsonProperty("client_uri")
    private final String clientURI;

    @JsonView(value = View.Register.class)
    @JsonProperty("logo_uri")
    private final String logoURI;

    @JsonView(value = View.Register.class)
    @JsonProperty("contacts")
    private final String contacts;

    @JsonView(value = View.Register.class)
    @JsonProperty("public")
    private final Boolean isPublic;

    @JsonView(value = View.Read.class)
    @JsonProperty("created_at")
    private final Date createdAt;
}
