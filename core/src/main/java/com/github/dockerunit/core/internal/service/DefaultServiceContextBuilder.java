package com.github.dockerunit.core.internal.service;

import java.util.HashSet;
import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerunit.core.Service;
import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.internal.UsageDescriptor;
import com.github.dockerunit.core.internal.ServiceBuilder;
import com.github.dockerunit.core.internal.ServiceBuilderFactory;
import com.github.dockerunit.core.internal.ServiceContextBuilder;
import com.github.dockerunit.core.internal.ServiceDescriptor;
import com.github.dockerunit.core.internal.docker.DockerClientProviderFactory;

public class DefaultServiceContextBuilder implements ServiceContextBuilder {

    private final DockerClient client = DockerClientProviderFactory.create().getClient();
    private final ServiceBuilder serviceBuilder = ServiceBuilderFactory.create();

    @Override
    public ServiceContext buildContext(UsageDescriptor descriptor) {
        return new DefaultServiceContext(descriptor.getUsages().stream()
                .map(d -> serviceBuilder.build(d, client))
                .collect(Collectors.toSet()));
    }

    @Override
    public ServiceContext clearContext(ServiceContext context) {
        return new DefaultServiceContext(context.getServices().stream()
                .map(s -> serviceBuilder.cleanup(s, client))
                .collect(Collectors.toSet()));
    }

    @Override
    public ServiceContext buildServiceContext(ServiceDescriptor descriptor) {
        HashSet<Service> services = new HashSet<>();
        services.add(serviceBuilder.build(descriptor, client));
        return new DefaultServiceContext(services);
    }

}
