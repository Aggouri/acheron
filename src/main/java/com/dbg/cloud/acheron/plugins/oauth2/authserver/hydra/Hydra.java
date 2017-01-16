package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.CredentialsStruct;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.OAuth2AuthorisationServer;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationResult;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationSpec;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionSpec;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.creation.ClientCreationSpecSimple;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.creation.ClientCreationSpecWithAuth;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.deletion.ClientDeletionSpecSimple;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.deletion.ClientDeletionSpecWithAuth;
import org.springframework.boot.web.client.RestTemplateBuilder;

public final class Hydra extends OAuth2AuthorisationServer.SpecCompliantServer {
    private final String rootURL;

    public Hydra(final String rootURL, final CredentialsStruct credentials,
                 final RestTemplateBuilder restTemplateBuilder) {
        super(credentials, restTemplateBuilder);
        this.rootURL = rootURL;
    }

    @Override
    protected String tokenEndpoint() {
        return rootURL + "/oauth2/token";
    }

    @Override
    protected String introspectionEndpoint() {
        return rootURL + "/oauth2/introspect";
    }

    @Override
    protected String operatingScopes() {
        return "hydra.clients";
    }

    @Override
    public ClientCreationSpec clientCreationSpec(final AuthenticationResult authenticationResult) {
        return new ClientCreationSpecSimple(
                getClientCreationURL(),
                authenticationResult.accessToken().get().token(),
                getRestTemplateBuilder());
    }

    @Override
    public ClientCreationSpec clientCreationSpec(final AuthenticationOperation authenticationOperation) {
        return new ClientCreationSpecWithAuth(
                getClientCreationURL(),
                authenticationOperation,
                getRestTemplateBuilder());
    }

    @Override
    public ClientDeletionSpec clientDeletionSpec(AuthenticationResult authenticationResult) {
        return new ClientDeletionSpecSimple(
                getClientDeletionURL(),
                authenticationResult.accessToken().get().token(),
                getRestTemplateBuilder()
        );
    }

    @Override
    public ClientDeletionSpec clientDeletionSpec(AuthenticationOperation authenticationOperation) {
        return new ClientDeletionSpecWithAuth(
                getClientDeletionURL(),
                authenticationOperation,
                getRestTemplateBuilder());
    }

    private String getClientCreationURL() {
        return rootURL + "/clients";
    }

    private String getClientDeletionURL() {
        return rootURL + "/clients";
    }
}
