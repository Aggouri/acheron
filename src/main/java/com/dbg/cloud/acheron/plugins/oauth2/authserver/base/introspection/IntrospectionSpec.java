package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

public interface IntrospectionSpec {

    IntrospectionOperation operationForToken(String token);

    @AllArgsConstructor
    final class Simple implements IntrospectionSpec {

        private final String introspectionURL;
        private final String bearerToken;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public IntrospectionOperation operationForToken(final String token) {
            return new IntrospectionOperation.Simple(introspectionURL, bearerToken, token, restTemplateBuilder);
        }
    }

    @AllArgsConstructor
    final class IntrospectionSpecWithAuth implements IntrospectionSpec {

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
