package com.dbg.cloud.acheron.plugins.oauth2;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.CredentialsStruct;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.OAuth2AuthorisationServer;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.Hydra;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public interface OAuth2ServerProvider {

    OAuth2AuthorisationServer authorisationServerOfRealm(String realmId);

    @AllArgsConstructor
    @Slf4j
    final class Default implements OAuth2ServerProvider {

        private final String clientId;
        private final String clientSecret;
        private final RouteLocator routeLocator;
        private final RestTemplateBuilder restTemplateBuilder;

        private final ConcurrentHashMap<String, OAuth2AuthorisationServer> cachedServersPerRealm =
                new ConcurrentHashMap<>();

        @Override
        public OAuth2AuthorisationServer authorisationServerOfRealm(final String realmId) {
            final OAuth2AuthorisationServer server;
            if (!cachedServersPerRealm.containsKey(realmId)) {
                final String authServerRootURL = getAuthServerRootURL(realmId);

                final CredentialsStruct credentials = new CredentialsStruct(clientId, clientSecret, Optional.empty());
                server = new OAuth2AuthorisationServer.ServerWithCachedAuthentication(
                        new Hydra(authServerRootURL, credentials, restTemplateBuilder));
                cachedServersPerRealm.put(realmId, server);
            } else {
                server = cachedServersPerRealm.get(realmId);
            }

            return server;
        }

        private String getAuthServerRootURL(final String realmId) {
            String authServerRootURL = null;
            final String authServerRoute = "hydra_" + realmId;

            // This is super inefficient but ok for now
            // TODO Get auth server URL properly
            final Optional<Route> potentialRoute = routeLocator.getRoutes().stream().filter(
                    route -> (authServerRoute).equals(route.getId()))
                    .findAny();

            if (potentialRoute.isPresent()) {
                authServerRootURL = potentialRoute.get().getLocation();
                log.info("Location of hydra is {}", authServerRootURL);
            } else {
                log.error("Could not find location of hydra (looking at route with id: {}", authServerRoute);
            }

            return authServerRootURL;
        }
    }
}
