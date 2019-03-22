package com.github.dockerunit.internal.lifecycle;

import java.util.HashSet;

import com.github.dockerunit.discovery.DiscoveryProvider;
import com.github.dockerunit.internal.service.DefaultServiceContext;
import org.junit.runners.model.Statement;

import com.github.dockerunit.DockerUnitRunner;
import com.github.dockerunit.ServiceContext;
import com.github.dockerunit.internal.ServiceContextBuilder;

import lombok.AllArgsConstructor;

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
