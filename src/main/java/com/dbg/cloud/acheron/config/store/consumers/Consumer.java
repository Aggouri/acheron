package com.dbg.cloud.acheron.config.store.consumers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

public interface Consumer {

    UUID getId();

    String getName();

    Date getCreatedAt();

    @AllArgsConstructor
    @Getter
    final class Smart implements Consumer {
        private final UUID id;
        private final String name;
        private final Date createdAt;
    }

    @AllArgsConstructor
    @Getter
    final class ForCreation implements Consumer {
        private final UUID id = null;
        private final Date createdAt = null;
        private final String name;
    }
}