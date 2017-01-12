package com.dbg.cloud.acheron.admin.routes;

import com.dbg.cloud.acheron.config.store.routing.Route;
import com.dbg.cloud.acheron.config.store.routing.RouteStore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/routes")
@AllArgsConstructor
@Slf4j
final class RoutesController {

    private final RouteStore routeStore;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<RouteTO> readRoutes() {
        final List<Route> routeList = routeStore.findAll();
        return routeList.stream().map(route -> new RouteTO(route)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/{routeId}", method = RequestMethod.GET)
    public ResponseEntity<?> readRoute(final @PathVariable String routeId) {
        if (routeId == null) {
            throw new RouteNotFoundException(routeId);
        }

        final Optional<Route> optionalRoute = routeStore.findById(routeId);
        return ResponseEntity.ok(new RouteTO(optionalRoute.orElseThrow(() -> new RouteNotFoundException(routeId))));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addRoute(final @JsonView(View.Create.class) @RequestBody RouteTO route) {
        if (!validateRoute(route)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(routeStore.add(new Route.ForCreation(
                        route.getId(),
                        route.getHttpMethods(),
                        route.getPath(),
                        route.getServiceId(),
                        route.getUrl(),
                        route.isKeepPrefix(),
                        route.isRetryable(),
                        route.isOverrideSensitiveHeaders(),
                        route.getSensitiveHeaders())));
    }

    @RequestMapping(value = "/{routeId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteRoute(final @PathVariable String routeId) {
        if (routeId == null) {
            throw new RouteNotFoundException(routeId);
        }
        routeStore.deleteById(routeId);

        return ResponseEntity.noContent().build();
    }

    private boolean validateRoute(final RouteTO route) {
        return route.getId() != null && !route.getId().isEmpty() &&
                // either service id or url must be given
                ((route.getServiceId() != null && !route.getServiceId().isEmpty()) ||
                        (route.getUrl() != null && !route.getUrl().isEmpty())
                ) &&
                route.getPath() != null && !route.getPath().isEmpty();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(final String routeId) {
            super("Could not find route '" + routeId + "'");
        }
    }
}
