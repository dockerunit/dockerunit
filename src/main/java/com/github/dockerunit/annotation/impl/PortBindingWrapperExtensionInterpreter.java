package com.github.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.annotation.ExtensionInterpreter;
import com.github.dockerunit.annotation.PortBinding;
import com.github.dockerunit.annotation.PortBindings;
import com.github.dockerunit.internal.ServiceDescriptor;

public class PortBindingWrapperExtensionInterpreter implements ExtensionInterpreter<PortBindings> {

    private PortBindingExtensionInterpreter builder = new PortBindingExtensionInterpreter();

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, PortBindings pbs) {
        for (PortBinding pb : pbs.value()) {
            cmd = builder.build(sd, cmd, pb);
        }
        return cmd;
    }

}
