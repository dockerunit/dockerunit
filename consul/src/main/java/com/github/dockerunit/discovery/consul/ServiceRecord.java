package com.github.dockerunit.discovery.consul;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@With
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRecord {

    @JsonProperty("ServiceName")
    private String name;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("ServicePort")
    private int port;

    @JsonProperty("ServiceAddress")
    private String serviceAddress;

    @JsonProperty("Service")
    private Service service;

    @JsonProperty("Checks")
    private List<Check> checks;

    public String getName() {
        return service != null ? service.getName() : name;
    }

    public String getAddress() {
        return service != null ? service.getAddress() : address;
    }

    public int getPort() {
        return service != null ? service.getPort() : port;
    }

    @With
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Service {

        @JsonProperty("Service")
        private String name;

        @JsonProperty("Address")
        private String address;

        @JsonProperty("Port")
        private int port;

    }

    @With
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Check {

        public static final String PASSING = "passing";

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Status")
        private String status;

    }
}
