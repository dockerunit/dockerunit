package com.github.dockerunit.internal;

import java.util.List;

public interface UsageDescriptor {

    List<ServiceDescriptor> getDependencies();
    
}
