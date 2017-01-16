package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.creation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@Accessors(fluent = true)
final class JSONResponse {

    @JsonProperty("id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;
}
