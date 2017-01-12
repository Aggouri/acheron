package com.dbg.cloud.acheron.plugins.correlationid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorrelationIdConfig {

    @JsonProperty("custom_header_name")
    private final String customHeaderName;
}
