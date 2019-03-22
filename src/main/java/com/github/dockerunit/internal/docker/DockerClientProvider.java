package com.github.dockerunit.internal.docker;

import com.github.dockerjava.api.DockerClient;

public interface DockerClientProvider {

    DockerClient getClient();

}
