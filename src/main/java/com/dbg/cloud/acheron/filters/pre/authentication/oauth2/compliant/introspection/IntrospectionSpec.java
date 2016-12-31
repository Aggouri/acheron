package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection;

import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication.AuthenticationOperation;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

public interface IntrospectionSpec {

    IntrospectionOperation operationForToken(String token);

    @AllArgsConstructor
    class Simple implements IntrospectionSpec {

        private final String introspectionURL;
        private final String bearerToken;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public IntrospectionOperation operationForToken(final String token) {
            return new IntrospectionOperation.Simple(introspectionURL, bearerToken, token, restTemplateBuilder);
        }
    }

    @AllArgsConstructor
    class IntrospectionSpecWithAuth implements IntrospectionSpec {

        private final String introspectionURL;
        private final AuthenticationOperation operation;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public IntrospectionOperation operationForToken(final String token) {
            return new IntrospectionOperation.IntrospectionOperationWithAuthentication(introspectionURL, operation,
                    token, restTemplateBuilder);
        }
    }

}
