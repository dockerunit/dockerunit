package com.github.dockerunit.internal.lifecycle;

import java.util.HashSet;

import com.github.dockerunit.discovery.DiscoveryProvider;
import com.github.dockerunit.internal.service.DefaultServiceContext;
import org.junit.runners.model.Statement;

import com.github.dockerunit.DockerUnitRunner;
import com.github.dockerunit.ServiceContext;
import com.github.dockerunit.ServiceInstance.Status;
import com.github.dockerunit.internal.ServiceContextBuilder;
import com.github.dockerunit.internal.UsageDescriptor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitBeforeClass extends Statement {

    private final DockerUnitRunner runner;
    private final Statement next;
    private final DiscoveryProvider discoveryProvider;
    private final ServiceContextBuilder contextBuilder;
    private final UsageDescriptor descriptor;
    private final UsageDescriptor discoveryProviderDescriptor;

    @Override
    public void evaluate() throws Throwable {
        ServiceContext discoveryContext = contextBuilder.buildContext(discoveryProviderDescriptor);
        runner.setDiscoveryContext(discoveryContext);
        if (!discoveryContext.checkStatus(Status.STARTED)) {
            throw new RuntimeException(discoveryContext.getFormattedErrors());
        }

        ServiceContext context = new DockerUnitSetup(contextBuilder, discoveryProvider).setup(descriptor);

        if (context == null) {
            context = new DefaultServiceContext(new HashSet<>());
        }

        runner.setClassContext(context);
        if (!context.checkStatus(Status.DISCOVERED)) {
            throw new RuntimeException(context.getFormattedErrors());
        }
        next.evaluate();
    }

}
