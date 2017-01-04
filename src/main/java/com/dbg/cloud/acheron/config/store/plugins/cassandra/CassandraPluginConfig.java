package com.dbg.cloud.acheron.config.store.plugins.cassandra;

import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Table(value = "plugins")
@Getter
@Setter
@EqualsAndHashCode
public class CassandraPluginConfig implements PluginConfig, Serializable {

    @PrimaryKey
    private CassandraPluginConfigPK pk;

    @Column(value = "route_id")
    private String routeId;

    @Column(value = "consumer_id")
    private UUID consumerId;

    @Column
    private String config;

    @Column
    private boolean enabled;

    @Column(value = "created_at")
    private Date createdAt;

    @Override
    public UUID getId() {
        return pk.getId();
    }

    @Override
    public String getName() {
        return pk.getName();
    }
}
