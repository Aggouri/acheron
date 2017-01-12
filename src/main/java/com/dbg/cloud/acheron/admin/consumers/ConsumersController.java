package com.dbg.cloud.acheron.admin.consumers;

import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.config.store.consumers.ConsumerStore;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/consumers")
@AllArgsConstructor
@Slf4j
final class ConsumersController {

    private final ConsumerStore consumerStore;
    private final PluginConfigStore pluginConfigStore;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<ConsumerTO> readConsumers() {
        final List<Consumer> consumerList = consumerStore.findAll();
        return consumerList.stream().map(consumer -> new ConsumerTO(consumer)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/{consumerId}", method = RequestMethod.GET)
    public ResponseEntity<?> readConsumer(final @PathVariable String consumerId) {
        final UUID uuidConsumerId =
                parseConsumerUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        final Optional<Consumer> optionalConsumer = consumerStore.findById(uuidConsumerId);

        return ResponseEntity.ok(new ConsumerTO(
                optionalConsumer.orElseThrow(() -> new ConsumerNotFoundException(consumerId))));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addConsumer(final @JsonView(View.Create.class) @RequestBody ConsumerTO consumer) {
        // We only deserialize 'name'
        if (!validateConsumer(consumer)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(consumerStore.add(new Consumer.ForCreation(consumer.getName())));
    }

    @RequestMapping(value = "/{consumerId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteConsumer(final @PathVariable String consumerId) {
        final UUID uuidConsumerId =
                parseConsumerUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        consumerStore.deleteById(uuidConsumerId);

        // Delete all plugin configs linked to the consumer
        final List<PluginConfig> consumerPluginConfigs = pluginConfigStore.findByConsumer(uuidConsumerId);

        consumerPluginConfigs.forEach(
                consumerPluginConfig -> pluginConfigStore.deleteById(consumerPluginConfig.getId()));

        return ResponseEntity.noContent().build();
    }

    private boolean validateConsumer(final ConsumerTO consumer) {
        return consumer.getName() != null;
    }

    private Optional<UUID> parseConsumerUUID(final String consumerId) {
        UUID uuidConsumerId = null;
        try {
            uuidConsumerId = UUID.fromString(consumerId);
        } catch (Exception e) {
            log.info("Passed consumer id is not a UUID {}", consumerId);
        }
        return Optional.ofNullable(uuidConsumerId);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class ConsumerNotFoundException extends RuntimeException {
        public ConsumerNotFoundException(final String consumerId) {
            super("Could not find consumer '" + consumerId + "'");
        }
    }
}
