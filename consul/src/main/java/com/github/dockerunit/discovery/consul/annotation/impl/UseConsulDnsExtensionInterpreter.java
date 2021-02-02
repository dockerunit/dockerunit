package com.github.dockerunit.discovery.consul.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerunit.core.annotation.ExtensionInterpreter;
import com.github.dockerunit.core.internal.ServiceDescriptor;
import com.github.dockerunit.discovery.consul.ContainerUtils;
import com.github.dockerunit.discovery.consul.annotation.UseConsulDns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UseConsulDnsExtensionInterpreter implements ExtensionInterpreter<UseConsulDns> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, UseConsulDns t) {

        Optional<String> dnsIp = Optional.ofNullable(ContainerUtils.extractBridgeIpAddress(ContainerUtils.getConsulContainer()
                .getNetworkSettings()).get());


        List<String> dnsList = new ArrayList<>();
        List<String> currentDnsList = Arrays.asList(Optional.ofNullable(cmd.getHostConfig().getDns()).orElse(new String[0]));
        String consulDns = dnsIp.orElseThrow(() -> new RuntimeException("Could not detect Consul container ip. Please make sure Consul is running."));
        dnsList.addAll(currentDnsList);
        dnsList.add(consulDns);

        HostConfig hc = cmd.getHostConfig()
                .withDns(dnsList);
        return cmd.withHostConfig(hc);
    }

}
