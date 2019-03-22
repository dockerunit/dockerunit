package com.github.dockerunit.discovery;

import com.github.dockerunit.ServiceContext;

public interface DiscoveryProvider {

    Class<?> getDiscoveryConfig();

    ServiceContext populateRegistry(ServiceContext context);

    ServiceContext clearRegistry(ServiceContext context, ServiceContext globalContext);

}
