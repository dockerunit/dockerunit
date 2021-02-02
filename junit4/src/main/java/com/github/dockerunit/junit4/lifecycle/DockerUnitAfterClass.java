package com.github.dockerunit.junit4.lifecycle;

import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.internal.ServiceContextBuilder;
import com.github.dockerunit.core.internal.service.DefaultServiceContext;
import com.github.dockerunit.junit4.DockerUnitRunner;
import lombok.AllArgsConstructor;
import org.junit.runners.model.Statement;

import java.util.HashSet;

@AllArgsConstructor
public class DockerUnitAfterClass extends Statement {

    private final DockerUnitRunner runner;
    private final Statement statement;
    private final DiscoveryProvider discoveryProvider;
    private final ServiceContextBuilder contextBuilder;

    @Override
    public void evaluate() throws Throwable {
        try {
            statement.evaluate();
        } finally {
            ServiceContext context = runner.getClassContext();
            if (context != null) {
                ServiceContext cleared = contextBuilder.clearContext(context);
                discoveryProvider.clearRegistry(cleared, new DefaultServiceContext(new HashSet<>()));
            }
            ServiceContext discoveryContext = runner.getDiscoveryContext();
            if (discoveryContext != null) {
                contextBuilder.clearContext(discoveryContext);
            }
        }
    }

}
