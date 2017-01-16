package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.creation;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.Client;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationResult;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.HydraClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
final class ClientCreationOperationSimple implements ClientCreationOperation {

    private final String clientCreationEndpointURL;
    private final String ownAccessToken;
    private final Client client;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public ClientCreationResult result() throws TechnicalException {
        final ClientCreationResult result;
        final RestTemplate restTemplate = buildRestTemplate();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ownAccessToken);

        final HydraClient hydraClient = new HydraClient(client);
        final HttpEntity<HydraClient> hydraClientRequestHttpEntity = new HttpEntity<>(hydraClient, headers);

        try {
            final JSONResponse jsonResponse = restTemplate.postForObject(clientCreationEndpointURL,
                    hydraClientRequestHttpEntity, JSONResponse.class);
            log.info("Hydra created client successfully: {}", jsonResponse.clientId());
            result = new HydraClientCreationResult(jsonResponse); // NPE is possible, caught below
        } catch (final HttpClientErrorException e) {
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
                log.info("Bad request to Hydra: {}", e.getResponseBodyAsString());
                return new ClientCreationResult.NoResult();
            } else {
                log.error("Hydra rejected the client creation request", e);
                throw new TechnicalException();
            }
        } catch (final HttpServerErrorException e) {
            log.error("Client creation request caused Hydra server error", e.getResponseBodyAsString());
            throw new TechnicalException();
        } catch (final Exception e) {
            log.error("Could not create client in authorisation server", e);
            throw new TechnicalException();
        }

        return result;
    }

    @SuppressWarnings("Duplicates")
    private RestTemplate buildRestTemplate() {
        final MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();

        final List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        jsonMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

        return restTemplateBuilder
                .additionalMessageConverters(jsonMessageConverter)
                .additionalMessageConverters(new FormHttpMessageConverter())
                .build();
    }
}
