package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant;

import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection.IntrospectionOperation;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection.IntrospectionResult;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection.IntrospectionSpec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public interface AccessToken {

    String token();

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
}