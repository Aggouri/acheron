package com.dbg.cloud.acheron.plugins.apikey.cassandra;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
class CassandraAPIKey implements Serializable {

    @PrimaryKey
    private UUID id;

    @Column(value = "api_key")
    private UUID apiKey;

    @Column(value = "consumer_id")
    private UUID consumerId;

    @Column(value = "consumer_name")
    private String consumerName;

    @Column(value = "consumer_created_at")
    private Date consumerCreatedAt;

    @Column(value = "created_at")
    private Date createdAt;
}
