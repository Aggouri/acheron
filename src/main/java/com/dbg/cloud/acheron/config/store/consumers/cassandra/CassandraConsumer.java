package com.dbg.cloud.acheron.config.store.consumers.cassandra;

import com.dbg.cloud.acheron.config.store.consumers.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Table(value = "consumers")
@Getter
@Setter
@EqualsAndHashCode
public final class CassandraConsumer implements Consumer, Serializable {

    @PrimaryKey
    private UUID id;

    @Column
    private String name;

    @Column(value = "created_at")
    private Date createdAt;
}
