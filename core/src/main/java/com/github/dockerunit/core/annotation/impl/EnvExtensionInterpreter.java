package com.github.dockerunit.core.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.core.annotation.Env;
import com.github.dockerunit.core.annotation.ExtensionInterpreter;
import com.github.dockerunit.core.internal.ServiceDescriptor;

public class EnvExtensionInterpreter implements ExtensionInterpreter<Env> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, Env e) {
        String[] env = cmd.getEnv();
        List<String> finalEnv = new ArrayList<>();
        finalEnv.addAll(Arrays.asList(e.value()));
        if (env != null) {
            finalEnv.addAll(Arrays.asList(env));
        }
        return cmd.withEnv(finalEnv);
    }

}
