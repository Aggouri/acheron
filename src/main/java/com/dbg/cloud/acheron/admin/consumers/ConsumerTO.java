package com.dbg.cloud.acheron.admin.consumers;

import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

@Getter
@AllArgsConstructor
@ToString
final class ConsumerTO {

    public ConsumerTO(final @NonNull Consumer consumer) {
        this(consumer.getId() != null ? consumer.getId().toString() : null,
                consumer.getName(), consumer.getCreatedAt());
    }

    @Value("consumer_id")
    private final String consumerId;

    @JsonView(View.Create.class)
    private final String name;

    @Value("created_at")
    private final Date createdAt;
}
