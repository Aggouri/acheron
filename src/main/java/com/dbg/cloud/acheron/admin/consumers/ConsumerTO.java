package com.dbg.cloud.acheron.admin.consumers;

import com.dbg.cloud.acheron.config.consumers.Consumer;
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
final class ConsumerTO {

    public ConsumerTO(final @NonNull Consumer consumer) {
        this(consumer.getId() != null ? consumer.getId().toString() : null,
                consumer.getName(), consumer.getCreatedAt());
    }

    @JsonProperty("consumer_id")
    private final String consumerId;

    @JsonView(View.Create.class)
    private final String name;

    @JsonProperty("created_at")
    private final Date createdAt;
}
