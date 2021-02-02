package com.github.dockerunit.discovery.consul;

import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.CONSUL_CONTAINER_NAME;

import com.github.dockerunit.core.annotation.WithSvc;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@WithSvc(svc = ConsulDescriptor.class, containerNamePrefix = CONSUL_CONTAINER_NAME)
public class ConsulDiscoveryConfig {

    public static final String DOCKER_BRIDGE_IP_PROPERTY = "docker.bridge.ip";
    public static final String DOCKER_BRIDGE_IP_DEFAULT = "172.17.42.1";
    public static final String DOCKER_HOST_PROPERTY = "docker.host";
    public static final String SERVICE_DISCOVERY_TIMEOUT = "service.discovery.timeout";
    public static final String SERVICE_DISCOVERY_TIMEOUT_DEFAULT = "30";
    public static final String CONSUL_POLLING_PERIOD = "consul.polling.period";
    public static final String CONSUL_POLLING_PERIOD_DEFAULT = "1";
    public static final String CONSUL_DNS_ENABLED_PROPERTY = "consul.dns.enabled";
    public static final String CONSUL_DNS_ENABLED_DEFAULT = "true";
    public static final String CONSUL_CONTAINER_NAME = "consul";
    public static final String CONSUL_IMAGE = "consul.image";
    public static final String CONSUL_DEFAULT_IMAGE = "consul:1.4.4";

}
