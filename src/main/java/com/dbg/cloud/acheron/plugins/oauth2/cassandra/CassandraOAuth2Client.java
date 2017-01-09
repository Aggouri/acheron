package com.dbg.cloud.acheron.plugins.oauth2.cassandra;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Table(value = "oauth2_clients")
@Getter
@Setter
@EqualsAndHashCode
final class CassandraOAuth2Client implements Serializable {

    @PrimaryKey(value = "id")
    private String clientId;

    @Column(value = "consumer_id")
    private UUID consumerId;

    @Column(value = "consumer_name")
    private String consumerName;

    @Column(value = "consumer_created_at")
    private Date consumerCreatedAt;

    @Column(value = "created_at")
    private Date createdAt;
}
