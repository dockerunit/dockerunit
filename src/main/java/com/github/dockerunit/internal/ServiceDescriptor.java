package com.github.dockerunit.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.github.dockerunit.annotation.Image;
import com.github.dockerunit.annotation.Named;

public interface ServiceDescriptor {

    Image getImage();
    
    Named getNamed();
    
    List<? extends Annotation> getOptions();
    
    Method getCustomisationHook();
    
    Object getInstance();
    
    String getContainerName();
    
    int getReplicas();
    
    int getOrder();
    
}
