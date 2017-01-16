package com.dbg.cloud.acheron.plugins.oauth2.store.cassandra;

import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Client;
import lombok.*;
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
@ToString
@AllArgsConstructor
@NoArgsConstructor
final class CassandraOAuth2Client implements OAuth2Client, Serializable {

    @PrimaryKey
    private UUID id;

    @Column(value = "client_id")
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
