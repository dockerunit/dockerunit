package com.github.dockerunit.discovery.consul;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class ConsulRegistrator {

    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    private static final Logger logger = Logger.getLogger(ConsulRegistrator.class.getSimpleName());

    private final DockerClient client;
    private final Map<String, ContainerTracker> trackers = new ConcurrentHashMap<>();
    private final Map<String, ConsulService> services = new ConcurrentHashMap<>();

    private final int pollingPeriod;
    private final HttpClient httpClient;
    private final String host;
    private final int port;
    private final ObjectWriter objectWriter;
    private final ConsulServiceFactory svcFactory;

    public ConsulRegistrator(DockerClient client, int pollingPeriod, String consulHost, int consulPort) {
        this.client = client;
        this.pollingPeriod = pollingPeriod;
        this.host = consulHost;
        this.port = consulPort;
        httpClient = HttpClientBuilder.create().build();
        objectWriter = new ObjectMapper().writerFor(ConsulService.class);
        svcFactory = new ConsulServiceFactory(client);
    }

    public void trackContainer(String containerId) {
        Optional<ContainerTracker> tracker = Optional.ofNullable(trackers.get(containerId));
        trackers.put(containerId, tracker.orElse(
                new ContainerTracker(client, containerId, pollingPeriod, this::onDetectHook, this::onDestroyHook)));
    }

    private void onDetectHook(Container container) {
        ConsulService svc = svcFactory.createSvc(container.getId());
        registerSvc(svc);
        services.put(container.getId(), svc);
    }

    private void onDestroyHook(Container container) {
        if (container != null && services.containsKey(container.getId())) {
            deregisterSvc(services.get(container.getId()));
            trackers.remove(container.getId());
            services.remove(container.getId());
        }
    }

    private void registerSvc(ConsulService svc) {
        Supplier<String> errorMessage = () -> "Could not register container " + svc.getContainerId() + " on Consul.";
        try {
            executePut("/v1/agent/service/register", objectWriter.writeValueAsString(svc),
                    errorMessage,
                    ex -> {
                        throw new RuntimeException(errorMessage.get(), ex);
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(errorMessage.get(), e);
        }
    }

    private void deregisterSvc(ConsulService svc) {
        Supplier<String> errorMessage = () -> "Could not deregister container " + svc.getContainerId()
                + " from Consul.";
        executePut("/v1/agent/service/deregister/" + svc.getId(), null,
                errorMessage,
                ex -> logger.info("Consul has already stopped. Service de-registration aborted."));
    }

    private void executePut(String endpoint,
            String body,
            Supplier<String> errorMessage,
            Consumer<Exception> onFailure) {
        HttpPut put = new HttpPut("http://" + host + ":" + port + endpoint);
        put.setHeader(ACCEPT, APPLICATION_JSON);
        put.setHeader(CONTENT_TYPE, APPLICATION_JSON);

        if (body != null) {
            try {
                put.setEntity(new StringEntity(body));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(errorMessage.get(), e);
            }
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(put);
        } catch (Exception e) {
            onFailure.accept(e);
        }

        if (response != null) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException(errorMessage.get() + " Unexpected status code " + statusCode);
            }
        }
    }

}
