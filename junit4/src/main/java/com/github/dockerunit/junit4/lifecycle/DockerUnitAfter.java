package com.github.dockerunit.junit4.lifecycle;

import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.internal.ServiceContextBuilder;
import com.github.dockerunit.junit4.DockerUnitRunner;
import lombok.AllArgsConstructor;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

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
