package com.github.qzagarese.dockerunit.internal;

import com.github.dockerjava.api.DockerClient;
import com.github.qzagarese.dockerunit.Service;

public interface ServiceBuilder {

    Service build(TestDependency dependency, DockerClient client);

	Service cleanup(Service s, DockerClient client);
    
}
