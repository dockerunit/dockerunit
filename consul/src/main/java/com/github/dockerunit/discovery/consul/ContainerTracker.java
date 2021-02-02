package com.github.dockerunit.discovery.consul;

import java.util.Collections;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

public class ContainerTracker {

    private static final Logger logger = Logger.getLogger(ContainerTracker.class.getSimpleName());

    private final DockerClient client;
    private final String containerId;
    private final int pollingPeriod;

    private final Consumer<Container> onDetect;
    private final Consumer<Container> onDestroy;

    private Container latestContainer;

    public ContainerTracker(
            DockerClient client,
            String containerId,
            int pollingPeriod,
            Consumer<Container> onDetect,
            Consumer<Container> onDestroy
    ) {
        this.client = client;
        this.containerId = containerId;
        this.pollingPeriod = pollingPeriod;
        this.onDetect = onDetect;
        this.onDestroy = onDestroy;
        init();
    }

    public ContainerTracker(DockerClient client, String containerId, int pollingPeriod) {
        this(client, containerId, pollingPeriod,
                c -> logger.info("Detected container with id: " + c.getId()),
                c -> logger.info("Container with id " + c.getId() + " has been destroyed."));
    }

    private void init() {
        latestContainer = findContainer().orElseThrow(() -> new RuntimeException(
                "Could not detect container with id: " + containerId));
        onDetect.accept(latestContainer);
        track();
    }

    private Optional<Container> findContainer() {
        return client.listContainersCmd().withIdFilter(Collections.singletonList(containerId)).exec()
                .stream()
                .findFirst();
    }

    private void track() {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                try {
                    latestContainer = findContainer()
                            .orElseThrow(() -> new RuntimeException("Container " + containerId + " has been removed."));
                } catch (Exception e) {
                    this.cancel();
                    onDestroy.accept(latestContainer);
                }
            }
        };
        Timer timer = new Timer("Container " + containerId + " monitor");
        timer.scheduleAtFixedRate(repeatedTask, 0, pollingPeriod * 1000);
    }

}
