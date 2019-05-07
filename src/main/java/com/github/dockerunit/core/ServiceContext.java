package com.github.dockerunit.core;

import java.util.Set;

import com.github.dockerunit.core.annotation.Svc;

/**
 * Entry point including all the services used by the executing test.
 * Tests can use this to obtain the ip address and port of each container
 * they need to hit.
 *
 * @see Service
 * @see ServiceInstance
 */
public interface ServiceContext {

    /**
     * @return a set of all the services declared by the executing test.
     */
    public Set<Service> getServices();

    /**
     * @param name the name of the requested svc as declared in {@linkplain Svc}
     * @return the svc specified by the provided name. Null if no such svc has been declared.
     */
    public Service getService(String name);

    ServiceContext merge(ServiceContext context);

    ServiceContext subtract(ServiceContext context);

    boolean allHealthy();

    /**
     * Checks whether all the {@link ServiceInstance}s of all {@link Service}s in this context are in the specified {@link ServiceInstance.Status}
     *
     * @param status the {@link ServiceInstance.Status} to check
     * @return
     */
    boolean checkStatus(ServiceInstance.Status status);

    String getFormattedErrors();

}
