package com.github.dockerunit.internal;

import com.github.dockerunit.ServiceContext;

public interface ServiceContextBuilder {

    ServiceContext buildContext(UsageDescriptor descriptor);

    ServiceContext buildServiceContext(ServiceDescriptor descriptor);
    
	ServiceContext clearContext(ServiceContext context); 
    
}
