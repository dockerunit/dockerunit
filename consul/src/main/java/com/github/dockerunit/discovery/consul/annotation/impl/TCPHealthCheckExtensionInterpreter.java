package com.github.dockerunit.discovery.consul.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.core.annotation.ExtensionInterpreter;
import com.github.dockerunit.core.internal.ServiceDescriptor;
import com.github.dockerunit.discovery.consul.annotation.TCPHealthCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCPHealthCheckExtensionInterpreter implements ExtensionInterpreter<TCPHealthCheck> {

	private static final String SERVICE_NAME_SUFFIX = "_NAME";
	private static final String SERVICE_PREFIX = "SERVICE_";
	private static final String SERVICE_CHECK_INTERVAL = SERVICE_PREFIX + "CHECK_INTERVAL";
	private static final String SERVICE_CHECK_INITIAL_STATUS = SERVICE_PREFIX + "CHECK_INITIAL_STATUS";
	private static final String SERVICE_CHECK_TCP = SERVICE_PREFIX + "CHECK_TCP";


	@Override
	public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, TCPHealthCheck ec) {
		final String serviceNameEnv = SERVICE_PREFIX + ec.port() + SERVICE_NAME_SUFFIX + "=" + sd.getSvcName();
		final String serviceCheckIntervalEnv = SERVICE_CHECK_INTERVAL + "=" + ec.pollingInterval() + "s";
		final String serviceInitialStatusEnv = SERVICE_CHECK_INITIAL_STATUS + "=" + ec.checkInitialStatus().toString().toLowerCase();
		final String serviceCheckTCP = SERVICE_CHECK_TCP + "=$SERVICE_IP" + ":" + ec.port();
		
		List<String> finalEnv = new ArrayList<>();
		List<String> enableConsulEnv = Arrays.asList(serviceNameEnv, serviceCheckIntervalEnv, serviceInitialStatusEnv, serviceCheckTCP);
		finalEnv.addAll(enableConsulEnv);

		String[] env = cmd.getEnv();
		
		if(env != null) {
			finalEnv.addAll(Arrays.asList(env));
		}
		return cmd.withEnv(finalEnv);
	}

}
