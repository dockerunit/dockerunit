package com.github.dockerunit.core.internal.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DefaultDockerClientProvider implements DockerClientProvider {

    private static final String DOCKER_SERVER_URL = "docker.server.url";
    private static final String DOCKER_SOCKET = "unix:///var/run/docker.sock";
    private final DockerClient client;

    public DefaultDockerClientProvider() {
        String dockerServerUrl = System.getProperty(DOCKER_SERVER_URL, DOCKER_SOCKET);
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerServerUrl)
                .withApiVersion("1.40")
                .build();
        client = DockerClientBuilder.getInstance(config).build();
    }

    @Override
    public DockerClient getClient() {
        return client;
    }

}
