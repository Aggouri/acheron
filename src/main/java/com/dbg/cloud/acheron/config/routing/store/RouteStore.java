package com.dbg.cloud.acheron.config.routing.store;

import com.dbg.cloud.acheron.config.routing.Route;
import lombok.NonNull;

import java.util.*;

public interface RouteStore {

    List<Route> findAll();

    Optional<Route> findById(@NonNull String routeId);

    Set<String> findHttpMethodsByRouteId(@NonNull String routeId);

    Route add(@NonNull Route route);

    void deleteById(@NonNull String routeId);

    class NoRouteStore implements RouteStore {

        @Override
        public List<Route> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Route> findById(@NonNull String routeId) {
            return Optional.empty();
        }

        @Override
        public Set<String> findHttpMethodsByRouteId(@NonNull String routeId) {
            // all
            return new HashSet<>(Collections.singletonList("*"));
        }

        @Override
        public Route add(@NonNull Route route) {
            return route;
        }

        @Override
        public void deleteById(@NonNull String routeId) {
            // nothing
        }
    }
}
