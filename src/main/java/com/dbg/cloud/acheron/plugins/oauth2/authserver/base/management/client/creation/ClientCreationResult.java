package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation;

import lombok.Getter;
import lombok.experimental.Accessors;

public interface ClientCreationResult {

    String clientId();

    String clientSecret();

    @Getter
    @Accessors(fluent = true)
    final class NoResult implements ClientCreationResult {
        private final String clientId = null;
        private final String clientSecret = null;
    }
}
