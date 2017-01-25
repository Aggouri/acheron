package com.dbg.cloud.acheron.plugins.apikey.rest;

import com.dbg.cloud.acheron.config.consumers.Consumer;
import com.dbg.cloud.acheron.config.consumers.ConsumerService;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKey;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKeyStore;
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
@RequestMapping("/admin")
@AllArgsConstructor
@Slf4j
final class APIKeyController {

    private final APIKeyStore apiKeyStore;
    private final ConsumerService consumerService;


    @RequestMapping(value = "/consumers/{consumerId}/api-keys", method = RequestMethod.GET)
    public List<APIKeyTO> readAPIKeysOfConsumer(final @PathVariable String consumerId) {
        final UUID uuidConsumerId = parseUUID(consumerId).orElseThrow(() -> new BadRequestException());
        final List<APIKey> apiKeyList = apiKeyStore.findByConsumer(uuidConsumerId);
        return apiKeyList.stream().map(apiKey -> new APIKeyTO(apiKey)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/consumers/{consumerId}/api-keys/{apiKeyId}", method = RequestMethod.GET)
    public ResponseEntity<?> readAPIKeyOfConsumer(final @PathVariable String consumerId,
                                                  final @PathVariable String apiKeyId) {
        final UUID uuidConsumerId =
                parseUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        final UUID uuidAPIKeyId =
                parseUUID(apiKeyId).orElseThrow(() -> new APIKeyNotFoundException(apiKeyId));
        final Optional<APIKey> optionalAPIKey = apiKeyStore.findById(uuidAPIKeyId);

        final APIKey apiKey = optionalAPIKey.orElseThrow(() -> new APIKeyNotFoundException(apiKeyId));
        if (!uuidConsumerId.equals(apiKey.getConsumerId())) {
            throw new ConsumerNotFoundException(consumerId);
        }
        return ResponseEntity.ok(new APIKeyTO(apiKey));
    }

    @RequestMapping(value = "/consumers/{consumerId}/api-keys", method = RequestMethod.POST)
    public ResponseEntity<?> addAPIKeyToConsumer(final @PathVariable String consumerId,
                                                 final @JsonView(View.Register.class) @RequestBody APIKeyTO apiKey) {
        if (!validateRegistration(apiKey)) {
            return ResponseEntity.badRequest().build();
        }

        final UUID uuidConsumerId = parseUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        final Consumer consumer = consumerService.getConsumer(uuidConsumerId).orElseThrow(
                () -> new ConsumerNotFoundException(consumerId));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new APIKeyTO(
                        apiKeyStore.add(new APIKey.ForCreation(apiKey.getApiKey(), consumer.getId(), consumer.getName(),
                                consumer.getCreatedAt()))));
    }

    @RequestMapping(value = "/consumers/{consumerId}/api-keys/{apiKeyId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAPIKeyOfConsumer(final @PathVariable String consumerId,
                                                    final @PathVariable String apiKeyId) {

        final UUID uuidConsumerId = parseUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        final UUID uuidAPIKeyId = parseUUID(apiKeyId).orElseThrow(() -> new APIKeyNotFoundException(apiKeyId));

        final APIKey apiKey = apiKeyStore.findById(uuidAPIKeyId).orElseThrow(
                () -> new APIKeyNotFoundException(apiKeyId));

        if (uuidConsumerId.equals(apiKey.getConsumerId())) {
            apiKeyStore.deleteById(uuidAPIKeyId);
        } else {
            throw new APIKeyNotFoundException(apiKeyId);
        }

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/plugins/api-key/keys/{apiKeyId}", method = RequestMethod.GET)
    public ResponseEntity<?> readAPIKey(final @PathVariable String apiKeyId) {
        final UUID uuidAPIKeyId =
                parseUUID(apiKeyId).orElseThrow(() -> new APIKeyNotFoundException(apiKeyId));
        final Optional<APIKey> optionalAPIKey = apiKeyStore.findById(uuidAPIKeyId);

        return ResponseEntity.ok(new APIKeyTO(
                optionalAPIKey.orElseThrow(() -> new APIKeyNotFoundException(apiKeyId))));
    }

    @RequestMapping(value = "/plugins/api-key/keys/{apiKeyId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAPIKey(final @PathVariable String apiKeyId) {
        final UUID uuidAPIKeyId =
                parseUUID(apiKeyId).orElseThrow(() -> new APIKeyNotFoundException(apiKeyId));
        apiKeyStore.deleteById(uuidAPIKeyId);

        return ResponseEntity.noContent().build();
    }

    private boolean validateRegistration(final APIKeyTO apiKey) {
        // if present, API key must not be an empty string
        return apiKey.getApiKey() == null || (apiKey.getApiKey() != null && !apiKey.getApiKey().trim().isEmpty());
    }

    private Optional<UUID> parseUUID(final String candidateUUID) {
        UUID uuid = null;
        try {
            uuid = UUID.fromString(candidateUUID);
        } catch (Exception e) {
            log.info("Passed id is not a UUID {}", candidateUUID);
        }
        return Optional.ofNullable(uuid);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class ConsumerNotFoundException extends RuntimeException {
        public ConsumerNotFoundException(final String consumerId) {
            super("Could not find consumer '" + consumerId + "'");
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class APIKeyNotFoundException extends RuntimeException {
        public APIKeyNotFoundException(final String apiKeyId) {
            super("Could not find api key '" + apiKeyId + "'");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    class BadRequestException extends RuntimeException {
    }

}
