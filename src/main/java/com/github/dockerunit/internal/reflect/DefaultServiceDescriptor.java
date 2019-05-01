package com.github.dockerunit.internal.reflect;

import com.github.dockerunit.annotation.Svc;
import com.github.dockerunit.internal.ServiceDescriptor;
import lombok.Builder;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

@Getter
@Builder
public class DefaultServiceDescriptor implements ServiceDescriptor {

    private Svc svcDefinition;
    private List<? extends Annotation> options;
    private Method customisationHook;
    private int replicas;
    private int priority;
    private String containerName;
    private Object instance;

}
