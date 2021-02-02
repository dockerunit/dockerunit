package com.github.dockerunit.core.internal.reflect;

import com.github.dockerunit.core.internal.UsageDescriptor;

import java.lang.reflect.Method;

public interface UsageDescriptorBuilder {

    UsageDescriptor buildDescriptor(Method method);

    UsageDescriptor buildDescriptor(Class<?> klass);

}
