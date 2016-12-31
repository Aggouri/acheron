package com.dbg.cloud.acheron.filters.pre.authentication.oauth2.hydra;

import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.CredentialsStruct;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.OAuth2AuthorisationServer;
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
}
