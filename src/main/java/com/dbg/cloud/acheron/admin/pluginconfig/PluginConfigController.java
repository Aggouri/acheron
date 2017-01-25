package com.dbg.cloud.acheron.admin.pluginconfig;

import com.dbg.cloud.acheron.config.common.TechnicalException;
import com.dbg.cloud.acheron.config.common.ValidationException;
import com.dbg.cloud.acheron.config.plugins.PluginConfig;
import com.dbg.cloud.acheron.config.plugins.PluginConfigService;
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
@RequestMapping("/admin/plugin-configs")
@AllArgsConstructor
@Slf4j
final class PluginConfigController {

    private final PluginConfigService pluginConfigService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<PluginConfigTO> readPlugins() {
        try {
            final List<PluginConfig> pluginConfigList = pluginConfigService.getAllPluginConfigs();
            return pluginConfigList.stream()
                    .map(pluginConfig -> new PluginConfigTO(pluginConfig))
                    .collect(Collectors.toList());
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.GET)
    public ResponseEntity<?> readPlugin(final @PathVariable String pluginConfigId) {
        try {
            final UUID uuidPluginConfigId =
                    parseUUID(pluginConfigId).orElseThrow(() -> new PluginConfigNotFoundException(pluginConfigId));
            final Optional<PluginConfig> optionalPlugin = pluginConfigService.getPluginConfig(uuidPluginConfigId);

            return ResponseEntity.ok(new PluginConfigTO(
                    optionalPlugin.orElseThrow(() -> new PluginConfigNotFoundException(pluginConfigId))));
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addPluginConfig(
            final @JsonView(View.Create.class) @RequestBody PluginConfigTO pluginConfig) {
        if (!validatePluginConfigForCreate(pluginConfig)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new PluginConfigTO(
                            pluginConfigService.addNewPluginConfig(new PluginConfig.ForCreation(
                                    pluginConfig.getName(),
                                    pluginConfig.getRouteId(),
                                    pluginConfig.getHttpMethods(),
                                    pluginConfig.getConsumerId() != null ?
                                            UUID.fromString(pluginConfig.getConsumerId()) : null,
                                    pluginConfig.safeGetConfig(),
                                    pluginConfig.getEnabled() != null ? pluginConfig.getEnabled() : true))));
        } catch (final ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> mergePluginConfig(final @PathVariable String pluginConfigId,
                                               final @JsonView(View.Merge.class) @RequestBody PluginConfigTO
                                                       pluginConfig) {
        try {
            // 404 if the plugin config does not exist
            final UUID uuid =
                    parseUUID(pluginConfigId).orElseThrow(() -> new PluginConfigNotFoundException(pluginConfigId));
            pluginConfigService.getPluginConfig(uuid).orElseThrow(
                    () -> new PluginConfigNotFoundException(pluginConfig.getId()));

            // Validate
            if (!validatePluginConfigForMerge(pluginConfig, pluginConfigId)) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(
                    new PluginConfigTO(
                            pluginConfigService.mergePluginConfig(
                                    toPluginConfig(pluginConfig))));
        } catch (final ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.PUT)
    public ResponseEntity<?> replacePluginConfig(final @PathVariable String pluginConfigId,
                                                 final @JsonView(View.Replace.class) @RequestBody PluginConfigTO
                                                         pluginConfig) {
        try {
            // 404 if the plugin config does not exist
            final UUID uuid = parseUUID(pluginConfigId).get();
            pluginConfigService.getPluginConfig(uuid).orElseThrow(
                    () -> new PluginConfigNotFoundException(pluginConfig.getId()));

            // Validate
            if (!validatePluginConfigForReplace(pluginConfig, pluginConfigId)) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(
                    new PluginConfigTO(
                            pluginConfigService.replacePluginConfig(
                                    toPluginConfig(pluginConfig))));
        } catch (final ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteConsumer(final @PathVariable String pluginConfigId) {
        try {
            // 404 if the plugin config does not exist
            final UUID uuidPluginConfigId =
                    parseUUID(pluginConfigId).orElseThrow(() -> new PluginConfigNotFoundException(pluginConfigId));

            pluginConfigService.deletePluginConfig(uuidPluginConfigId);

            return ResponseEntity.noContent().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    private boolean validatePluginConfigForCreate(final PluginConfigTO pluginConfig) {
        // consumer id is either null or a UUID
        return pluginConfig.getConsumerId() == null || parseUUID(pluginConfig.getConsumerId()).isPresent();
    }

    private boolean validatePluginConfigForReplace(final PluginConfigTO pluginConfig, final String id) {
        return parseUUID(id).isPresent() && // id is a UUID (and not null)
                // object id is either not present OR equal to id
                (pluginConfig.getId() == null || id.equals(pluginConfig.getId())) &&
                // whatever validation for create says
                validatePluginConfigForCreate(pluginConfig);
    }

    private boolean validatePluginConfigForMerge(final PluginConfigTO pluginConfig, final String id) {
        return parseUUID(id).isPresent() && // id is a UUID (and not null)
                // object id is either not present OR equal to id
                (pluginConfig.getId() == null || id.equals(pluginConfig.getId())) &&
                // if consumer id is present, it's a UUID
                (pluginConfig.getConsumerId() == null || parseUUID(pluginConfig.getConsumerId()).isPresent());
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

    private PluginConfig toPluginConfig(final PluginConfigTO pluginConfigTO) {
        return new PluginConfig.Default(
                UUID.fromString(pluginConfigTO.getId()),
                pluginConfigTO.getName(),
                pluginConfigTO.getRouteId(),
                pluginConfigTO.getHttpMethods(),
                pluginConfigTO.getConsumerId() != null ? // being null is a legitimate case
                        UUID.fromString(pluginConfigTO.getConsumerId()) : null,
                pluginConfigTO.safeGetConfig(),
                pluginConfigTO.getEnabled() != null ? pluginConfigTO.getEnabled() : true,
                pluginConfigTO.getCreatedAt()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class PluginConfigNotFoundException extends RuntimeException {
        public PluginConfigNotFoundException(final String pluginConfig) {
            super("Could not find plugin config '" + pluginConfig + "'");
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    class InternalServerError extends RuntimeException {
    }
}
