package com.dbg.cloud.acheron.plugins.oauth2.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

public interface OAuth2Client {

    UUID getId();

    String getClientId();

    UUID getConsumerId();

    String getConsumerName();

    Date getConsumerCreatedAt();

    Date getCreatedAt();

    @AllArgsConstructor
    @Getter
    @ToString
    final class Default implements OAuth2Client {
        private final UUID id;
        private final String clientId;
        private final UUID consumerId;
        private final String consumerName;
        private final Date consumerCreatedAt;
        private final Date createdAt;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    final class ForCreation implements OAuth2Client {
        private final String clientId;
        private final UUID consumerId;
        private final String consumerName;
        private final Date consumerCreatedAt;

        // already set
        private final UUID id = null;
        private final Date createdAt = new Date();
    }
}
