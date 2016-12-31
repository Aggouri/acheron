package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication;

import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.CredentialsStruct;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Optional;

public interface AuthenticationSpec {

    AuthenticationOperation operation();

    @AllArgsConstructor
    final class Simple implements AuthenticationSpec {

        private final String tokenEndpointURL;
        private final CredentialsStruct credentials;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public AuthenticationOperation operation() {
            if (credentials.bearerToken().isPresent()) {
                // Operation returning bearer token
                final String bearerToken = credentials.bearerToken().get();
                return () -> new AuthenticationResult(Optional.of(bearerToken));
            } else {
                // Operation calling the server and returning the server's result
                return new AuthenticationOperation.BasicAuthWithClientCredentials(tokenEndpointURL,
                        credentials.clientId(), credentials.clientSecret(), restTemplateBuilder);
            }
        }
    }
}
