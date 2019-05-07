package com.github.dockerunit.core.internal;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerunit.core.Service;

public interface ServiceBuilder {

    Service build(ServiceDescriptor descriptor, DockerClient client);

	Service cleanup(Service s, DockerClient client);
    
}
