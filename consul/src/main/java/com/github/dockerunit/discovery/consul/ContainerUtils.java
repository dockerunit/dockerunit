package com.github.dockerunit.discovery.consul;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerunit.core.internal.docker.DefaultDockerClientProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContainerUtils {

    private static final com.github.dockerjava.api.DockerClient dockerClient = new DefaultDockerClientProvider().getClient();

    public static Optional<String> extractBridgeIpAddress(ContainerNetworkSettings settings) {
        return extractIp(settings.getNetworks());
    }

    public static Optional<String> extractBridgeIpAddress(NetworkSettings settings) {
        return extractIp(settings.getNetworks());
    }

    public static Optional<Integer> extractMappedPort(int port, NetworkSettings networkSettings) {
        return networkSettings.getPorts().getBindings().entrySet().stream()
                .filter(entry -> entry.getKey().getPort() == port)
                .map(Map.Entry::getValue)
                .filter(bindings -> bindings != null && bindings.length > 0)
                .findFirst()
                .flatMap(bindings -> parsePort(bindings[0].getHostPortSpec()));
    }

    public static Container getConsulContainer() {
        return dockerClient.listContainersCmd().exec().stream()
                .filter(ContainerUtils::isConsul)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(("Could not detect the Consul container.")));
    }

    private static boolean isConsul(Container c) {
        return Arrays.asList(c.getNames()).contains("/" + ConsulDiscoveryConfig.CONSUL_CONTAINER_NAME);
    }

    private static Optional<String> extractIp(Map<String, ContainerNetwork> networks) {
        return Optional.ofNullable(networks.entrySet().stream()
                .filter(network -> "bridge".equals(network.getKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(("Container is not connected to the bridge network.")))
                .getValue()
                .getIpAddress());
    }

    private static Optional<Integer> parsePort(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }

}
