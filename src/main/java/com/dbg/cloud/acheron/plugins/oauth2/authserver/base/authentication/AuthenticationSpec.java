package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.AccessToken;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.CredentialsStruct;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Instant;
import java.util.Optional;

public interface AuthenticationSpec {

    AuthenticationOperation operation();

    @AllArgsConstructor
    final class Simple implements AuthenticationSpec {

        private final String tokenEndpointURL;
        private final CredentialsStruct credentials;
        private final String scopes;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public AuthenticationOperation operation() {
            if (isAlreadyAuthenticated(credentials.accessToken())) {
                // Operation returning bearer token
                return () -> new AuthenticationResult(credentials.accessToken());
            } else {
                // Operation calling the server and returning the server's result
                return new AuthenticationOperation.BasicAuthWithClientCredentials(tokenEndpointURL,
                        credentials.clientId(), credentials.clientSecret(), scopes, restTemplateBuilder);
            }
        }

        private boolean isAlreadyAuthenticated(final Optional<AccessToken> accessToken) {
            return accessToken.isPresent() &&
                    Instant.now().isBefore(accessToken.get().expiration().orElse(Instant.MIN));
        }
    }

    final class AuthenticationSpecWithCache implements AuthenticationSpec {

        private final AuthenticationSpec spec;
        private AuthenticationOperation authenticationOperation = null;

        public AuthenticationSpecWithCache(AuthenticationSpec spec) {
            this.spec = spec;
        }

        @Override
        public AuthenticationOperation operation() {
            if (authenticationOperation == null) {
                authenticationOperation =
                        new AuthenticationOperation.AuthenticationOperationWithCache(spec.operation());
            }

            return authenticationOperation;
        }
    }
}
