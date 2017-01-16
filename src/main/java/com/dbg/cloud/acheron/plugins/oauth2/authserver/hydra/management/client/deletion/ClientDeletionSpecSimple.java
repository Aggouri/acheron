package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.deletion;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionSpec;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

@AllArgsConstructor
public final class ClientDeletionSpecSimple implements ClientDeletionSpec {

    private final String clientDeletionURL;
    private final String bearerToken;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public ClientDeletionOperation operationForClient(final String clientId) {
        return new ClientDeletionOperationSimple(clientDeletionURL, bearerToken, clientId, restTemplateBuilder);
    }
}
