package com.github.dockerunit.core.internal.reflect;

public class DependencyDescriptorBuilderFactory {

    private static final DefaultUsageDescriptorBuilder INSTANCE = new DefaultUsageDescriptorBuilder();

    public static UsageDescriptorBuilder create() {
        return INSTANCE;
    }

}
