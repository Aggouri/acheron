package com.dbg.cloud.acheron.plugins.ratelimiting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RateLimitingConfig {

    @JsonProperty("limit")
    private final Long limitRequestsPerWindow;

    @JsonProperty(value = "window")
    private Integer windowInSeconds = 1;
}
