package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant;

import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication.AuthenticationResult;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication.AuthenticationSpec;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection.IntrospectionSpec;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

public interface OAuth2AuthorisationServer {

    AuthenticationSpec authenticationSpec();

    IntrospectionSpec introspectionSpec(AuthenticationResult authenticationResult);

    IntrospectionSpec introspectionSpec(AuthenticationOperation authenticationOperation);

    @AllArgsConstructor
    abstract class SpecCompliantServer implements OAuth2AuthorisationServer {

        private final CredentialsStruct credentials;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public final AuthenticationSpec authenticationSpec() {
            return new AuthenticationSpec.Simple(tokenEndpoint(), credentials, restTemplateBuilder);
        }

        @Override
        public final IntrospectionSpec introspectionSpec(final AuthenticationResult authenticationResult) {
            return new IntrospectionSpec.Simple(
                    introspectionEndpoint(),
                    authenticationResult.accessToken().get(),
                    restTemplateBuilder);
        }

        @Override
        public final IntrospectionSpec introspectionSpec(final AuthenticationOperation authenticationOperation) {
            return new IntrospectionSpec.IntrospectionSpecWithAuth(
                    introspectionEndpoint(),
                    authenticationOperation,
                    restTemplateBuilder);
        }

        protected abstract String tokenEndpoint();

        protected abstract String introspectionEndpoint();
    }
}
