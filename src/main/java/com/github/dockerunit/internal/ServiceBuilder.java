package com.github.dockerunit.internal;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerunit.Service;

public interface ServiceBuilder {

    Service build(ServiceDescriptor descriptor, DockerClient client);

	Service cleanup(Service s, DockerClient client);
    
}
