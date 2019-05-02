package com.github.dockerunit.internal.reflect;

import java.util.List;

import com.github.dockerunit.internal.UsageDescriptor;
import com.github.dockerunit.internal.ServiceDescriptor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultUsageDescriptor implements UsageDescriptor {

    private List<ServiceDescriptor> usages;

    public List<ServiceDescriptor> getUsages() {
        usages.sort((d1, d2) -> d2.getPriority() - d1.getPriority());
        return usages;
    }

}
