package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.deletion;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationResult;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;

@AllArgsConstructor
@Slf4j
final class ClientDeletionOperationWithAuthentication implements ClientDeletionOperation {

    private final String clientDeletionEndpointURL;
    private final AuthenticationOperation authenticationOperation;
    private final String clientId;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public ClientDeletionResult result() throws TechnicalException {
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
            return new ClientDeletionOperationSimple(clientDeletionEndpointURL, authResult.accessToken().get().token(),
                    clientId, restTemplateBuilder).result();
        }
    }
}
