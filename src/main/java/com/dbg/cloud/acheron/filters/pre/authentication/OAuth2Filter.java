package com.dbg.cloud.acheron.filters.pre.authentication;

import com.dbg.cloud.acheron.AcheronHeaders;
import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.config.consumers.Consumer;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.dbg.cloud.acheron.plugins.oauth2.OAuth2ServerProvider;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.AccessToken;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.OAuth2AuthorisationServer;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection.IntrospectionOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection.IntrospectionResult;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Store;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
public final class OAuth2Filter extends PreFilter {

    private final OAuth2ServerProvider oAuth2ServerProvider;
    private final OAuth2Store oAuth2Store;

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

        final AccessToken token = new AccessToken.BearerToken(context.getRequest());

        final String realmId = (String) context.get(AcheronRequestContextKeys.REALM_ID);
        final OAuth2AuthorisationServer authServer = oAuth2ServerProvider.authorisationServerOfRealm(realmId);

        return token.introspection(authServer.introspectionSpec(authServer.authenticationSpec().operation()));
    }

    private void throwInvalidAccessToken() {
        throwFailure(401, "{ \"error\": \"Invalid access token\" }");
    }

    private void throwForbidden() {
        throwFailure(403, "{ \"error\": \"Invalid access token\" }");
    }
}
