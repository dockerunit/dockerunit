package com.github.dockerunit.junit5;

import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.ServiceInstance;
import com.github.dockerunit.core.annotation.WithSvc;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.discovery.DiscoveryProviderFactory;
import com.github.dockerunit.core.internal.ServiceContextBuilder;
import com.github.dockerunit.core.internal.ServiceContextBuilderFactory;
import com.github.dockerunit.core.internal.UsageDescriptor;
import com.github.dockerunit.core.internal.reflect.DependencyDescriptorBuilderFactory;
import com.github.dockerunit.core.internal.reflect.UsageDescriptorBuilder;
import com.github.dockerunit.core.internal.service.DefaultServiceContext;
import com.github.dockerunit.junit5.exception.ConfigException;
import com.github.dockerunit.junit5.lifecycle.DockerUnitSetup;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class DockerUnitExtension implements BeforeAllCallback, AfterAllCallback {

    private static final Logger logger = Logger.getLogger(DockerUnitExtension.class.getSimpleName());

    private static final Map<String, ServiceContext> activeContexts = new HashMap<>();

    private final UsageDescriptorBuilder descriptorBuilder = DependencyDescriptorBuilderFactory.create();
    private final ServiceContextBuilder contextBuilder = ServiceContextBuilderFactory.create();
    private final DiscoveryProvider discoveryProvider;
    private final String serviceContextName;
    private ServiceContext discoveryContext;

    private static final String GLOBAL_CONTEXT_NAME = "dockerunit_global_context";

    /**
     * Returns the {@link ServiceContext} that has been associated to the instance of {@link DockerUnitExtension} that
     * has serviceContextName as parameter.
     *
     * @param serviceContextName
     * @return the associated {@link ServiceContext}
     */
    public static ServiceContext getServiceContext(String serviceContextName) {
        return Optional.ofNullable(activeContexts.get(serviceContextName))
                .orElseThrow(() -> new ConfigException("No active context for name " + serviceContextName
                        + " . Please make sure that you are using the context name that you have passed to the "
                        + DockerUnitExtension.class.getSimpleName()
                        + " constructor."));
    }

    /**
     * Returns the default {@link ServiceContext} or a random one if all the current instances of {@link DockerUnitExtension}
     * have been initialised with a specific svc context name.
     *
     * @return the default{@link ServiceContext}
     */
    public static ServiceContext getDefaultServiceContext() {
        return Optional.ofNullable(activeContexts.get(GLOBAL_CONTEXT_NAME))
                .orElse(
                        activeContexts.values().stream().findAny()
                                .orElseThrow(() -> new ConfigException("No active context detected. "
                                        + "Please make sure that you have declared at least one @"
                                        + WithSvc.class.getSimpleName()
                                        + " annotation on tha class that declares your "
                                        + DockerUnitExtension.class.getSimpleName()
                                        + " instance.")));
    }

    public DockerUnitExtension() {
        this(GLOBAL_CONTEXT_NAME);
    }

    public DockerUnitExtension(String serviceContextName) {
        ServiceLoader<DiscoveryProviderFactory> loader = ServiceLoader.load(DiscoveryProviderFactory.class);

        this.discoveryProvider = StreamSupport.stream(loader.spliterator(), false)
                .peek(impl -> logger.info(
                        "Found discovery provider factory of type " + impl.getClass().getSimpleName()))
                .findFirst()
                .map(impl -> {
                    logger.info("Using discovery provider factory " + impl.getClass().getSimpleName());
                    return impl;
                })
                .map(DiscoveryProviderFactory::getProvider)
                .orElseThrow(() -> new RuntimeException("No discovery provider factory found. Aborting test."));

        this.serviceContextName = serviceContextName;
    }

    private void doSetup(final ExtensionContext description) {
        UsageDescriptor descriptor = descriptorBuilder.buildDescriptor(description.getRequiredTestClass());
        UsageDescriptor discoveryProviderDescriptor = descriptorBuilder.buildDescriptor(discoveryProvider.getDiscoveryConfig());

        // Build discovery context
        this.discoveryContext = contextBuilder.buildContext(discoveryProviderDescriptor);
        if (!discoveryContext.checkStatus(ServiceInstance.Status.STARTED)) {
            throw new RuntimeException(discoveryContext.getFormattedErrors());
        }

        ServiceContext completeContext = new DockerUnitSetup(contextBuilder, discoveryProvider).setup(descriptor);

        activeContexts.put(this.serviceContextName, completeContext);
        if (!completeContext.checkStatus(ServiceInstance.Status.DISCOVERED)) {
            throw new RuntimeException(completeContext.getFormattedErrors());
        }

    }

    private void doTeardown() {
        ServiceContext context = activeContexts.get(this.serviceContextName);
        if (context != null) {
            ServiceContext cleared = contextBuilder.clearContext(context);
            discoveryProvider.clearRegistry(cleared, new DefaultServiceContext(new HashSet<>()));
        }

        if (this.discoveryContext != null) {
            contextBuilder.clearContext(discoveryContext);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        logger.info(
                "Cleaning up active services for the following context: " + context.getDisplayName());
        doTeardown();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        logger.info("Performing svc discovery for the following context: " + context.getDisplayName());
        doSetup(context);
    }

}
