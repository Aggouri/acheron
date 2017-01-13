package com.dbg.cloud.acheron.plugins.apikey.store.cassandra;

import com.dbg.cloud.acheron.plugins.apikey.store.APIKey;
import lombok.*;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Table(value = "api_key_keys")
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
class CassandraAPIKey implements APIKey, Serializable {

    @PrimaryKey
    private UUID id;

    @Column(value = "api_key")
    private String apiKey;

    @Column(value = "consumer_id")
    private UUID consumerId;

    @Column(value = "consumer_name")
    private String consumerName;

    @Column(value = "consumer_created_at")
    private Date consumerCreatedAt;

    @Column(value = "created_at")
    private Date createdAt;

    public String getAPIKey() {
        return apiKey;
    }
}
