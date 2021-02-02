package com.github.dockerunit.discovery.consul;

import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.CONSUL_POLLING_PERIOD;
import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.CONSUL_POLLING_PERIOD_DEFAULT;
import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_DEFAULT;
import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_PROPERTY;
import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_HOST_PROPERTY;
import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.SERVICE_DISCOVERY_TIMEOUT;
import static com.github.dockerunit.discovery.consul.ConsulDiscoveryConfig.SERVICE_DISCOVERY_TIMEOUT_DEFAULT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerunit.core.Service;
import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.ServiceInstance;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.internal.ServiceDescriptor;
import com.github.dockerunit.core.internal.docker.DefaultDockerClientProvider;
import com.github.dockerunit.core.internal.service.DefaultServiceContext;
import com.github.dockerunit.discovery.consul.annotation.TCPHealthCheck;

public class ConsulDiscoveryProvider implements DiscoveryProvider {

    private static final String DOCKER_HOST = System.getProperty(
            DOCKER_HOST_PROPERTY,
            System.getProperty(DOCKER_BRIDGE_IP_PROPERTY, DOCKER_BRIDGE_IP_DEFAULT)
    );
    private final ConsulHttpResolver resolver;
    private final ConsulRegistrator registrator;
    private final DockerClient dockerClient;
    private final int discoveryTimeout;
    private final int consulPollingPeriod;

    static final String CONSUL_DNS_SUFFIX = ".service.consul";

    public ConsulDiscoveryProvider() {
        resolver = new ConsulHttpResolver(DOCKER_HOST, ConsulDescriptor.CONSUL_PORT);
        discoveryTimeout = Integer.parseInt(
                System.getProperty(SERVICE_DISCOVERY_TIMEOUT, SERVICE_DISCOVERY_TIMEOUT_DEFAULT)
        );
        consulPollingPeriod = Integer.parseInt(
                System.getProperty(CONSUL_POLLING_PERIOD, CONSUL_POLLING_PERIOD_DEFAULT)
        );
        dockerClient = new DefaultDockerClientProvider().getClient();
        registrator = new ConsulRegistrator(
                dockerClient,
                consulPollingPeriod,
                DOCKER_HOST,
                ConsulDescriptor.CONSUL_PORT
        );
    }

    @Override
    public Class<?> getDiscoveryConfig() {
        return ConsulDiscoveryConfig.class;
    }

    @Override
    public ServiceContext populateRegistry(ServiceContext context) {
        trackContext(context);
        Set<Service> services = context.getServices().stream()
                .map(this::doDiscovery)
                .collect(Collectors.toSet());

        return new DefaultServiceContext(services);
    }

    private void trackContext(ServiceContext context) {
        context.getServices()
                .forEach(svc -> svc.getInstances().forEach(si -> registrator.trackContainer(si.getContainerId())));
    }

    @Override
    public ServiceContext clearRegistry(ServiceContext currentContext, ServiceContext globalContext) {
        Set<Service> services = currentContext.getServices().stream()
                .map(s -> doCleanup(s, globalContext.getService(s.getName())))
                .collect(Collectors.toSet());

        return new DefaultServiceContext(services);
    }

    private Service doDiscovery(Service s) {
        List<ServiceRecord> records;
        try {
            records = resolver.resolveService(s.getName(), s.getInstances().size(),
                    discoveryTimeout, consulPollingPeriod, extractInitialDelay(s.getDescriptor()));
        } catch (Exception e) {
            return s.withInstances(s.getInstances().stream()
                    .map(i -> i.withStatus(ServiceInstance.Status.ABORTED)
                            .withStatusDetails(e.getMessage()))
                    .collect(Collectors.toSet()));
        }

        Set<ServiceInstance> withPorts = s.getInstances().stream()
                .map(si -> {
                    InspectContainerResponse r = dockerClient.inspectContainerCmd(si.getContainerId()).exec();
                    return si.withGatewayPort(findPort(r, records).orElse(0))
                            .withContainerPort(records.stream().findFirst().map(sr -> sr.getPort()).orElse(0))
                            .withGatewayAddress(DOCKER_HOST)
                            .withContainerName(r.getName())
                            .withContainerIp(ContainerUtils.extractBridgeIpAddress(r.getNetworkSettings()).orElse(""))
                            .withStatus(ServiceInstance.Status.DISCOVERED)
                            .withStatusDetails("Discovered via consul");
                }).collect(Collectors.toSet());

        return s.withInstances(withPorts);
    }

    private int extractInitialDelay(ServiceDescriptor descriptor) {
        return descriptor.getOptions().stream()
                .filter(TCPHealthCheck.class::isInstance)
                .findFirst()
                .map(TCPHealthCheck.class::cast)
                .map(TCPHealthCheck::initialDelay)
                .orElse(0);
    }

    private Service doCleanup(Service current, Service global) {
        try {
            int expectedRecords = global != null ? global.getInstances().size() : 0;
            resolver.verifyCleanup(current.getName() + CONSUL_DNS_SUFFIX,
                    expectedRecords,
                    discoveryTimeout,
                    consulPollingPeriod);
            return current;
        } catch (Exception e) {
            return current.withInstances(current.getInstances().stream()
                    .map(si -> si.withStatus(ServiceInstance.Status.TERMINATION_FAILED)
                            .withStatusDetails(e.getMessage()))
                    .collect(Collectors.toSet()));
        }
    }

    private Optional<Integer> findPort(InspectContainerResponse response, List<ServiceRecord> records) {
        return records.stream()
                .filter(r -> matchRecord(r, response))
                .findFirst()
                .flatMap(r -> ContainerUtils.extractMappedPort(r.getPort(), response.getNetworkSettings()));
    }

    private boolean matchRecord(ServiceRecord record, InspectContainerResponse r) {
        return matchIP(record.getServiceAddress(), r) && matchPort(record.getPort(), r);
    }

    private boolean matchPort(int port, InspectContainerResponse r) {
        return r.getNetworkSettings().getPorts().getBindings().keySet().stream()
                .map(ExposedPort::getPort)
                .anyMatch(p -> p == port);
    }

    private boolean matchIP(String address, InspectContainerResponse r) {
        return null != address && address.equals(ContainerUtils.extractBridgeIpAddress(r.getNetworkSettings())
                .orElse(null));
    }

    private Optional<Integer> parsePort(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }

}
