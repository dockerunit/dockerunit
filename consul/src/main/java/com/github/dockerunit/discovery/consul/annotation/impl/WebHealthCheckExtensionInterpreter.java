package com.github.dockerunit.discovery.consul.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.core.annotation.ExtensionInterpreter;
import com.github.dockerunit.core.internal.ServiceDescriptor;
import com.github.dockerunit.discovery.consul.annotation.WebHealthCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebHealthCheckExtensionInterpreter implements ExtensionInterpreter<WebHealthCheck> {

	private static final String SERVICE_NAME_SUFFIX = "_NAME";
	private static final String SERVICE_PREFIX = "SERVICE_";
	private static final String SERVICE_CHECK_HTTP = SERVICE_PREFIX + "CHECK_HTTP";
	private static final String SERVICE_CHECK_INTERVAL = SERVICE_PREFIX + "CHECK_INTERVAL";
	private static final String SERVICE_CHECK_METHOD = "SERVICE_CHECK_METHOD";

	@Override
	public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, WebHealthCheck whc) {
		final String serviceNameEnv = SERVICE_PREFIX + whc.port() + SERVICE_NAME_SUFFIX + "=" + sd.getSvcName();
		final String serviceCheckIntervalEnv = SERVICE_CHECK_INTERVAL + "=" + whc.pollingInterval() + "s";
		final String serviceCheckEnv = SERVICE_CHECK_HTTP + "=" +whc.protocol().toString().toLowerCase()
				+ "://$SERVICE_IP:$SERVICE_PORT"
				+ whc.endpoint();
		final String serviceCheckMethodEnv = SERVICE_CHECK_METHOD + "=" + "GET";

		List<String> finalEnv = new ArrayList<>();
		List<String> healthCheckEnv = Arrays.asList(serviceNameEnv, serviceCheckEnv, serviceCheckIntervalEnv);
		finalEnv.addAll(healthCheckEnv);

		String[] env = cmd.getEnv();
		
		if(env != null) {
			finalEnv.addAll(Arrays.asList(env));
		}
		return cmd.withEnv(finalEnv);
	}

}
