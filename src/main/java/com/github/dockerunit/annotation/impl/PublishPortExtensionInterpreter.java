package com.github.dockerunit.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerunit.annotation.ExtensionInterpreter;
import com.github.dockerunit.annotation.PublishPort;
import com.github.dockerunit.annotation.PublishPort.Protocol;
import com.github.dockerunit.internal.ServiceDescriptor;

public class PublishPortExtensionInterpreter implements ExtensionInterpreter<PublishPort> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, PublishPort pb) {
        ExposedPort containerPort = toExposedPort(pb);

        List<ExposedPort> ports = new ArrayList<>(Arrays.asList(cmd.getExposedPorts()));
        ports.add(containerPort);

        HostConfig hc = cmd.getHostConfig();

        Ports bindings = Optional.ofNullable(hc.getPortBindings()).orElse(new Ports());
        bindings.bind(containerPort, toHostBinding(pb));

        return cmd.withExposedPorts(ports).withHostConfig(hc.withPortBindings(bindings));
    }

    private ExposedPort toExposedPort(PublishPort pb) {
        return pb.protocol() == Protocol.TCP ? ExposedPort.tcp(pb.container()) : ExposedPort.udp(pb.container());
    }

    private Binding toHostBinding(PublishPort pb) {
        return pb.hostIp().isEmpty()
                ? Binding.bindPort(pb.host())
                : Binding.bindIpAndPort(pb.hostIp(), pb.host());
    }
}
