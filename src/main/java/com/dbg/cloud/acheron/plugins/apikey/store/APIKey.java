package com.dbg.cloud.acheron.plugins.apikey.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

public interface APIKey {

    UUID getId();

    String getAPIKey();

    UUID getConsumerId();

    String getConsumerName();

    Date getConsumerCreatedAt();

    Date getCreatedAt();

    @AllArgsConstructor
    @Getter
    @ToString
    final class Default implements APIKey {
        private final UUID id;
        private final String aPIKey;
        private final UUID consumerId;
        private final String consumerName;
        private final Date consumerCreatedAt;
        private final Date createdAt;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    final class ForCreation implements APIKey {
        private final String aPIKey;
        private final UUID consumerId;
        private final String consumerName;
        private final Date consumerCreatedAt;

        // already set
        private final UUID id = null;
        private final Date createdAt = new Date();
    }
}
