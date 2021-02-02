package com.github.dockerunit.discovery.consul;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsulService {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonIgnore
    private String containerId;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Port")
    private int port;

    @JsonProperty("Check")
    private ConsulCheck check;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConsulCheck {

        @JsonProperty("Args")
        private List<String> args;

        @JsonProperty("HTTP")
        private String http;

        @JsonProperty("TCP")
        private String tcp;

        @JsonProperty("Method")
        private String method;

        @JsonProperty("Interval")
        private String interval;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("TTL")
        private String ttl;

        @JsonProperty("Shell")
        private String shell;

        @JsonProperty("TLSSkipVerify")
        private boolean tlsSkipVerify;
    }
}
