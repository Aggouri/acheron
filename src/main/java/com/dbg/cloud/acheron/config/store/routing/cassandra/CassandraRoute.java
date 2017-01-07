package com.dbg.cloud.acheron.config.store.routing.cassandra;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.Set;

@Table(value = "routes")
@Getter
@Setter
@EqualsAndHashCode
public final class CassandraRoute implements Serializable {

    @PrimaryKey
    private String id;

    @Column("http_methods")
    private Set<String> httpMethods;

    @Column("path")
    private String path;

    @Column(value = "service_id")
    private String serviceId;

    @Column
    private String url;

    @Column(value = "keep_prefix")
    private Boolean keepPrefix;

    @Column
    private Boolean retryable;

    @Column(value = "override_sensitive_headers")
    private Boolean overrideSensitiveHeaders;

    @Column(value = "sensitive_headers")
    private Set<String> sensitiveHeaders;
}
