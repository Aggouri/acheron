package com.dbg.cloud.acheron.admin.routes;

import com.dbg.cloud.acheron.config.common.TechnicalException;
import com.dbg.cloud.acheron.config.common.ValidationException;
import com.dbg.cloud.acheron.config.routing.Route;
import com.dbg.cloud.acheron.config.routing.RouteService;
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

    private final RouteService routeService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<RouteTO> readRoutes() {
        try {
            final List<Route> routeList = routeService.getAllRoutes();
            return routeList.stream().map(route -> new RouteTO(route)).collect(Collectors.toList());
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{routeId}", method = RequestMethod.GET)
    public ResponseEntity<?> readRoute(final @PathVariable String routeId) {
        if (routeId == null) {
            throw new RouteNotFoundException(routeId);
        }

        try {
            final Optional<Route> optionalRoute = routeService.getRoute(routeId);
            return ResponseEntity.ok(new RouteTO(optionalRoute.orElseThrow(() -> new RouteNotFoundException(routeId))));
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addRoute(final @JsonView(View.Create.class) @RequestBody RouteTO route) {
        if (!validateRoute(route)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(routeService.addNewRoute(new Route.ForCreation(
                            route.getId(),
                            route.getHttpMethods(),
                            route.getPath(),
                            route.getServiceId(),
                            route.getUrl(),
                            route.isKeepPrefix(),
                            route.isRetryable(),
                            route.isOverrideSensitiveHeaders(),
                            route.getSensitiveHeaders())));
        } catch (final ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    @RequestMapping(value = "/{routeId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteRoute(final @PathVariable String routeId) {
        if (routeId == null) {
            throw new RouteNotFoundException(routeId);
        }

        try {
            routeService.deleteRoute(routeId);

            return ResponseEntity.noContent().build();
        } catch (final TechnicalException e) {
            throw new InternalServerError();
        }
    }

    private boolean validateRoute(final RouteTO route) {
        return true;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(final String routeId) {
            super("Could not find route '" + routeId + "'");
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    class InternalServerError extends RuntimeException {
    }
}
