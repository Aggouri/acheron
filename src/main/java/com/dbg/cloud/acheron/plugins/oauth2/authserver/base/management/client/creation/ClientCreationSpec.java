package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.Client;

public interface ClientCreationSpec {

    ClientCreationOperation operationForClient(Client client);
}
