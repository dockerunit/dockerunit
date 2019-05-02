package com.github.dockerunit.core.internal;

import com.github.dockerunit.core.internal.service.DefaultServiceContextBuilder;

public class ServiceContextBuilderFactory {

    private static final DefaultServiceContextBuilder INSTANCE = new DefaultServiceContextBuilder();

    public static ServiceContextBuilder create() {
        return INSTANCE;
    }
    
}
