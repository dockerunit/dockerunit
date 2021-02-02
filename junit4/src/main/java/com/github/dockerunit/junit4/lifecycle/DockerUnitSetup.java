package com.github.dockerunit.junit4.lifecycle;

import com.github.dockerunit.core.Service;
import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.core.ServiceInstance;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.internal.ServiceContextBuilder;
import com.github.dockerunit.core.internal.UsageDescriptor;
import com.github.dockerunit.core.internal.service.DefaultServiceContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DockerUnitSetup {

    private static final Logger logger = Logger.getLogger(DockerUnitSetup.class.getSimpleName());

    private final ServiceContextBuilder contextBuilder;
    private final DiscoveryProvider discoveryProvider;

    public ServiceContext setup(UsageDescriptor descriptor) {
        // Create containers and perform discovery one svc at the time
        final AtomicBoolean failureOccured = new AtomicBoolean(false);
        List<ServiceContext> serviceContexts = descriptor.getUsages().stream()
                .map(contextBuilder::buildServiceContext)
                .map(ctx -> {
                    if (!ctx.checkStatus(ServiceInstance.Status.STARTED)) {
                        failureOccured.set(true);
                    }
                    return ctx;
                }).map(ctx -> {
                    if (failureOccured.get()) {
                        logger.info(
                                "Skipping discovery of svc " + getServiceName(ctx) + " due to a previous failure.");
                        return abortService(ctx);
                    }

                    logger.info("Performing discovery for svc " + getServiceName(ctx));
                    ServiceContext postDiscoveryCtx = discoveryProvider.populateRegistry(ctx);
                    if (!postDiscoveryCtx.checkStatus(ServiceInstance.Status.DISCOVERED)) {
                        failureOccured.set(true);
                    }
                    return postDiscoveryCtx;
                }).collect(Collectors.toList());

        ServiceContext completeContext = mergeContexts(serviceContexts);
        return completeContext;
    }

    private String getServiceName(ServiceContext ctx) {
        return ctx.getServices().stream().findFirst().get().getName();
    }

    private ServiceContext abortService(ServiceContext ctx) {
        return new DefaultServiceContext(ctx.getServices().stream()
                .map(svc -> svc.withInstances(abortInstances(svc)))
                .collect(Collectors.toSet()));
    }

    private Set<ServiceInstance> abortInstances(Service svc) {
        return svc.getInstances().stream()
                .map(si -> ensureStatus(si, ServiceInstance.Status.ABORTED, "Aborted due to previous failure."))
                .collect(Collectors.toSet());
    }

    private ServiceInstance ensureStatus(ServiceInstance si, ServiceInstance.Status status, String statusDetails) {
        return si.hasStatus(status) ? si : si.withStatus(status)
                .withStatusDetails(statusDetails);
    }

    private ServiceContext mergeContexts(List<ServiceContext> serviceContexts) {
        ServiceContext completeContext = null;
        if (!serviceContexts.isEmpty()) {
            completeContext = serviceContexts.remove(0);
        }
        for (ServiceContext serviceContext : serviceContexts) {
            completeContext = completeContext.merge(serviceContext);
        }
        return completeContext;
    }

}
