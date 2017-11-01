package com.github.qzagarese.dockerunit.internal.service;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.annotation.ContainerBuilder;
import com.github.qzagarese.dockerunit.annotation.OptionBuilder;
import com.github.qzagarese.dockerunit.annotation.OptionHandler;
import com.github.qzagarese.dockerunit.annotation.Image.PullStrategy;
import com.github.qzagarese.dockerunit.exception.ContainerException;
import com.github.qzagarese.dockerunit.internal.ServiceBuilder;
import com.github.qzagarese.dockerunit.internal.TestDependency;

public class DefaultServiceBuilder implements ServiceBuilder {

	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
	
    @Override
    public Service build(TestDependency dependency, DockerClient client) {
        Set<ServiceInstance> instances = new HashSet<>();
        for (int i = 0; i < dependency.getReplicas(); i++) {
            instances.add(createInstance(dependency, client, i));
        }
        return new Service(dependency.getNamed()
            .value(), instances);
    }

    private ServiceInstance createInstance(TestDependency dependency, DockerClient client, int i) {
        CreateContainerCmd cmd = client.createContainerCmd(dependency.getImage().value());
        cmd = computeContainerName(dependency, i, cmd);
        cmd = executeOptionBuilders(dependency, cmd);
        if (dependency.getCustomisationHook() != null) {
            cmd = executeCustomisationHook(dependency.getCustomisationHook(), dependency.getInstance(), cmd);
        }
        String containerId = null;
        Status status = null;
        String statusDetails = null;
		try {
			containerId = createAndStartContainer(cmd, dependency.getImage().pull(),  client);
			status = Status.STARTED;
			statusDetails = "Started.";
		} catch (Throwable t) {
			if(t instanceof CompletionException) {
				if(t.getCause() != null && t.getCause() instanceof ContainerException) {
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

	private CreateContainerCmd computeContainerName(TestDependency dependency, int i, CreateContainerCmd cmd) {
		if (!dependency.getContainerName()
            .isEmpty()) {
            String name = dependency.getReplicas() > 1 
            		? dependency.getContainerName() +  "-" + (i + 1)  
            		: dependency.getContainerName();
            cmd = cmd.withName(name);
        }
		return cmd;
	}

	private String createAndStartContainer(CreateContainerCmd cmd, PullStrategy pullStrategy, DockerClient client) {
		CompletableFuture<String> respFut = new CompletableFuture<>();
		ListImagesCmd imagesCmd = client.listImagesCmd().withImageNameFilter(cmd.getImage());
		List<Image> imagesList = imagesCmd.exec();
		boolean imageAbsent = imagesList == null || imagesList.size() == 0;
		CompletableFuture<Void> pullFut;
		if(imageAbsent || pullStrategy.equals(PullStrategy.ALWAYS)) {
			pullFut = pullImage(cmd, client);
		} else {
			pullFut = CompletableFuture.completedFuture(null);
		}
		
		pullFut
			.exceptionally(ex -> {
				logger.warning("An error occurred while executing a docker pull operation: " + ex.getMessage());
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

	private CompletableFuture<Void> pullImage(CreateContainerCmd cmd, DockerClient client) {
		PullImageCmd pullImageCmd = client.pullImageCmd(cmd.getImage());
		CompletableFuture<Void> pullFut = new CompletableFuture<Void>();
		ResultCallback<PullResponseItem> resultCallback = new ResultCallback<PullResponseItem>() {

			private Closeable closeable;
			
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
				if(object.getId() != null) {
					logger.info("Pulling image " + object.getId() + "...");
				}
			}

			@Override
			public void onError(Throwable throwable) {
				pullFut.completeExceptionally(
						new RuntimeException("Failed pulling image " + cmd.getImage(), throwable));
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
                "An error occurred while executing a method marked with @" + ContainerBuilder.class.getSimpleName() + ", named "
                                       + customisationHook.getName() + " and declared in class " + instance.getClass()
                                           .getName(),
                e);
        }
        return cmd;
    }

    private CreateContainerCmd executeOptionBuilders(TestDependency dependency, CreateContainerCmd cmd) {
        for (Annotation a : dependency.getOptions()) {
            Class<? extends OptionBuilder<?>> builderType = a.annotationType().getAnnotation(OptionHandler.class)
                .value();
            OptionBuilder<?> builder = null;
            Method buildMethod = null;
            try {
                builder = builderType.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate " + OptionBuilder.class.getSimpleName() + " of type " + builderType.getSimpleName()
                                           + " to handle annotation " + a.annotationType().getSimpleName()
                                           + " that has been detected on class " + dependency.getInstance()
                                               .getClass()
                                               .getName(),
                    e);
            }
            try {
            	buildMethod = builderType.getDeclaredMethod("build", new Class<?>[] { CreateContainerCmd.class, a.annotationType() });
                cmd = (CreateContainerCmd) buildMethod.invoke(builder, cmd, a);
            } catch (Exception e) {
                throw new RuntimeException(
                    "An error occurred while invoking the build method on builder class " + builderType.getName(), e);
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
		if(i.getContainerId() != null) {
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
