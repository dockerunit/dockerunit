package com.github.dockerunit.internal.reflect;

import java.util.List;

import com.github.dockerunit.internal.UsageDescriptor;
import com.github.dockerunit.internal.ServiceDescriptor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultDependencyDescriptor implements UsageDescriptor {

    private List<ServiceDescriptor> dependencies;

    public List<ServiceDescriptor> getDependencies() {
        dependencies.sort((d1, d2) -> d1.getOrder() - d2.getOrder());
        return dependencies;
    }

}
