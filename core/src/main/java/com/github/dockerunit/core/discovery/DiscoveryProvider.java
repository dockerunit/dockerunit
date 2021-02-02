package com.github.dockerunit.core.discovery;

import com.github.dockerunit.core.ServiceContext;

public interface DiscoveryProvider {

    Class<?> getDiscoveryConfig();

    ServiceContext populateRegistry(ServiceContext context);

    ServiceContext clearRegistry(ServiceContext context, ServiceContext globalContext);

}
