package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.Client;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Set;

@AllArgsConstructor
@Getter
public final class HydraClient implements Client {

    public HydraClient(final @NonNull Client client) {
        this(client.getClientName(), client.getClientId(), client.getRedirectURIs(), client.getGrantTypes(),
                client.getResponseTypes(), client.getScope(), client.getOwner(), client.getPolicyURI(),
                client.getTosURI(), client.getClientURI(), client.getLogoURI(), client.getContacts(),
                client.getIsPublic());
    }

    @JsonProperty("client_name")
    private final String clientName;

    @JsonProperty("consumer_id")
    private final String clientId;

    @JsonProperty("redirect_uris")
    private final Set<String> redirectURIs;

    @JsonProperty("grant_types")
    private final Set<String> grantTypes;

    @JsonProperty("response_types")
    private final Set<String> responseTypes;

    private final String scope;

    private final String owner;

    @JsonProperty("policy_uri")
    private final String policyURI;

    @JsonProperty("tos_uri")
    private final String tosURI;

    @JsonProperty("client_uri")
    private final String clientURI;

    @JsonProperty("logo_uri")
    private final String logoURI;

    @JsonProperty("contacts")
    private final String contacts;

    @JsonProperty("public")
    private final Boolean isPublic;
}
