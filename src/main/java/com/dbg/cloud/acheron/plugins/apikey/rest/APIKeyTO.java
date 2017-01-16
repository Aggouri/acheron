package com.dbg.cloud.acheron.plugins.apikey.rest;

import com.dbg.cloud.acheron.plugins.apikey.store.APIKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Date;

@Getter
@AllArgsConstructor
@ToString
final class APIKeyTO {

    public APIKeyTO(final @NonNull APIKey apiKey) {
        this(apiKey.getId() != null ? apiKey.getId().toString() : null,
                apiKey.getAPIKey(),
                apiKey.getConsumerId() != null ? apiKey.getConsumerId().toString() : null,
                apiKey.getConsumerName(),
                apiKey.getConsumerCreatedAt(),
                apiKey.getCreatedAt());
    }

    private final String id;

    @JsonProperty("api_key")
    @JsonView(View.Register.class)
    private final String apiKey;

    @JsonProperty("consumer_id")
    private final String consumerId;

    @JsonProperty("consumer_name")
    private final String consumerName;

    @JsonProperty("consumer_created_at")
    private final Date consumerCreatedAt;

    @JsonProperty("created_at")
    private final Date createdAt;
}
