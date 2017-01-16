package com.dbg.cloud.acheron.plugins.oauth2.authserver.base;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationResult;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationSpec;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection.IntrospectionSpec;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationSpec;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionSpec;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.web.client.RestTemplateBuilder;

public interface OAuth2AuthorisationServer {

    AuthenticationSpec authenticationSpec();

    IntrospectionSpec introspectionSpec(AuthenticationResult authenticationResult);

    IntrospectionSpec introspectionSpec(AuthenticationOperation authenticationOperation);

    ClientCreationSpec clientCreationSpec(AuthenticationResult authenticationResult);

    ClientCreationSpec clientCreationSpec(AuthenticationOperation authenticationOperation);

    ClientDeletionSpec clientDeletionSpec(AuthenticationResult authenticationResult);

    ClientDeletionSpec clientDeletionSpec(AuthenticationOperation authenticationOperation);

    @AllArgsConstructor
    abstract class SpecCompliantServer implements OAuth2AuthorisationServer {

        private final CredentialsStruct credentials;

        @Getter(AccessLevel.PROTECTED)
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public final AuthenticationSpec authenticationSpec() {
            return new AuthenticationSpec.Simple(tokenEndpoint(), credentials, operatingScopes(), restTemplateBuilder);
        }

        @Override
        public final IntrospectionSpec introspectionSpec(final AuthenticationResult authenticationResult) {
            return new IntrospectionSpec.Simple(
                    introspectionEndpoint(),
                    authenticationResult.accessToken().get().token(),
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

        protected abstract String operatingScopes();
    }

    final class ServerWithCachedAuthentication implements OAuth2AuthorisationServer {
        private final OAuth2AuthorisationServer oAuth2Server;
        private AuthenticationSpec authenticationSpec;

        public ServerWithCachedAuthentication(final OAuth2AuthorisationServer oAuth2Server) {
            this.oAuth2Server = oAuth2Server;
            this.authenticationSpec = null;
        }

        @Override
        public AuthenticationSpec authenticationSpec() {
            if (authenticationSpec == null) {
                authenticationSpec =
                        new AuthenticationSpec.AuthenticationSpecWithCache(oAuth2Server.authenticationSpec());
            }
            return authenticationSpec;
        }

        @Override
        public IntrospectionSpec introspectionSpec(AuthenticationOperation authenticationOperation) {
            return oAuth2Server.introspectionSpec(authenticationOperation);
        }

        @Override
        public IntrospectionSpec introspectionSpec(AuthenticationResult authenticationResult) {
            return oAuth2Server.introspectionSpec(authenticationResult);
        }

        @Override
        public ClientCreationSpec clientCreationSpec(AuthenticationOperation authenticationOperation) {
            return oAuth2Server.clientCreationSpec(authenticationOperation);
        }

        @Override
        public ClientDeletionSpec clientDeletionSpec(AuthenticationResult authenticationResult) {
            return oAuth2Server.clientDeletionSpec(authenticationResult);
        }

        @Override
        public ClientDeletionSpec clientDeletionSpec(AuthenticationOperation authenticationOperation) {
            return oAuth2Server.clientDeletionSpec(authenticationOperation);
        }

        @Override
        public ClientCreationSpec clientCreationSpec(AuthenticationResult authenticationResult) {
            return oAuth2Server.clientCreationSpec(authenticationResult);
        }

        protected OAuth2AuthorisationServer getAuthServer() {
            return oAuth2Server;
        }
    }
}
