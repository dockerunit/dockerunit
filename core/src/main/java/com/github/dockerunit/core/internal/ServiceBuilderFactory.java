package com.github.dockerunit.core.internal;

import com.github.dockerunit.core.internal.service.DefaultServiceBuilder;

public class ServiceBuilderFactory {

    private static final DefaultServiceBuilder INSTANCE = new DefaultServiceBuilder();

    public static ServiceBuilder create() {
        return INSTANCE;
    }
    
}
