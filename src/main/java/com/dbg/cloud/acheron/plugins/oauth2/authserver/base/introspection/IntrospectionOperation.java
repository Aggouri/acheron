package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationOperation;
import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.authentication.AuthenticationResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public interface IntrospectionOperation {

    IntrospectionResult result() throws TechnicalException;

    final class TechnicalException extends Exception {
    }

    @AllArgsConstructor
    @Slf4j
    final class Simple implements IntrospectionOperation {

        private final String introspectionEndpointURL;
        private final String ownAccessToken;
        private final String introspectedAccessToken;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public IntrospectionResult result() throws TechnicalException {
            final IntrospectionResult result;
            final RestTemplate restTemplate = buildRestTemplate();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + ownAccessToken);

            final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("token", introspectedAccessToken);

            final HttpEntity<MultiValueMap<String, String>> introspectionRequestHttpEntity =
                    new HttpEntity<>(map, headers);

            try {
                final JSONResponse jsonResponse =
                        restTemplate.postForObject(introspectionEndpointURL,
                                introspectionRequestHttpEntity,
                                JSONResponse.class);
                result = new IntrospectionResult(jsonResponse); // NPE is possible, caught below
            } catch (Exception e) {
                log.error("Could not introspect access token via authorisation server", e);
                throw new TechnicalException();
            }

            return result;
        }

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

    @AllArgsConstructor
    @Slf4j
    final class IntrospectionOperationWithAuthentication implements IntrospectionOperation {
        private final String introspectionEndpointURL;
        private final AuthenticationOperation authenticationOperation;
        private final String introspectedAccessToken;
        private final RestTemplateBuilder restTemplateBuilder;

        @Override
        public IntrospectionResult result() throws TechnicalException {
            // Authenticate
            final AuthenticationResult authResult;
            try {
                authResult = authenticationOperation.result();
            } catch (AuthenticationOperation.TechnicalException e) {
                throw new TechnicalException();
            }

            // Fail if the authentication response does not have an access token
            if (!authResult.accessToken().isPresent()) {
                log.error("Could not obtain access token from authorisation server");
                throw new TechnicalException();
            } else {
                return new Simple(introspectionEndpointURL, authResult.accessToken().get().token(),
                        introspectedAccessToken, restTemplateBuilder).result();
            }
        }
    }
}
