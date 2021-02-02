package com.github.dockerunit.core.internal;

import com.github.dockerunit.core.ServiceContext;

public interface ServiceContextBuilder {

    ServiceContext buildContext(UsageDescriptor descriptor);

    ServiceContext buildServiceContext(ServiceDescriptor descriptor);
    
	ServiceContext clearContext(ServiceContext context); 
    
}
