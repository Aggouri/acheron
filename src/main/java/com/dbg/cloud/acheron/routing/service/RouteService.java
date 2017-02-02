package com.dbg.cloud.acheron.routing.service;

import com.dbg.cloud.acheron.cluster.ClusterEventBus;
import com.dbg.cloud.acheron.exception.TechnicalException;
import com.dbg.cloud.acheron.exception.ValidationException;
import com.dbg.cloud.acheron.pluginconfig.service.PluginConfigService;
import com.dbg.cloud.acheron.routing.Route;
import com.dbg.cloud.acheron.routing.store.RouteStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RouteService {

    List<Route> getAllRoutes() throws TechnicalException;

    Optional<Route> getRoute(@NonNull String routeId) throws TechnicalException;

    Route addNewRoute(@NonNull Route route) throws ValidationException, TechnicalException;

    void deleteRoute(@NonNull String routeId) throws TechnicalException;

    Set<String> getHttpMethodsOfRoute(@NonNull String routeId);

    @Service
    @AllArgsConstructor
    class RouteServiceImpl implements RouteService {

        private final RouteStore routeStore;
        private final PluginConfigService pluginConfigService;
        private final ClusterEventBus bus;

        @Override
        public List<Route> getAllRoutes() throws TechnicalException {
            try {
                return routeStore.findAll();
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public Optional<Route> getRoute(final @NonNull String routeId) throws TechnicalException {
            try {
                return routeStore.findById(routeId);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public Route addNewRoute(final @NonNull Route route) throws ValidationException, TechnicalException {
            validateRoute(route);

            try {
                final Date createdAt = new Date();

                return routeStore.add(new Route.Smart(
                        route.getId(),
                        route.getHttpMethods(),
                        route.getPath(),
                        route.getServiceId(),
                        route.getUrl(),
                        route.isKeepPrefix(),
                        route.isRetryable(),
                        route.isOverrideSensitiveHeaders(),
                        route.getSensitiveHeaders(),
                        createdAt));
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public void deleteRoute(final @NonNull String routeId) throws TechnicalException {
            try {
                routeStore.deleteById(routeId);

                refreshRoutes();

                // FIXME: This is not sustainable. Come up with an event-based model (event bus, publish/subscribe,
                // whatever)
                pluginConfigService.getPluginConfigsOfRoute(routeId).stream().forEach(
                        pluginConfig -> pluginConfigService.deletePluginConfig(pluginConfig.getId()));

            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        @Override
        public Set<String> getHttpMethodsOfRoute(@NonNull String routeId) {
            try {
                return routeStore.findHttpMethodsByRouteId(routeId);
            } catch (final RuntimeException e) {
                throw new TechnicalException(e);
            }
        }

        private void validateRoute(final Route route) throws ValidationException {
            if (route.getId() == null || route.getId().isEmpty()) {
                throw new ValidationException("route id must not be null");
            }

            if ((route.getServiceId() == null || route.getServiceId().isEmpty()) &&
                    (route.getUrl() == null || route.getUrl().isEmpty())) {
                throw new ValidationException("either service id or url must be given");
            }

            if (route.getPath() == null || route.getPath().isEmpty()) {
                throw new ValidationException("path must be given");
            }
        }

        private void refreshRoutes() {
            bus.refreshRoutes();
        }
    }
}
