package com.github.dockerunit.internal.reflect;

import org.junit.runners.model.FrameworkMethod;

import com.github.dockerunit.internal.UsageDescriptor;

public interface UsageDescriptorBuilder {

    UsageDescriptor buildDescriptor(FrameworkMethod method);

    UsageDescriptor buildDescriptor(Class<?> klass);

}
