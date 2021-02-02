package com.github.dockerunit.discovery.consul;

import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.discovery.DiscoveryProviderFactory;

public class ConsulDiscoveryProviderFactory implements DiscoveryProviderFactory {

    @Override
    public DiscoveryProvider getProvider() {
        return new ConsulDiscoveryProvider();
    }

}
