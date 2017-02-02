package com.dbg.cloud.acheron.pluginconfig.store.cassandra;

import com.dbg.cloud.acheron.pluginconfig.PluginConfig;
import lombok.*;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(value = "plugins")
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public final class CassandraPluginConfig implements PluginConfig, Serializable {

    @PrimaryKey
    private CassandraPluginConfigPK pk;

    @Column(value = "route_id")
    private String routeId;

    @Column(value = "consumer_id")
    private UUID consumerId;

    @Column(value = "http_methods")
    private Set<String> httpMethods;

    @Column
    private String config;

    @Column
    private boolean enabled;

    @Column(value = "created_at")
    private Date createdAt = new Date();

    @Override
    public UUID getId() {
        return pk.getId();
    }

    @Override
    public String getName() {
        return pk.getName();
    }

    public Set<String> getHttpMethods() {
        return this.httpMethods != null ? this.httpMethods : new HashSet<>();
    }
}
