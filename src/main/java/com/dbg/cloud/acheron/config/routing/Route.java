package com.dbg.cloud.acheron.config.routing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Set;

public interface Route {

    String getId();

    Set<String> getHttpMethods();

    String getPath();

    String getServiceId();

    String getUrl();

    boolean isKeepPrefix();

    boolean isRetryable();

    boolean isOverrideSensitiveHeaders();

    Set<String> getSensitiveHeaders();

    Date getCreatedAt();

    @AllArgsConstructor
    @Getter
    @ToString
    final class Smart implements Route {
        private final String id;
        private final Set<String> httpMethods;
        private final String path;
        private final String serviceId;
        private final String url;
        private final boolean keepPrefix;
        private final boolean retryable;
        private final boolean overrideSensitiveHeaders;
        private final Set<String> sensitiveHeaders;
        private final Date createdAt;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    final class ForCreation implements Route {
        private final String id;
        private final Set<String> httpMethods;
        private final String path;
        private final String serviceId;
        private final String url;
        private final boolean keepPrefix;
        private final boolean retryable;
        private final boolean overrideSensitiveHeaders;
        private final Set<String> sensitiveHeaders;
        // already set
        private final Date createdAt = new Date();
    }
}
