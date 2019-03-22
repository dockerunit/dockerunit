package com.github.dockerunit.internal.docker;

public class DockerClientProviderFactory {

    private static final DefaultDockerClientProvider INSTANCE = new DefaultDockerClientProvider();

    public static DockerClientProvider create() {
        return INSTANCE;
    }

}
