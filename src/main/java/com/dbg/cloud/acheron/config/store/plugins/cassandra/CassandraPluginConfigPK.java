package com.dbg.cloud.acheron.config.store.plugins.cassandra;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.UUID;

@PrimaryKeyClass
@Getter
@Setter
@EqualsAndHashCode
public final class CassandraPluginConfigPK implements Serializable {

    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    @PrimaryKeyColumn(name = "name", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private String name;
}
