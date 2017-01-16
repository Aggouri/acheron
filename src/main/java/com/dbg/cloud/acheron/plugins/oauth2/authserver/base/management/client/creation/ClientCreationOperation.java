package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation;

public interface ClientCreationOperation {

    ClientCreationResult result() throws TechnicalException;

    final class TechnicalException extends Exception {
    }
}
