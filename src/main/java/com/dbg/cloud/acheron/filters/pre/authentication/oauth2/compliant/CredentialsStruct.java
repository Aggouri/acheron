package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public final class CredentialsStruct {

    private final String clientId;
    private final String clientSecret;
    private final Optional<String> bearerToken;
}
