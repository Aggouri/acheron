package com.dbg.cloud.acheron.consumers.service;

import com.datastax.driver.core.utils.UUIDs;
import com.dbg.cloud.acheron.consumers.Consumer;
import com.dbg.cloud.acheron.exception.TechnicalException;
import com.dbg.cloud.acheron.exception.ValidationException;
import com.dbg.cloud.acheron.consumers.store.ConsumerStore;
import com.dbg.cloud.acheron.pluginconfig.PluginConfig;
import com.dbg.cloud.acheron.pluginconfig.service.PluginConfigService;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKey;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKeyStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsumerService {

    List<Consumer> getAllConsumers() throws TechnicalException;

    Optional<Consumer> getConsumer(@NonNull UUID consumerId) throws TechnicalException;

    Consumer addNewConsumer(@NonNull Consumer consumer) throws ValidationException, TechnicalException;

    void deleteConsumer(@NonNull UUID consumerId) throws TechnicalException;

    @Service
    @AllArgsConstructor
    class ConsumerServiceImpl implements ConsumerService {

        private final ConsumerStore consumerStore;
        private final PluginConfigService pluginConfigService;
        private final APIKeyStore apiKeyStore; // TODO Replace with service

        @Override
        public List<Consumer> getAllConsumers() throws TechnicalException {
            try {
                return consumerStore.findAll();
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public Optional<Consumer> getConsumer(final @NonNull UUID consumerId) throws TechnicalException {
            try {
                return consumerStore.findById(consumerId);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public Consumer addNewConsumer(final @NonNull Consumer consumer) throws ValidationException,
                TechnicalException {
            validateConsumer(consumer);

            try {
                final UUID consumerId = UUIDs.random();
                final Date createdAt = new Date();
                return consumerStore.add(new Consumer.Smart(consumerId, consumer.getName(), createdAt));
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public void deleteConsumer(final @NonNull UUID consumerId) {
            try {
                consumerStore.deleteById(consumerId);

                // Delete all plugin configs linked to the consumer
                final List<PluginConfig> consumerPluginConfigs =
                        pluginConfigService.getPluginConfigsOfConsumer(consumerId);
                consumerPluginConfigs.forEach(
                        consumerPluginConfig -> pluginConfigService.deletePluginConfig(consumerPluginConfig.getId()));

                // Delete API Keys
                final List<APIKey> consumerAPIKeys = apiKeyStore.findByConsumer(consumerId);
                consumerAPIKeys.forEach(consumerAPIKey -> apiKeyStore.deleteById(consumerAPIKey.getId()));
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        private void validateConsumer(final Consumer consumer) throws ValidationException {
            if (consumer.getName() == null || consumer.getName().isEmpty()) {
                throw new ValidationException("consumer name cannot be empty");
            }
        }
    }
}
