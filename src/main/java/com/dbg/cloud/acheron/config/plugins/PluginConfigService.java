package com.dbg.cloud.acheron.config.plugins;

import com.datastax.driver.core.utils.UUIDs;
import com.dbg.cloud.acheron.config.common.TechnicalException;
import com.dbg.cloud.acheron.config.common.ValidationException;
import com.dbg.cloud.acheron.config.plugins.store.PluginConfigStore;
import com.dbg.cloud.acheron.config.routing.RouteService;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

public interface PluginConfigService {

    List<PluginConfig> getAllPluginConfigs() throws TechnicalException;

    List<PluginConfig> getPluginConfigsOfConsumer(@NonNull UUID consumerId) throws TechnicalException;

    List<PluginConfig> getPluginConfigsOfRoute(@NonNull String routeId) throws TechnicalException;

    Optional<PluginConfig> getPluginConfig(@NonNull UUID pluginConfigId) throws TechnicalException;

    PluginConfig addNewPluginConfig(@NonNull PluginConfig pluginConfig) throws ValidationException, TechnicalException;

    PluginConfig replacePluginConfig(@NonNull PluginConfig pluginConfig) throws ValidationException, TechnicalException;

    PluginConfig mergePluginConfig(@NonNull PluginConfig pluginConfig) throws ValidationException, TechnicalException;

    void deletePluginConfig(@NonNull UUID pluginConfigId) throws TechnicalException;

    @Service
    class PluginConfigServiceImpl implements PluginConfigService {

        private static final Collection<String> SUPPORTED_PLUGINS = Arrays.asList("oauth2", "api_key", "correlation_id",
                "rate_limiting");

        private final PluginConfigStore pluginConfigStore;
        private final RouteService routeService;

        public PluginConfigServiceImpl(final PluginConfigStore pluginConfigStore,
                                       final @Lazy RouteService routeService) {
            this.pluginConfigStore = pluginConfigStore;
            this.routeService = routeService;
        }


        @Override
        public List<PluginConfig> getAllPluginConfigs() throws TechnicalException {
            try {
                return pluginConfigStore.findAll();
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public List<PluginConfig> getPluginConfigsOfConsumer(final @NonNull UUID consumerId) throws TechnicalException {
            try {
                return pluginConfigStore.findByConsumer(consumerId);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public List<PluginConfig> getPluginConfigsOfRoute(final @NonNull String routeId) throws TechnicalException {
            try {
                return pluginConfigStore.findByRoute(routeId);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public Optional<PluginConfig> getPluginConfig(final @NonNull UUID pluginConfigId) throws TechnicalException {
            try {
                return pluginConfigStore.findById(pluginConfigId);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public PluginConfig addNewPluginConfig(final @NonNull PluginConfig pluginConfig) throws ValidationException,
                TechnicalException {

            validatePluginConfigForCreate(pluginConfig);

            try {
                // Generate ID and creation date
                final UUID pluginConfigId = UUIDs.random();
                final Date createdAt = new Date();

                return pluginConfigStore.add(
                        new PluginConfig.Default(
                                pluginConfigId,
                                pluginConfig.getName(),
                                pluginConfig.getRouteId(),
                                pluginConfig.getHttpMethods(),
                                pluginConfig.getConsumerId(),
                                pluginConfig.getConfig(),
                                pluginConfig.isEnabled(),
                                createdAt));
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public PluginConfig replacePluginConfig(final @NonNull PluginConfig pluginConfig)
                throws ValidationException, TechnicalException {

            validatePluginConfigForReplace(pluginConfig);

            try {
                this.getPluginConfig(pluginConfig.getId()).orElseThrow(() -> new ValidationException("not found"));

                return pluginConfigStore.update(new PluginConfig.WithoutCreatedDate(pluginConfig));
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public PluginConfig mergePluginConfig(final @NonNull PluginConfig pluginConfig) throws ValidationException,
                TechnicalException {

            validatePluginConfigForMerge(pluginConfig);

            try {
                final UUID uuid = pluginConfig.getId();
                final PluginConfig existingPluginConfig =
                        this.getPluginConfig(uuid).orElseThrow(() -> new ValidationException("not found"));

                final PluginConfig mergedPluginConfig =
                        new PluginConfig.WithoutCreatedDate(
                                new PluginConfig.Merge(
                                        existingPluginConfig,
                                        pluginConfig));

                return pluginConfigStore.update(mergedPluginConfig);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public void deletePluginConfig(final @NonNull UUID pluginConfigId) throws TechnicalException {
            try {
                pluginConfigStore.deleteById(pluginConfigId);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        private void validatePluginConfigForCreate(final PluginConfig pluginConfig) throws ValidationException,
                TechnicalException {
            if (!SUPPORTED_PLUGINS.contains(pluginConfig.getName())) {
                throw new ValidationException("plugin is not supported");
            }

            if (pluginConfig.getRouteId() == null || pluginConfig.getRouteId().isEmpty()) {
                throw new ValidationException("route id cannot be empty");
            }

            routeService.getRoute(pluginConfig.getRouteId()).orElseThrow(
                    () -> new ValidationException("route not found"));
        }

        private void validatePluginConfigForReplace(final PluginConfig pluginConfig) throws ValidationException {
            if (pluginConfig.getId() == null) {
                throw new ValidationException("plugin id cannot be null");
            }

            validatePluginConfigForCreate(pluginConfig);
        }

        private void validatePluginConfigForMerge(final PluginConfig pluginConfig) throws ValidationException {
            if (pluginConfig.getId() == null) {
                throw new ValidationException("plugin id cannot be null");
            }

            // if present, plugin name is supported
            if (pluginConfig.getName() != null && !SUPPORTED_PLUGINS.contains(pluginConfig.getName())) {
                throw new ValidationException("plugin is not supported");
            }

            // if present, route id must not be empty and must exist
            if (pluginConfig.getRouteId() != null) {
                if (pluginConfig.getRouteId().isEmpty()) {
                    throw new ValidationException("route id cannot be set to an empty value");
                }

                routeService.getRoute(pluginConfig.getRouteId()).orElseThrow(
                        () -> new ValidationException("route not found"));
            }
        }
    }
}
