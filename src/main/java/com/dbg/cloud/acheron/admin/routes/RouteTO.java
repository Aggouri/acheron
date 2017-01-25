package com.dbg.cloud.acheron.admin.routes;

import com.dbg.cloud.acheron.config.routing.Route;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Date;
import java.util.Set;

@Getter
@AllArgsConstructor
@ToString
class RouteTO {

    public RouteTO(final @NonNull Route route) {
        this(route.getId(), route.getHttpMethods(), route.getPath(), route.getServiceId(), route.getUrl(),
                route.isKeepPrefix(), route.isRetryable(), route.isOverrideSensitiveHeaders(),
                route.getSensitiveHeaders(), route.getCreatedAt());
    }

    @JsonView(View.Create.class)
    private final String id;

    @JsonView(View.Create.class)
    @JsonProperty("http_methods")
    private final Set<String> httpMethods;

    @JsonView(View.Create.class)
    private final String path;

    @JsonView(View.Create.class)
    @JsonProperty("service_id")
    private final String serviceId;

    @JsonView(View.Create.class)
    private final String url;

    @JsonView(View.Create.class)
    @JsonProperty("keep_prefix")
    private final boolean keepPrefix;

    @JsonView(View.Create.class)
    private final boolean retryable;

    @JsonView(View.Create.class)
    @JsonProperty("override_sensitive_headers")
    private final boolean overrideSensitiveHeaders;

    @JsonView(View.Create.class)
    @JsonProperty("sensitive_headers")
    private final Set<String> sensitiveHeaders;

    @JsonProperty("created_at")
    private final Date createdAt;
}
