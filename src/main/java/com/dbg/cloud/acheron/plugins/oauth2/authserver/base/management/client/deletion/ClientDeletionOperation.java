package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion;

public interface ClientDeletionOperation {

    ClientDeletionResult result() throws TechnicalException;

    final class TechnicalException extends Exception {
    }
}
