package com.github.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerunit.annotation.ExtensionInterpreter;
import com.github.dockerunit.annotation.PublishPorts;
import com.github.dockerunit.internal.ServiceDescriptor;

public class PublishPortsExtensionInterpreter implements ExtensionInterpreter<PublishPorts> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, PublishPorts pp) {
        HostConfig hc = cmd.getHostConfig().withPublishAllPorts(true);

        return cmd.withHostConfig(hc);
    }

}
