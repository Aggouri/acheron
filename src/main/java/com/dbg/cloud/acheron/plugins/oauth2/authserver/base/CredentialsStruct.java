package com.dbg.cloud.acheron.plugins.oauth2.authserver.base;

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
    private final Optional<AccessToken> accessToken;
}
