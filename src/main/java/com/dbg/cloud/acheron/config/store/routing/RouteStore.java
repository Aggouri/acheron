package com.dbg.cloud.acheron.config.store.routing;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RouteStore {

    List<Route> findAll();

    Optional<Route> findById(@NonNull String routeId);

    Set<String> findHttpMethodsByRouteId(@NonNull String routeId);

    Route add(@NonNull Route route);

    void deleteById(@NonNull String routeId);
}
