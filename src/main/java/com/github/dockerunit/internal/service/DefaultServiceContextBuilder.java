package com.github.dockerunit.internal.service;

import java.util.HashSet;
import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerunit.Service;
import com.github.dockerunit.ServiceContext;
import com.github.dockerunit.internal.UsageDescriptor;
import com.github.dockerunit.internal.ServiceBuilder;
import com.github.dockerunit.internal.ServiceBuilderFactory;
import com.github.dockerunit.internal.ServiceContextBuilder;
import com.github.dockerunit.internal.ServiceDescriptor;
import com.github.dockerunit.internal.docker.DockerClientProviderFactory;

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
