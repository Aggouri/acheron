package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection;

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

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("exp")
    private Long exp;

    @JsonProperty("iat")
    private Long iat;

    @JsonProperty("aud")
    private String aud;
}
