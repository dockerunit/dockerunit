package com.github.dockerunit.discovery.consul;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerunit.discovery.consul.annotation.TCPHealthCheck;
import com.github.dockerunit.discovery.consul.annotation.WebHealthCheck;

public class ConsulServiceFactory {

    private static final String SERVICE_NAME_PREFIX = "SERVICE_";
    private static final String SERVICE_NAME_SUFFIX = "_NAME";
    private static final String DEFINE_HEALTH_CHECK_MSG = "Make sure you have defined a "
            + "health-check by using @" + WebHealthCheck.class.getSimpleName()
            + " or @" + TCPHealthCheck.class.getSimpleName();

    private static final String SVC_HEALTH_CHECK_PORT_NOT_FOUND_MSG =
            "No health-check port definition detected" + DEFINE_HEALTH_CHECK_MSG;
    private static final String SVC_NAME_NOT_FOUND_ERROR_MSG = "No svc name detected. " + DEFINE_HEALTH_CHECK_MSG;

    private static final String DOCKERUNIT = "dockerunit";
    private static final String SVC_ADDRESS_NOT_FOUND_ERROR_MSG = "No svc address detected. The container might not be running due an internal error.";
    private static final String SERVICE_CHECK_INTERVAL = "SERVICE_CHECK_INTERVAL";
    private static final String SERVICE_CHECK_HTTP = "SERVICE_CHECK_HTTP";
    private static final String SERVICE_CHECK_METHOD = "SERVICE_CHECK_METHOD";
    private static final String SERVICE_CHECK_INITIAL_STATUS = "SERVICE_CHECK_INITIAL_STATUS";
    private static final String SERVICE_CHECK_TCP = "SERVICE_CHECK_TCP";

    private final DockerClient client;

    public ConsulServiceFactory(DockerClient client) {
        this.client = client;
    }

    public ConsulService createSvc(String containerId) {
        InspectContainerResponse r = client.inspectContainerCmd(containerId).exec();

        Map<String, String> options = buildKeyValueMap(r.getConfig().getLabels(), r.getConfig().getEnv());

        String svcName;
        try {
            svcName = URLEncoder.encode(
                    findName(options).orElseThrow(() -> new RuntimeException(SVC_NAME_NOT_FOUND_ERROR_MSG)),
                    StandardCharsets.UTF_8.name()
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Invalid svc name detected.", e);
        }

        Integer port = extractHealthCheckPort(options)
                .orElseThrow(() -> new RuntimeException(SVC_HEALTH_CHECK_PORT_NOT_FOUND_MSG));
        String address = ContainerUtils.extractBridgeIpAddress(r.getNetworkSettings())
                .orElseThrow(() -> new RuntimeException(SVC_ADDRESS_NOT_FOUND_ERROR_MSG));

        return ConsulService.builder()
                .containerId(containerId)
                .name(svcName)
                .id(DOCKERUNIT + ":" + svcName + ":" + containerId)
                .address(address)
                .port(port)
                .check(buildCheck(options, address, port))
                .build();
    }

    private Map<String, String> buildKeyValueMap(Map<String, String> labels, String[] env) {
        if (env == null) {
            return labels != null ? labels : new HashMap<>();
        }
        if (labels == null) {
            return new HashMap<>();
        }
        Map<String, String> keyValueMap = labels.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Arrays.stream(env)
                .filter(s -> s.contains("="))
                .map(s -> s.split("="))
                .forEach(kv -> keyValueMap.put(kv[0], kv[1]));
        return keyValueMap;
    }

    private ConsulService.ConsulCheck buildCheck(Map<String, String> options, String address, Integer port) {
        ConsulService.ConsulCheck.ConsulCheckBuilder builder = ConsulService.ConsulCheck.builder();
        builder.interval(options.get(SERVICE_CHECK_INTERVAL));
        builder.http(interpolateCheckScript(
                options.getOrDefault(SERVICE_CHECK_HTTP, null), address, port)
        );
        builder.tcp(interpolateCheckScript(options.getOrDefault(SERVICE_CHECK_TCP, null), address, port));
        builder.method(options.getOrDefault(SERVICE_CHECK_METHOD, null));
        builder.tlsSkipVerify(true);
        builder.status(options.getOrDefault(SERVICE_CHECK_INITIAL_STATUS, null));

        return builder.build();
    }

    private String interpolateCheckScript(String script, String address, Integer port) {
        return script != null ? script.replaceAll("\\$SERVICE_IP", address)
                .replaceAll("\\$SERVICE_PORT", port.toString()) : null;
    }

    private Optional<Integer> extractHealthCheckPort(Map<String, String> options) {
        return options.entrySet().stream()
                .filter(this::hasServiceName)
                .findFirst()
                .map(kv -> extractPortString(kv.getKey()))
                .map(this::asInteger);
    }

    private Integer asInteger(String port) {
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private Optional<String> findName(Map<String, String> options) {
        return options.entrySet().stream()
                .filter(this::hasServiceName)
                .findFirst()
                .map(Map.Entry::getValue);
    }

    private boolean hasServiceName(Map.Entry<String, String> kv) {
        return kv.getKey().startsWith(SERVICE_NAME_PREFIX)
                && kv.getKey().endsWith(SERVICE_NAME_SUFFIX);
    }

    private String extractPortString(String s) {
        if (!(s.lastIndexOf("_") > (s.indexOf("_") + 1))) {
            return null;
        }
        return s.substring(s.indexOf("_") + 1, s.lastIndexOf("_"));
    }

}
