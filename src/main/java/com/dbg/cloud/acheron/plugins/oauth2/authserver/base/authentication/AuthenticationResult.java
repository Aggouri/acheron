package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.AccessToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public final class AuthenticationResult {

    private final Optional<AccessToken> accessToken;
}
