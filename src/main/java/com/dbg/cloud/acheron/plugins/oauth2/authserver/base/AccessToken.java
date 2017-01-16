package com.dbg.cloud.acheron.plugins.oauth2.authserver.base;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection.IntrospectionOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection.IntrospectionResult;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection.IntrospectionSpec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Optional;

public interface AccessToken {

    String token();

    Optional<Instant> expiration();

    IntrospectionResult introspection(IntrospectionSpec introspectionSpec) throws
            IntrospectionOperation.TechnicalException;

    @Slf4j
    final class BearerToken implements AccessToken {

        private static final String ACCESS_TOKEN = "access_token";
        private static final String BEARER_TYPE = "Bearer";

        private final String accessToken;

        public BearerToken(@NonNull final HttpServletRequest request) throws BearerTokenException {
            accessToken = extractToken(request);
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new BearerTokenException();
            }
        }

        @Override
        public String token() {
            return accessToken;
        }

        @Override
        public Optional<Instant> expiration() {
            return Optional.empty();
        }

        @Override
        public IntrospectionResult introspection(final IntrospectionSpec introspectionSpec) throws
                IntrospectionOperation.TechnicalException {

            final IntrospectionOperation operation = introspectionSpec.operationForToken(accessToken);
            return operation.result();
        }

        private String extractToken(final HttpServletRequest request) {
            String token = extractHeaderToken(request);

            // Bearer type allows a request parameter as well
            if (token == null) {
                log.info("{} not found in headers. Trying request parameters.", ACCESS_TOKEN);
                token = request.getParameter(ACCESS_TOKEN);
                if (token == null) {
                    log.info("{} not found in request parameters. Not an OAuth2 request.", ACCESS_TOKEN);
                }
            }

            return token;
        }

        private String extractHeaderToken(final HttpServletRequest request) {
            final Enumeration<String> headers = request.getHeaders("Authorization");
            while (headers.hasMoreElements()) { // typically there is only one (most servers enforce that)
                final String value = headers.nextElement();
                if ((value.toLowerCase().startsWith(BEARER_TYPE.toLowerCase()))) {
                    String authHeaderValue = value.substring(BEARER_TYPE.length()).trim();

                    int commaIndex = authHeaderValue.indexOf(',');
                    if (commaIndex > 0) {
                        authHeaderValue = authHeaderValue.substring(0, commaIndex);
                    }
                    return authHeaderValue;
                }
            }

            return null;
        }

        public final class BearerTokenException extends Exception {
        }
    }

    final class Normal implements AccessToken {

        private final String token;
        private final Instant expiration;

        public Normal(final @NonNull String token, final @NonNull Instant expiration) {
            this.token = token;
            this.expiration = expiration;
        }

        @Override
        public String token() {
            return token;
        }

        @Override
        public Optional<Instant> expiration() {
            return Optional.of(expiration);
        }

        @Override
        public IntrospectionResult introspection(IntrospectionSpec introspectionSpec) throws IntrospectionOperation
                .TechnicalException {
            final IntrospectionOperation operation = introspectionSpec.operationForToken(token);
            return operation.result();
        }
    }
}