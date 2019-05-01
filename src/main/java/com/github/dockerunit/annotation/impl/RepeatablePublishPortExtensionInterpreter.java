package com.github.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.annotation.ExtensionInterpreter;
import com.github.dockerunit.annotation.PublishPort;
import com.github.dockerunit.annotation.RepeatablePublishPort;
import com.github.dockerunit.internal.ServiceDescriptor;

public class RepeatablePublishPortExtensionInterpreter implements ExtensionInterpreter<RepeatablePublishPort> {

    private PublishPortExtensionInterpreter builder = new PublishPortExtensionInterpreter();

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, RepeatablePublishPort pbs) {
        for (PublishPort pb : pbs.value()) {
            cmd = builder.build(sd, cmd, pb);
        }
        return cmd;
    }

}
