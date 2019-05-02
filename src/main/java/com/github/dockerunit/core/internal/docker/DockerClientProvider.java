package com.github.dockerunit.core.internal.docker;

import com.github.dockerjava.api.DockerClient;

public interface DockerClientProvider {

    DockerClient getClient();

}
