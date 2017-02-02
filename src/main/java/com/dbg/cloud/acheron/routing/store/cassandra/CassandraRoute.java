package com.dbg.cloud.acheron.routing.store.cassandra;

import com.dbg.cloud.acheron.routing.Route;
import lombok.*;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Table(value = "routes")
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public final class CassandraRoute implements Route, Serializable {

    @PrimaryKey
    private String id;

    @Column("http_methods")
    private Set<String> httpMethods = new HashSet<>();

    @Column("path")
    private String path;

    @Column(value = "service_id")
    private String serviceId;

    @Column
    private String url;

    @Column(value = "keep_prefix")
    private Boolean keepPrefix = false;

    @Column
    private Boolean retryable = false;

    @Column(value = "override_sensitive_headers")
    private Boolean overrideSensitiveHeaders = false;

    @Column(value = "sensitive_headers")
    private Set<String> sensitiveHeaders = new HashSet<>();

    @Column(value = "created_at")
    private Date createdAt = new Date();

    @Override
    public boolean isKeepPrefix() {
        return keepPrefix != null ? keepPrefix : false;
    }

    @Override
    public boolean isRetryable() {
        return retryable != null ? retryable : false;
    }

    @Override
    public boolean isOverrideSensitiveHeaders() {
        return overrideSensitiveHeaders != null ? overrideSensitiveHeaders : false;
    }

    public Set<String> getHttpMethods() {
        return httpMethods != null ? httpMethods : new HashSet<>();
    }

    public Set<String> getSensitiveHeaders() {
        return sensitiveHeaders != null ? sensitiveHeaders : new HashSet<>();
    }
}
