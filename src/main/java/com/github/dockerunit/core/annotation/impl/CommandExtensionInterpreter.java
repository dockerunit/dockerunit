package com.github.dockerunit.core.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.core.annotation.Command;
import com.github.dockerunit.core.annotation.ExtensionInterpreter;
import com.github.dockerunit.core.internal.ServiceDescriptor;

public class CommandExtensionInterpreter implements ExtensionInterpreter<Command> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, Command c) {
        return cmd.withCmd(c.value());
    }

}
