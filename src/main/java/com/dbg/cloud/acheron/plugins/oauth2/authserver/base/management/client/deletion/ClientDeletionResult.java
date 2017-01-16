package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion;

import lombok.Getter;

public interface ClientDeletionResult {

    boolean isOk();

    @Getter
    final class Successful implements ClientDeletionResult {
        private final boolean ok = true;
    }

    @Getter
    final class Unsuccessful implements ClientDeletionResult {
        private final boolean ok = false;
    }
}
