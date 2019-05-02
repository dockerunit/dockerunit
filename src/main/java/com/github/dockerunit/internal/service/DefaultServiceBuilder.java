package com.github.dockerunit.internal.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerunit.Service;
import com.github.dockerunit.ServiceInstance;
import com.github.dockerunit.ServiceInstance.Status;
import com.github.dockerunit.annotation.ContainerBuilder;
import com.github.dockerunit.annotation.ExtensionInterpreter;
import com.github.dockerunit.annotation.ExtensionMarker;
import com.github.dockerunit.annotation.Svc;
import com.github.dockerunit.exception.ContainerException;
import com.github.dockerunit.internal.ServiceBuilder;
import com.github.dockerunit.internal.ServiceDescriptor;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultServiceBuilder implements ServiceBuilder {

    private final static Logger logger = Logger.getLogger(DefaultServiceBuilder.class.getSimpleName());

    @Override
    public Service build(ServiceDescriptor descriptor, DockerClient client) {
        Set<ServiceInstance> instances = new HashSet<>();
        for (int i = 0; i < descriptor.getReplicas(); i++) {
            instances.add(createInstance(descriptor, client, i));
        }
        return new Service(descriptor.getName(), instances, descriptor);
    }

    private ServiceInstance createInstance(ServiceDescriptor descriptor, DockerClient client, int i) {
        String containerId = null;
        Status status = null;
        String statusDetails = null;
        CreateContainerCmd cmd = null;
        try {
            cmd = client.createContainerCmd(descriptor.getSvcDefinition().image());
            cmd = computeContainerName(descriptor, i, cmd);
            cmd = executeOptionBuilders(descriptor, cmd);
            if (descriptor.getCustomisationHook() != null) {
                cmd = executeCustomisationHook(descriptor.getCustomisationHook(), descriptor.getInstance(), cmd);
            }
            containerId = createAndStartContainer(cmd, descriptor.getSvcDefinition().pull(), client);
            status = Status.STARTED;
            statusDetails = "Started.";
        } catch (Throwable t) {
            if (t instanceof CompletionException) {
                if (t.getCause() != null && t.getCause() instanceof ContainerException) {
                    containerId = ((ContainerException) t.getCause()).getContainerId();
                    statusDetails = t.getCause().getCause() != null ? t.getCause().getCause().getMessage() : null;
                } else {
                    statusDetails = t.getCause() != null ? t.getCause().getMessage() : null;
                }
            } else {
                statusDetails = t.getMessage();
            }
            status = Status.ABORTED;
        }
        return ServiceInstance.builder()
                .containerName(cmd.getName())
                .containerId(containerId)
                .status(status)
                .statusDetails(statusDetails)
                .build();
    }

    private CreateContainerCmd computeContainerName(ServiceDescriptor dependency, int i, CreateContainerCmd cmd) {
        if (!dependency.getContainerName()
                .isEmpty()) {
            String name = dependency.getReplicas() > 1
                    ? dependency.getContainerName() + "-" + (i + 1)
                    : dependency.getContainerName();
            cmd = cmd.withName(name);
        }
        return cmd;
    }

    private String createAndStartContainer(CreateContainerCmd cmd, Svc.PullStrategy pullStrategy, DockerClient client) {
        CompletableFuture<String> respFut = new CompletableFuture<>();
        CompletableFuture<Void> pullFut;

        String imageName = computeImageName(cmd.getImage());

        Optional<Image> image = findImage(imageName, client);

        if (!image.isPresent() || pullStrategy.equals(Svc.PullStrategy.ALWAYS)) {
            pullFut = pullImage(imageName, client);
        } else {
            pullFut = CompletableFuture.completedFuture(null);
        }

        pullFut
                .exceptionally(ex -> {
                    String msg = String.format("An error occurred while pulling image %s - %s",
                            imageName,
                            ex.getMessage());
                    logger.warning(msg);
                    respFut.completeExceptionally(new RuntimeException(msg));
                    return null;
                }).thenRun(() -> {
            String containerId = startContainer(cmd, client);
            respFut.complete(containerId);
        })
                .exceptionally(ex -> {
                    respFut.completeExceptionally(ex.getCause());
                    return null;
                });
        respFut.exceptionally(ex -> {
            logger.severe("Cannot create container. Reason: " + ex.getMessage());
            return null;
        });
        return respFut.join();
    }

    private String computeImageName(String cmdImageName) {
        return Arrays.asList(cmdImageName).stream()
                .filter(s -> s.lastIndexOf("/") > s.lastIndexOf(":"))
                .findFirst()
                .map(s -> s += ":latest")
                .orElse(cmdImageName);
    }

    private Optional<Image> findImage(String imageName, DockerClient client) {
        ListImagesCmd imagesCmd = client.listImagesCmd().withImageNameFilter(imageName);
        List<Image> imagesList = imagesCmd.exec();
        if (imagesList == null || imagesList.isEmpty()) {
            return Optional.empty();
        }

        return imagesList.stream()
                .findFirst();
    }

    private CompletableFuture<Void> pullImage(String imageName, DockerClient client) {
        PullImageCmd pullImageCmd = client.pullImageCmd(imageName);
        CompletableFuture<Void> pullFut = new CompletableFuture<Void>();
        ResultCallback<PullResponseItem> resultCallback = new ResultCallback<PullResponseItem>() {

            private Closeable closeable;
            private DockerPullStatusManager manager = new DockerPullStatusManager(imageName);

            @Override
            public void close() throws IOException {
                try {
                    closeable.close();
                } catch (IOException e) {
                    throw new RuntimeException("Cannot close closeable " + closeable, e);
                }
            }

            @Override
            public void onStart(Closeable closeable) {
                this.closeable = closeable;
            }

            @Override
            public void onNext(PullResponseItem object) {
                System.out.print(manager.update(object));
            }

            @Override
            public void onError(Throwable throwable) {
                pullFut.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                pullFut.complete(null);
            }

        };
        pullImageCmd.exec(resultCallback);
        return pullFut;
    }

    private String startContainer(CreateContainerCmd cmd, DockerClient client) {
        CreateContainerResponse createResp = cmd.exec();
        StartContainerCmd startCmd = client.startContainerCmd(createResp.getId());
        try {
            startCmd.exec();
        } catch (Throwable t) {
            throw new ContainerException(createResp.getId(), t);
        }
        return startCmd.getContainerId();
    }

    private CreateContainerCmd executeCustomisationHook(Method customisationHook, Object instance,
            CreateContainerCmd cmd) {
        try {
            cmd = (CreateContainerCmd) customisationHook.invoke(instance, cmd);
        } catch (Exception e) {
            throw new RuntimeException(
                    "An error occurred while executing a method marked with @" + ContainerBuilder.class.getSimpleName()
                            + ", svcDefinition "
                            + customisationHook.getName() + " and declared in class " + instance.getClass()
                            .getName(),
                    e);
        }
        return cmd;
    }

    private CreateContainerCmd executeOptionBuilders(ServiceDescriptor descriptor, CreateContainerCmd cmd) {
        for (Annotation a : descriptor.getOptions()) {
            Class<? extends ExtensionInterpreter<?>> builderType = a.annotationType()
                    .getAnnotation(ExtensionMarker.class)
                    .value();
            ExtensionInterpreter<?> builder = null;
            Method buildMethod = null;
            try {
                builder = builderType.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Cannot instantiate " + ExtensionInterpreter.class.getSimpleName() + " of type "
                                + builderType.getSimpleName()
                                + " to handle annotation " + a.annotationType().getSimpleName()
                                + " that has been detected on class " + descriptor.getInstance()
                                .getClass()
                                .getName(),
                        e);
            }
            try {
                buildMethod = builderType.getDeclaredMethod("build",
                        new Class<?>[] { ServiceDescriptor.class, CreateContainerCmd.class, a.annotationType() });
                cmd = (CreateContainerCmd) buildMethod.invoke(builder, descriptor, cmd, a);
            } catch (Exception e) {
                throw new RuntimeException(
                        "An error occurred while invoking the build method on builder class " + builderType.getName(),
                        e);
            }
        }
        return cmd;
    }

    @Override
    public Service cleanup(Service s, DockerClient client) {
        return s.withInstances(s.getInstances().stream()
                .map(si -> destroyInstance(si, client))
                .collect(Collectors.toSet()));
    }

    private ServiceInstance destroyInstance(ServiceInstance i, DockerClient client) {
        if (i.getContainerId() != null) {
            RemoveContainerCmd cmd = client.removeContainerCmd(i.getContainerId()).withForce(true);
            try {
                cmd.exec();
                return i.withStatus(Status.TERMINATED);
            } catch (NotFoundException e) {
                logger.warning("No container with id " + i.getContainerId() + " found");
                return i.withStatus(Status.TERMINATION_FAILED)
                        .withStatusDetails(e.getMessage());
            }
        } else {
            return i.withStatus(Status.TERMINATION_FAILED)
                    .withStatusDetails("No container id found.");
        }

    }

}
