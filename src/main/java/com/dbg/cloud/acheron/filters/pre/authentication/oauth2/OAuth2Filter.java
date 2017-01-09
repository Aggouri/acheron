package com.dbg.cloud.acheron.filters.pre.authentication.oauth2;

import com.dbg.cloud.acheron.AcheronHeaders;
import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.AccessToken;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.CredentialsStruct;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.OAuth2AuthorisationServer;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.authentication.AuthenticationResult;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection.IntrospectionOperation;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.compliant.introspection.IntrospectionResult;
import com.dbg.cloud.acheron.filters.pre.authentication.oauth2.hydra.Hydra;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2Store;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public final class OAuth2Filter extends PreFilter {

    private final RouteLocator routeLocator;
    private final OAuth2Store oAuth2Store;
    private final String clientId;
    private final String clientSecret;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;


    public OAuth2Filter(final RouteLocator routeLocator, final OAuth2Store oAuth2Store, String clientId,
                        String clientSecret) {
        this.routeLocator = routeLocator;
        this.oAuth2Store = oAuth2Store;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public int filterOrder() {
        return 35;
    }

    @Override
    public boolean shouldFilter() {
        return isEnabled("plugins.oauth2.enabled");
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();

        try {
            /***************************************
             * Introspect token of incoming request
             ***************************************/
            final IntrospectionResult tokenInfo = introspectIncomingToken(context);
            log.info("Introspection response is: {}", tokenInfo);

            /***************
             * Verify token
             ***************/
            // Is it active? (will be false if the token is rubbish or expired)
            if (!Boolean.TRUE.equals(tokenInfo.active())) {
                throwInvalidAccessToken();
            }

            // FIXME Should we not check the aud claim?

            // Is it issued for the required scopes?
            if (tokenInfo.scope() == null || "".equals(tokenInfo.scope().trim())) {
                log.info("Scope is null or empty");
                throwInvalidAccessToken();
            }

            final String routeId = (String) context.get(AcheronRequestContextKeys.ROUTE_ID);
            final String[] scopes = tokenInfo.scope().split(" ");
            if (Arrays.stream(scopes).noneMatch(s -> routeId.equals(s))) {
                log.info("Access token does not have required scope [required = {}, actual = {}]", routeId,
                        tokenInfo.scope());
                throwForbidden();
            }

            if (tokenInfo.clientId() == null) {
                log.error("Client ID is null. Not good.");
                throwInternalServerError();
            }

            /*****************
             * Inject headers
             *****************/
            addRequestHeader(AcheronHeaders.OAUTH2_SUBJECT, tokenInfo.sub());
            addRequestHeader(AcheronHeaders.OAUTH2_CLIENT_ID, tokenInfo.clientId());

            // TODO Decide whether to keep Authorization header
            removeRequestHeader("Authorization");

            /*********************
             * Add info to context
             *********************/
            // Set Consumer ID
            final Optional<Consumer> optionalConsumer = oAuth2Store.findConsumerByClientId(tokenInfo.clientId());
            if (optionalConsumer.isPresent() && optionalConsumer.get().getId() != null) {
                final Consumer consumer = optionalConsumer.get();
                log.info("Determined caller is consumer with name = {} and id = {}", consumer.getName(),
                        consumer.getId());
                setUniqueConsumerIdOrFail(optionalConsumer.get().getId().toString());
            } else {
                log.error("Client ID does not correspond to any consumer. Not good.");
                throwInternalServerError();
            }
        } catch (AccessToken.BearerToken.BearerTokenException e) {
            // throw when there is no bearer token in the request
            throwInvalidAccessToken();
        } catch (IntrospectionOperation.TechnicalException | AuthenticationOperation.TechnicalException e) {
            throwInternalServerError();
        }

        return null;
    }

    private IntrospectionResult introspectIncomingToken(final RequestContext context) throws
            AccessToken.BearerToken.BearerTokenException, IntrospectionOperation.TechnicalException,
            AuthenticationOperation.TechnicalException {

        Optional<String> currentBearerToken = Optional.empty(); // TODO Get from cache later
        final AccessToken token = new AccessToken.BearerToken(context.getRequest());

        final String realm_id = (String) context.get(AcheronRequestContextKeys.REALM_ID);
        final String hydraRootURL = getAuthServerRootURL(realm_id);

        final CredentialsStruct credentials = new CredentialsStruct(clientId, clientSecret, currentBearerToken);
        final OAuth2AuthorisationServer authServer = new Hydra(hydraRootURL, credentials, restTemplateBuilder);

        AuthenticationResult authResult = authServer.authenticationSpec().operation().result();
        currentBearerToken = authResult.accessToken(); // TODO Update some cache later

        return token.introspection(authServer.introspectionSpec(authResult));
    }

    private void throwInvalidAccessToken() {
        throwFailure(401, "{ \"error\": \"Invalid access token\" }");
    }

    private void throwForbidden() {
        throwFailure(403, "{ \"error\": \"Invalid access token\" }");
    }

    private String getAuthServerRootURL(final String realm_id) {
        String hydraRootURL = null;
        final String hydraRouteId = "hydra_" + realm_id;

        // Get hydra's URL
        // This is super inefficient but ok for now
        // TODO Get hydra URL properly
        final Optional<Route> potentialRoute = routeLocator.getRoutes().stream().filter(
                route -> (hydraRouteId).equals(route.getId()))
                .findAny();

        if (potentialRoute.isPresent()) {
            hydraRootURL = potentialRoute.get().getLocation();
            log.info("Location of hydra is {}", hydraRootURL);
        } else {
            log.error("Could not find location of hydra (looking at route with id: {}", hydraRouteId);
        }

        return hydraRootURL;
    }
}
