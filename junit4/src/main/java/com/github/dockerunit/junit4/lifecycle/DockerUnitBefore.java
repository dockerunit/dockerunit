package com.github.dockerunit.junit4.lifecycle;

import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.ServiceInstance;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.internal.ServiceContextBuilder;
import com.github.dockerunit.core.internal.UsageDescriptor;
import com.github.dockerunit.core.internal.service.DefaultServiceContext;
import com.github.dockerunit.junit4.DockerUnitRunner;
import lombok.AllArgsConstructor;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.HashSet;

@AllArgsConstructor
public class DockerUnitBefore extends Statement {

    private final FrameworkMethod method;
    private final DockerUnitRunner runner;
    private final Statement next;
    private final DiscoveryProvider discoveryProvider;
    private final ServiceContextBuilder contextBuilder;
    private final UsageDescriptor descriptor;

    @Override
    public void evaluate() throws Throwable {

        ServiceContext methodLevelContext = new DockerUnitSetup(contextBuilder, discoveryProvider).setup(descriptor);

        if (methodLevelContext == null) {
            methodLevelContext = new DefaultServiceContext(new HashSet<>());
        }
        methodLevelContext = methodLevelContext.merge(runner.getClassContext());

        runner.setContext(method, methodLevelContext);
        if (!methodLevelContext.checkStatus(ServiceInstance.Status.DISCOVERED)) {
            throw new RuntimeException(methodLevelContext.getFormattedErrors());
        }
        next.evaluate();
    }

}
