package com.dbg.cloud.acheron.consumers.endpoints;

import com.dbg.cloud.acheron.adminendpoints.AdminEndpoint;
import com.dbg.cloud.acheron.exception.TechnicalException;
import com.dbg.cloud.acheron.exception.ValidationException;
import com.dbg.cloud.acheron.consumers.Consumer;
import com.dbg.cloud.acheron.consumers.service.ConsumerService;
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
final class ConsumersController implements AdminEndpoint {

    private final ConsumerService consumerService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<ConsumerTO> readConsumers() {
        try {
            final List<Consumer> consumerList = consumerService.getAllConsumers();
            return consumerList.stream().map(consumer -> new ConsumerTO(consumer)).collect(Collectors.toList());
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{consumerId}", method = RequestMethod.GET)
    public ResponseEntity<?> readConsumer(final @PathVariable String consumerId) {
        try {
            final UUID uuidConsumerId =
                    parseConsumerUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
            final Optional<Consumer> optionalConsumer = consumerService.getConsumer(uuidConsumerId);

            return ResponseEntity.ok(new ConsumerTO(
                    optionalConsumer.orElseThrow(() -> new ConsumerNotFoundException(consumerId))));
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addConsumer(final @JsonView(View.Create.class) @RequestBody ConsumerTO consumer) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(consumerService.addNewConsumer(new Consumer.ForCreation(consumer.getName())));
        } catch (final ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{consumerId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteConsumer(final @PathVariable String consumerId) {
        try {
            final UUID uuidConsumerId =
                    parseConsumerUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
            consumerService.deleteConsumer(uuidConsumerId);

            return ResponseEntity.noContent().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
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

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    class InternalServerError extends RuntimeException {
    }
}
