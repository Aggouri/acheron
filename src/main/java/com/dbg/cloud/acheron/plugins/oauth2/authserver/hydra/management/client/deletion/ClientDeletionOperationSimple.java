package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.deletion;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.deletion.ClientDeletionResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
final class ClientDeletionOperationSimple implements ClientDeletionOperation {

    private final String clientDeletionEndpointURL;
    private final String ownAccessToken;
    private final String clientId;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public ClientDeletionResult result() throws TechnicalException {
        final ClientDeletionResult result;
        final RestTemplate restTemplate = buildRestTemplate();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ownAccessToken);

        final HttpEntity<?> deleteEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(clientDeletionEndpointURL + "/" + clientId, HttpMethod.DELETE, deleteEntity,
                    Void.class);
            result = new ClientDeletionResult.Successful();
            log.info("Hydra deleted client successfully: {}", clientId);
        } catch (final HttpClientErrorException e) {
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
                log.info("Bad request to Hydra: {}", e.getResponseBodyAsString());
                return new ClientDeletionResult.Unsuccessful();
            } else {
                log.error("Hydra rejected the client deletion request", e.getResponseBodyAsString());
                throw new TechnicalException();
            }
        } catch (final HttpServerErrorException e) {
            log.error("Client deletion request caused Hydra server error", e.getResponseBodyAsString());
            throw new TechnicalException();
        } catch (final Exception e) {
            log.error("Could not delete client in authorisation server", e);
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
