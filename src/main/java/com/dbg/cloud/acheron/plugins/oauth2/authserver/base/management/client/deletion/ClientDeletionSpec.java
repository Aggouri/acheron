package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion;

public interface ClientDeletionSpec {

    ClientDeletionOperation operationForClient(String clientId);
}
