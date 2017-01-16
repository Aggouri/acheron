package com.dbg.cloud.acheron.plugins.oauth2.rest;

import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.config.store.consumers.ConsumerStore;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2ServerProvider;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.OAuth2AuthorisationServer;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationResult;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionResult;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Client;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Store;
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
final class OAuth2ClientController {

    private final OAuth2Store oAuth2Store;
    private final ConsumerStore consumerStore;
    private final OAuth2ServerProvider oAuth2ServerProvider;

    @RequestMapping(value = "/consumers/{consumerId}/oauth2-clients", method = RequestMethod.GET)
    @JsonView(value = View.Read.class)
    public List<OAuth2ClientTO> readOAuth2ClientsOfConsumer(final @PathVariable String consumerId) {
        final UUID uuidConsumerId = parseUUID(consumerId).orElseThrow(() -> new BadRequestException());
        final List<OAuth2Client> clientList = oAuth2Store.findByConsumer(uuidConsumerId);
        return clientList.stream().map(client -> new OAuth2ClientTO(client)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/consumers/{consumerId}/oauth2-clients/{id}", method = RequestMethod.GET)
    @JsonView(value = View.Read.class)
    public ResponseEntity<?> readOAuth2ClientOfConsumer(final @PathVariable String consumerId,
                                                        final @PathVariable String id) {
        final UUID uuidConsumerId = parseUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        final UUID uuid = parseUUID(id).orElseThrow(() -> new OAuth2ClientNotFoundException(id));
        final Optional<OAuth2Client> optionalClient = oAuth2Store.findById(uuid);

        final OAuth2Client client = optionalClient.orElseThrow(() -> new OAuth2ClientNotFoundException(id));
        if (!uuidConsumerId.equals(client.getConsumerId())) {
            throw new ConsumerNotFoundException(consumerId);
        }
        return ResponseEntity.ok(new OAuth2ClientTO(client));
    }

    @RequestMapping(value = "/consumers/{consumerId}/oauth2-clients", method = RequestMethod.POST)
    @JsonView(value = View.ReadRegister.class)
    public ResponseEntity<?> addOAuth2ClientToConsumer(final @PathVariable String consumerId,
                                                       final @JsonView(View.Register.class) @RequestBody
                                                               OAuth2ClientTO oauth2Client) {
        if (!validateRegistration(oauth2Client)) {
            return ResponseEntity.badRequest().build();
        }

        final UUID uuidConsumerId = parseUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        final Consumer consumer = consumerStore.findById(uuidConsumerId).orElseThrow(
                () -> new ConsumerNotFoundException(consumerId));

        // Create in Authorisation Server
        // TODO Change hardcoded realm when the concept of realms is clear
        final OAuth2AuthorisationServer oauth2Server = oAuth2ServerProvider.authorisationServerOfRealm("realm1");
        Optional<ClientCreationResult> optionalResult = Optional.empty();
        try {
            optionalResult = Optional.ofNullable(
                    oauth2Server.clientCreationSpec(oauth2Server.authenticationSpec().operation())
                            .operationForClient(oauth2Client).result());
        } catch (ClientCreationOperation.TechnicalException e) {
            log.error("Error occurred while trying to create client", e);
        }

        final ResponseEntity<?> response;
        if (optionalResult.isPresent()) {
            final ClientCreationResult result = optionalResult.get();
            if (result.clientId() != null && !result.clientId().trim().isEmpty() &&
                    result.clientSecret() != null && !result.clientSecret().trim().isEmpty()) {

                // Store Client in DB
                final OAuth2Client client = oAuth2Store.add(new OAuth2Client.ForCreation(result.clientId(),
                        consumer.getId(), consumer.getName(), consumer.getCreatedAt()));

                // FIXME Handle storage failure, i.e. call auth server to delete client
                response = ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(new OAuth2ClientTO(client, result.clientSecret()));
            } else {
                response = ResponseEntity.badRequest().build();
            }
        } else {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }

    @RequestMapping(value = "/consumers/{consumerId}/oauth2-clients/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteOAuth2ClientOfConsumer(final @PathVariable String consumerId,
                                                          final @PathVariable String id) {
        final ResponseEntity<?> response;
        final UUID uuidConsumerId = parseUUID(consumerId).orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        final UUID uuid = parseUUID(id).orElseThrow(() -> new OAuth2ClientNotFoundException(id));

        // Read oauth2 client from DB to get client id
        final OAuth2Client oAuth2Client = oAuth2Store.findById(uuid).orElseThrow(
                () -> new OAuth2ClientNotFoundException(id));

        if (uuidConsumerId.equals(oAuth2Client.getConsumerId())) {
            response = delete(oAuth2Client);
        } else {
            throw new OAuth2ClientNotFoundException(id);
        }

        return response;
    }

    @RequestMapping(value = "/plugins/oauth2/clients/{id}", method = RequestMethod.GET)
    @JsonView(value = View.Read.class)
    public ResponseEntity<?> readOAuth2Client(final @PathVariable String id) {
        final UUID uuid = parseUUID(id).orElseThrow(() -> new OAuth2ClientNotFoundException(id));
        final Optional<OAuth2Client> optionalClient = oAuth2Store.findById(uuid);

        return ResponseEntity.ok(new OAuth2ClientTO(
                optionalClient.orElseThrow(() -> new OAuth2ClientNotFoundException(id))));
    }

    @RequestMapping(value = "/plugins/oauth2/clients/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteOAuth2Client(final @PathVariable String id) {
        final UUID uuid = parseUUID(id).orElseThrow(() -> new OAuth2ClientNotFoundException(id));

        // Read oauth2 client from DB to get client id
        final OAuth2Client oAuth2Client = oAuth2Store.findById(uuid).orElseThrow(
                () -> new OAuth2ClientNotFoundException(id));

        return delete(oAuth2Client);
    }

    private ResponseEntity<?> delete(final OAuth2Client oAuth2Client) {
        final ResponseEntity<?> response;

        // Delete in Authorisation Server
        // TODO Change hardcoded realm when the concept of realms is clear
        final OAuth2AuthorisationServer oauth2Server = oAuth2ServerProvider.authorisationServerOfRealm("realm1");
        Optional<ClientDeletionResult> optionalResult = Optional.empty();
        try {
            optionalResult = Optional.ofNullable(
                    oauth2Server.clientDeletionSpec(oauth2Server.authenticationSpec().operation())
                            .operationForClient(oAuth2Client.getClientId())
                            .result());
        } catch (ClientDeletionOperation.TechnicalException e) {
            log.error("Error occurred while trying to delete client", e);
        }

        if (optionalResult.isPresent()) {
            final ClientDeletionResult result = optionalResult.get();
            if (result.isOk()) {
                // Delete in DB
                oAuth2Store.deleteById(oAuth2Client.getId());

                // FIXME Handle storage failure, i.e. call auth server to delete client
                response = ResponseEntity.noContent().build();
            } else {
                response = ResponseEntity.badRequest().build();
            }
        } else {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }

    private boolean validateRegistration(final OAuth2ClientTO oauth2Client) {
        // This will be revisited when it makes sense
        return true;
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
    class OAuth2ClientNotFoundException extends RuntimeException {
        public OAuth2ClientNotFoundException(final String clientId) {
            super("Could not find oauth2 client '" + clientId + "'");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    class BadRequestException extends RuntimeException {
    }
}
