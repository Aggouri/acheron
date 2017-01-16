package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.creation;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.Client;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationSpec;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;

@AllArgsConstructor
public final class ClientCreationSpecSimple implements ClientCreationSpec {

    private final String clientCreationURL;
    private final String bearerToken;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public ClientCreationOperation operationForClient(final Client client) {
        return new ClientCreationOperationSimple(clientCreationURL, bearerToken, client, restTemplateBuilder);
    }
}
