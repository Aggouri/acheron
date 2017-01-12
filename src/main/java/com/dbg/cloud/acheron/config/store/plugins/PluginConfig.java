package com.dbg.cloud.acheron.config.store.plugins;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public interface PluginConfig {

    UUID getId();

    String getName();

    String getRouteId();

    Set<String> getHttpMethods();

    UUID getConsumerId();

    String getConfig();

    boolean isEnabled();

    Date getCreatedAt();

    @AllArgsConstructor
    @Getter
    @ToString
    final class Default implements PluginConfig {
        private final UUID id;
        private final String name;
        private final String routeId;
        private final Set<String> httpMethods;
        private final UUID consumerId;
        private final String config;
        private final boolean isEnabled;
        private final Date createdAt;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    final class ForCreation implements PluginConfig {
        private final String name;
        private final String routeId;
        private final Set<String> httpMethods;
        private final UUID consumerId;
        private final String config;
        private final boolean isEnabled;

        // already set
        private final UUID id = null;
        private final Date createdAt = null;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @ToString
    final class Merge implements PluginConfig {
        private final UUID id;
        private final String name;
        private final String routeId;
        private final Set<String> httpMethods;
        private final UUID consumerId;
        private final String config;
        private final boolean isEnabled;
        private final Date createdAt;

        public Merge(final PluginConfig baseConfig, final PluginConfig mergeConfig) {
            this(
                    baseConfig.getId(),
                    mergeConfig.getName() != null ? mergeConfig.getName() : baseConfig.getName(),
                    mergeConfig.getRouteId() != null ? mergeConfig.getRouteId() : baseConfig.getRouteId(),
                    mergeConfig.getHttpMethods() != null ? mergeConfig.getHttpMethods() : baseConfig.getHttpMethods(),
                    mergeConfig.getConsumerId() != null ? mergeConfig.getConsumerId() : baseConfig.getConsumerId(),
                    mergeConfig.getConfig() != null ? mergeConfig.getConfig() : baseConfig.getConfig(),
                    mergeConfig.isEnabled(),
                    mergeConfig.getCreatedAt() != null ? mergeConfig.getCreatedAt() : baseConfig.getCreatedAt());
        }
    }

    @AllArgsConstructor
    @Getter
    @ToString
    final class WithoutCreatedDate implements PluginConfig {
        private final UUID id;
        private final String name;
        private final String routeId;
        private final Set<String> httpMethods;
        private final UUID consumerId;
        private final String config;
        private final boolean isEnabled;
        private final Date createdAt = null;

        public WithoutCreatedDate(final PluginConfig patch) {
            this(
                    patch.getId(),
                    patch.getName(),
                    patch.getRouteId(),
                    patch.getHttpMethods(),
                    patch.getConsumerId(),
                    patch.getConfig(),
                    patch.isEnabled()
            );
        }
    }
}
