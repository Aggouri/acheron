package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.deletion;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionSpec;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

@AllArgsConstructor
public final class ClientDeletionSpecWithAuth implements ClientDeletionSpec {

    private final String clientDeletionURL;
    private final AuthenticationOperation operation;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public ClientDeletionOperation operationForClient(final String clientId) {
        return new ClientDeletionOperationWithAuthentication(
                clientDeletionURL, operation, clientId, restTemplateBuilder);
    }
}
