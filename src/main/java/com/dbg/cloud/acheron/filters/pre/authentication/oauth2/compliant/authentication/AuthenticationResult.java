package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public final class AuthenticationResult {

    private final Optional<String> accessToken;
}
