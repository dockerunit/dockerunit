package com.github.dockerunit.core;

import com.github.dockerunit.core.annotation.Svc;
import com.github.dockerunit.core.discovery.DiscoveryProvider;
import com.github.dockerunit.core.internal.ServiceDescriptor;
import lombok.AllArgsConstructor;
import lombok.With;
import lombok.experimental.Wither;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a discoverable container or group of Docker containers.
 * Each container is based on the declared image.
 * <p>
 * It has at least one instance (container) and it is healthy if and only
 * if all its instances are healthy.
 * <p>
 * The svc name is used to instruct the {@link DiscoveryProvider} and
 * make the svc discoverable by  other services.
 *
 * @see Svc
 */
@With
@AllArgsConstructor
public class Service {

    private final String name;
    private final Set<ServiceInstance> instances;
    private final ServiceDescriptor descriptor;

    /**
     * @return true if all the instances have started successfully.
     */
    public boolean isHealthy() {
        return !instances.stream()
                .filter(this::isAborted)
                .findFirst().isPresent();
    }

    /**
     * Checks whether all the {@link ServiceInstance}s in this svc are in the specified {@link ServiceInstance.Status}.
     *
     * @param status the {@link ServiceInstance.Status} to check
     * @return true if all the {@link ServiceInstance}s in this svc are in the specified status, false otherwise.
     */
    public boolean checkStatus(ServiceInstance.Status status) {
        return instances.stream()
                .allMatch(si -> si.hasStatus(status));
    }

    /**
     * @return the svc name as declared in {@linkplain Svc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the set of instances (containers) for this svc.
     * @see ServiceInstance
     */
    public Set<ServiceInstance> getInstances() {
        return this.instances;
    }

    /**
     * @return the descriptor of this svc that provides the runtime representation of the annotation based
     * configuration that has been used.
     */
    public ServiceDescriptor getDescriptor() {
        return this.descriptor;
    }

    public List<String> getWarnings() {
        return instances.stream()
                .filter(this::isAborted)
                .map(i -> i.getStatusDetails())
                .collect(Collectors.toList());
    }

    private boolean isAborted(ServiceInstance i) {
        return i.hasStatus(ServiceInstance.Status.ABORTED);
    }

}
