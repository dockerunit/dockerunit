package com.github.dockerunit.core.internal;

import java.util.List;

public interface UsageDescriptor {

    List<ServiceDescriptor> getUsages();
    
}
