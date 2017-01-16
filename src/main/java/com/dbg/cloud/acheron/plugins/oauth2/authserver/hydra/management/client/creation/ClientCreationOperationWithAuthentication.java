package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.creation;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationResult;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.Client;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;

@AllArgsConstructor
@Slf4j
final class ClientCreationOperationWithAuthentication implements ClientCreationOperation {

    private final String clientCreationEndpointURL;
    private final AuthenticationOperation authenticationOperation;
    private final Client client;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public ClientCreationResult result() throws TechnicalException {
        // Authenticate
        final AuthenticationResult authResult;
        try {
            authResult = authenticationOperation.result();
        } catch (AuthenticationOperation.TechnicalException e) {
            throw new TechnicalException();
        }

        // Fail if the authentication response does not have an access token
        if (!authResult.accessToken().isPresent()) {
            log.error("Could not obtain access token from authorisation server");
            throw new TechnicalException();
        } else {
            return new ClientCreationOperationSimple(clientCreationEndpointURL, authResult.accessToken().get().token(),
                    client, restTemplateBuilder).result();
        }
    }
}
