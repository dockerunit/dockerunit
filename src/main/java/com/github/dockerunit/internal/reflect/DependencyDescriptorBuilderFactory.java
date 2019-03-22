package com.github.dockerunit.internal.reflect;

public class DependencyDescriptorBuilderFactory {

    private static final DefaultDependencyDescriptorBuilder INSTANCE = new DefaultDependencyDescriptorBuilder();

    public static UsageDescriptorBuilder create() {
        return INSTANCE;
    }

}
