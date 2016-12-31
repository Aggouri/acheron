package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication;

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
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public AuthenticationResult result() throws TechnicalException {
            Optional<String> accessToken = null;
            final RestTemplate restTemplate = buildRestTemplate();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");

            final HttpEntity<MultiValueMap<String, String>> authenticationRequestHttpEntity =
                    new HttpEntity<>(map, headers);

            try {
                final JSONResponse jsonResponse = restTemplate.postForObject(
                        tokenEndpointURL,
                        authenticationRequestHttpEntity,
                        JSONResponse.class);

                accessToken = Optional.of(jsonResponse.accessToken()); // NPE will be caught
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
}
