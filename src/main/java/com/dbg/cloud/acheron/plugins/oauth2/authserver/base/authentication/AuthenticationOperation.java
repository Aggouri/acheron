package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.AccessToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

public interface AuthenticationOperation {

    AuthenticationResult result() throws TechnicalException;

    final class TechnicalException extends Exception {
    }

    @Slf4j
    @AllArgsConstructor
    final class BasicAuthWithClientCredentials implements AuthenticationOperation {
        private final String tokenEndpointURL;
        private final String clientId;
        private final String clientSecret;
        private final String scopes;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public AuthenticationResult result() throws TechnicalException {
            final Optional<AccessToken> accessToken;
            final RestTemplate restTemplate = buildRestTemplate();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("scope", scopes);

            final HttpEntity<MultiValueMap<String, String>> authenticationRequestHttpEntity =
                    new HttpEntity<>(map, headers);

            try {
                log.info("Authenticating against OAuth2 server");
                final JSONResponse jsonResponse = restTemplate.postForObject(
                        tokenEndpointURL,
                        authenticationRequestHttpEntity,
                        JSONResponse.class);

                final String token = jsonResponse.accessToken();
                final Optional<Long> expiresIn = Optional.ofNullable(jsonResponse.expiresIn());
                final Instant expiration = Instant.now().plusSeconds(expiresIn.orElse(0L));

                accessToken = Optional.of(new AccessToken.Normal(token, expiration)); // NPE will be caught
            } catch (RuntimeException e) {
                log.error("Could not authenticate with client credentials against authorisation server", e);
                throw new TechnicalException();
            }

            return new AuthenticationResult(accessToken);
        }

        private RestTemplate buildRestTemplate() {
            return restTemplateBuilder
                    .additionalMessageConverters(new FormHttpMessageConverter())
                    .basicAuthorization(clientId, clientSecret)
                    .build();
        }
    }

    @AllArgsConstructor
    final class AuthenticationOperationWithCache implements AuthenticationOperation {

        private final AuthenticationOperation authenticationOperation;
        private AuthenticationResult cachedResult;

        public AuthenticationOperationWithCache(final AuthenticationOperation authenticationOperation) {
            this(authenticationOperation, new AuthenticationResult(Optional.empty()));
        }

        @Override
        public AuthenticationResult result() throws TechnicalException {
            if (!isAlreadyAuthenticated(cachedResult.accessToken())) {
                synchronized (this) {
                    cachedResult = authenticationOperation.result();
                }
            }
            return cachedResult;
        }

        private boolean isAlreadyAuthenticated(final Optional<AccessToken> accessToken) {
            return accessToken.isPresent() &&
                    Instant.now().isBefore(accessToken.get().expiration().orElse(Instant.MIN));
        }
    }
}
