package com.dbg.cloud.acheron.config.consumers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

public interface Consumer {

    UUID getId();

    String getName();

    Date getCreatedAt();

    @AllArgsConstructor
    @Getter
    @ToString
    final class Smart implements Consumer {
        private final UUID id;
        private final String name;
        private final Date createdAt;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    final class ForCreation implements Consumer {
        private final String name;

        // already set
        private final UUID id = null;
        private final Date createdAt = new Date();
    }
}