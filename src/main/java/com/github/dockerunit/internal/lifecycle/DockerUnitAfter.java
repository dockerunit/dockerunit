package com.github.dockerunit.internal.lifecycle;

import com.github.dockerunit.discovery.DiscoveryProvider;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.dockerunit.DockerUnitRunner;
import com.github.dockerunit.ServiceContext;
import com.github.dockerunit.internal.ServiceContextBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitAfter extends Statement {

    private final FrameworkMethod method;
    private final DockerUnitRunner runner;
    private final Statement statement;
    private final DiscoveryProvider discoveryProvider;
    private final ServiceContextBuilder contextBuilder;

    @Override
    public void evaluate() throws Throwable {
        try {
            statement.evaluate();
        } finally {
            ServiceContext context = runner.getContext(method);
            if (context != null) {
                context = context.subtract(runner.getClassContext());
                ServiceContext cleared = contextBuilder.clearContext(context);
                runner.setContext(method, cleared);
                discoveryProvider.clearRegistry(cleared, runner.getClassContext());
            }
        }
    }

}
