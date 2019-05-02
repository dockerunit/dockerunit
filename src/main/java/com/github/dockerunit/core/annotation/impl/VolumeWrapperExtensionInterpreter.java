package com.github.dockerunit.core.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.core.annotation.ExtensionInterpreter;
import com.github.dockerunit.core.annotation.Volume;
import com.github.dockerunit.core.annotation.RepeatableVolume;
import com.github.dockerunit.core.internal.ServiceDescriptor;

public class VolumeWrapperExtensionInterpreter implements ExtensionInterpreter<RepeatableVolume> {

    private VolumeExtensionInterpreter builder = new VolumeExtensionInterpreter();

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, RepeatableVolume vs) {
        for (Volume v : vs.value()) {
            cmd = builder.build(sd, cmd, v);
        }
        return cmd;
    }

}
