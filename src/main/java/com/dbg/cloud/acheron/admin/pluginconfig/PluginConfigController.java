package com.dbg.cloud.acheron.admin.pluginconfig;

import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/plugin-configs")
@AllArgsConstructor
@Slf4j
final class PluginConfigController {

    private static final Collection<String> SUPPORTED_PLUGINS = Arrays.asList("oauth2", "api_key", "correlation_id",
            "rate_limiting");
    private final PluginConfigStore pluginConfigStore;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<PluginConfigTO> readPlugins() {
        final List<PluginConfig> pluginConfigList = pluginConfigStore.findAll();
        return pluginConfigList.stream()
                .map(pluginConfig -> new PluginConfigTO(pluginConfig))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.GET)
    public ResponseEntity<?> readPlugin(final @PathVariable String pluginConfigId) {
        final UUID uuidPluginConfigId =
                parseUUID(pluginConfigId).orElseThrow(() -> new PluginConfigNotFoundException(pluginConfigId));
        final Optional<PluginConfig> optionalPlugin = pluginConfigStore.findById(uuidPluginConfigId);

        return ResponseEntity.ok(new PluginConfigTO(
                optionalPlugin.orElseThrow(() -> new PluginConfigNotFoundException(pluginConfigId))));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addPluginConfig(
            final @JsonView(View.Create.class) @RequestBody PluginConfigTO pluginConfig) {
        if (!validatePluginConfigForCreate(pluginConfig)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PluginConfigTO(
                        pluginConfigStore.add(new PluginConfig.ForCreation(
                                pluginConfig.getName(),
                                pluginConfig.getRouteId(),
                                pluginConfig.getHttpMethods(),
                                pluginConfig.getConsumerId() != null ?
                                        UUID.fromString(pluginConfig.getConsumerId()) : null,
                                pluginConfig.safeGetConfig(),
                                pluginConfig.getEnabled() != null ? pluginConfig.getEnabled() : true))));
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> mergePluginConfig(final @PathVariable String pluginConfigId,
                                               final @JsonView(View.Merge.class) @RequestBody PluginConfigTO
                                                       pluginConfig) {
        if (!validatePluginConfigForMerge(pluginConfig, pluginConfigId)) {
            return ResponseEntity.badRequest().build();
        }

        final UUID uuid = parseUUID(pluginConfigId).get(); // safe due to validation above
        final PluginConfig existingPluginConfig = pluginConfigStore.findById(uuid)
                .orElseThrow(() -> new PluginConfigNotFoundException(pluginConfig.getId()));

        final PluginConfig mergedPluginConfig = new PluginConfig.WithoutCreatedDate(
                new PluginConfig.Merge(
                        existingPluginConfig,
                        new PluginConfig.Default(
                                uuid,
                                pluginConfig.getName(),
                                pluginConfig.getRouteId(),
                                pluginConfig.getHttpMethods(),
                                pluginConfig.getConsumerId() != null ?
                                        UUID.fromString(pluginConfig.getConsumerId()) : null,
                                pluginConfig.safeGetConfig(),
                                pluginConfig.getEnabled() != null ?
                                        pluginConfig.getEnabled() : true,
                                pluginConfig.getCreatedAt()
                        )));

        // no validation on coherence; perhaps we should do it
        return ResponseEntity.ok(new PluginConfigTO(pluginConfigStore.update(mergedPluginConfig)));
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.PUT)
    public ResponseEntity<?> replacePluginConfig(final @PathVariable String pluginConfigId,
                                                 final @JsonView(View.Replace.class) @RequestBody PluginConfigTO
                                                         pluginConfig) {
        if (!validatePluginConfigForReplace(pluginConfig, pluginConfigId)) {
            return ResponseEntity.badRequest().build();
        }

        final UUID uuid = parseUUID(pluginConfigId).get(); // safe due to validation above
        pluginConfigStore.findById(uuid)
                .orElseThrow(() -> new PluginConfigNotFoundException(pluginConfig.getId()));

        return ResponseEntity
                .ok(new PluginConfigTO(
                        pluginConfigStore.update(
                                new PluginConfig.WithoutCreatedDate(
                                        new PluginConfig.Default(
                                                uuid,
                                                pluginConfig.getName(),
                                                pluginConfig.getRouteId(),
                                                pluginConfig.getHttpMethods(),
                                                pluginConfig.getConsumerId() != null ?
                                                        UUID.fromString(pluginConfig.getConsumerId()) : null,
                                                pluginConfig.safeGetConfig(),
                                                pluginConfig.getEnabled() != null ? pluginConfig.getEnabled() : true,
                                                pluginConfig.getCreatedAt()
                                        )))));
    }

    @RequestMapping(value = "/{pluginConfigId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteConsumer(final @PathVariable String pluginConfigId) {
        final UUID uuidPluginConfigId =
                parseUUID(pluginConfigId).orElseThrow(() -> new PluginConfigNotFoundException(pluginConfigId));
        pluginConfigStore.deleteById(uuidPluginConfigId);

        return ResponseEntity.noContent().build();
    }

    private boolean validatePluginConfigForCreate(final PluginConfigTO pluginConfig) {
        return SUPPORTED_PLUGINS.contains(pluginConfig.getName()) &&
                pluginConfig.getRouteId() != null && !pluginConfig.getRouteId().isEmpty() &&
                // consumer id is either null or a UUID
                (pluginConfig.getConsumerId() == null || parseUUID(pluginConfig.getConsumerId()).isPresent());
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
                // if present, plugin name is supported
                (pluginConfig.getName() == null || pluginConfig.getName().isEmpty() ||
                        SUPPORTED_PLUGINS.contains(pluginConfig.getName())) &&
                // object id is either not present OR equal to id
                (pluginConfig.getId() == null || id.equals(pluginConfig.getId())) &&
                // if consumer if is present, it's a UUID
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class PluginConfigNotFoundException extends RuntimeException {
        public PluginConfigNotFoundException(final String pluginConfig) {
            super("Could not find plugin config '" + pluginConfig + "'");
        }
    }
}
