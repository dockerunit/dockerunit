package com.github.dockerunit.discovery.consul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerunit.core.annotation.ContainerBuilder;
import com.github.dockerunit.core.annotation.Svc;
import com.github.dockerunit.discovery.consul.annotation.UseConsulDns;

@Svc(name = "consul", image = "")
public class ConsulDescriptor {

    private static final Logger logger = Logger.getLogger(ConsulDescriptor.class.getSimpleName());

    static final int CONSUL_DNS_PORT = 53;
    static final int CONSUL_PORT = 8500;

    @ContainerBuilder
    public CreateContainerCmd setup(CreateContainerCmd cmd) {
        List<ExposedPort> ports = new ArrayList<>(Arrays.asList(Objects.requireNonNull(cmd.getExposedPorts())));

        ExposedPort consulPort = ExposedPort.tcp(CONSUL_PORT);
        ports.add(consulPort);

        Ports bindings = Objects.requireNonNull(cmd.getHostConfig()).getPortBindings();
        if (bindings == null) {
            bindings = new Ports();
        }

        boolean enableDnsFlag = Boolean.parseBoolean(
                System.getProperty(
                        ConsulDiscoveryConfig.CONSUL_DNS_ENABLED_PROPERTY,
                        ConsulDiscoveryConfig.CONSUL_DNS_ENABLED_DEFAULT)
        );

        if (enableDnsFlag) {
            activateDns(ports);
        } else {
            logger.warning("Consul DNS has been disabled. Usages of @" + UseConsulDns.class.getSimpleName()
                    + " will be ignored.");
        }

        bindings.bind(consulPort, Binding.bindPort(8500));

        HostConfig hc = cmd.getHostConfig().withPortBindings(bindings);

        return cmd.withHostConfig(hc)
                .withExposedPorts(ports)
                .withImage(
                        System.getProperty(
                                ConsulDiscoveryConfig.CONSUL_IMAGE,
                                ConsulDiscoveryConfig.CONSUL_DEFAULT_IMAGE
                        )
                )
                .withCmd("sh", "-c", "exec consul agent -dev -client=0.0.0.0 -enable-script-checks -dns-port=53");

    }

    private void activateDns(List<ExposedPort> ports) {
        ExposedPort dnsPort = ExposedPort.udp(CONSUL_DNS_PORT);
        ports.add(dnsPort);
    }

}
