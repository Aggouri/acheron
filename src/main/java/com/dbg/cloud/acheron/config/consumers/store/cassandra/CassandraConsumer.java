package com.dbg.cloud.acheron.config.consumers.store.cassandra;

import com.dbg.cloud.acheron.config.consumers.Consumer;
import lombok.*;
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
@AllArgsConstructor
@NoArgsConstructor
public final class CassandraConsumer implements Consumer, Serializable {

    @PrimaryKey
    private UUID id;

    @Column
    private String name;

    @Column(value = "created_at")
    private Date createdAt;
}
