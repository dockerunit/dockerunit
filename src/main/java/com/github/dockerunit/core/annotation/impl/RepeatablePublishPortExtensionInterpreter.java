package com.github.dockerunit.core.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.core.annotation.ExtensionInterpreter;
import com.github.dockerunit.core.annotation.PublishPort;
import com.github.dockerunit.core.annotation.RepeatablePublishPort;
import com.github.dockerunit.core.internal.ServiceDescriptor;

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
