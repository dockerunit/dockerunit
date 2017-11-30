package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.PublishPorts;
import com.github.qzagarese.dockerunit.internal.TestDescriptor;

public class PublishPortsExtensionInterpreter implements ExtensionInterpreter<PublishPorts> {

    @Override
    public CreateContainerCmd build(TestDescriptor td, CreateContainerCmd cmd, PublishPorts t) {
        return cmd.withPublishAllPorts(true);
    }

}