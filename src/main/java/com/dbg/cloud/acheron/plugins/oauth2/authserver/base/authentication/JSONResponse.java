package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@JsonIgnoreProperties
@ToString
@Getter
@Accessors(fluent = true)
final class JSONResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;
}
