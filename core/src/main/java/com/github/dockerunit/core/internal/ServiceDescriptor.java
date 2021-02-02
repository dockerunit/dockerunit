package com.github.dockerunit.core.internal;

import com.github.dockerunit.core.annotation.Svc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public interface ServiceDescriptor {

    Svc getSvcDefinition();

    String getSvcName();

    List<? extends Annotation> getOptions();
    
    Method getCustomisationHook();
    
    Object getInstance();
    
    String getContainerName();
    
    int getReplicas();
    
    int getPriority();
    
}
