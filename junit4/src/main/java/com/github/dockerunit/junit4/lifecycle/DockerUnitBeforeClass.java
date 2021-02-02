package com.github.dockerunit.junit4.lifecycle;

import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.ServiceInstance;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.internal.ServiceContextBuilder;
import com.github.dockerunit.core.internal.UsageDescriptor;
import com.github.dockerunit.core.internal.service.DefaultServiceContext;
import com.github.dockerunit.junit4.DockerUnitRunner;
import lombok.AllArgsConstructor;
import org.junit.runners.model.Statement;

import java.util.HashSet;

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
        if (!discoveryContext.checkStatus(ServiceInstance.Status.STARTED)) {
            throw new RuntimeException(discoveryContext.getFormattedErrors());
        }

        ServiceContext context = new DockerUnitSetup(contextBuilder, discoveryProvider).setup(descriptor);

        if (context == null) {
            context = new DefaultServiceContext(new HashSet<>());
        }

        runner.setClassContext(context);
        if (!context.checkStatus(ServiceInstance.Status.DISCOVERED)) {
            throw new RuntimeException(context.getFormattedErrors());
        }
        next.evaluate();
    }

}
