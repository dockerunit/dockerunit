package com.github.dockerunit.discovery.consul.annotation;

import com.github.dockerunit.core.annotation.ExtensionMarker;
import com.github.dockerunit.discovery.consul.annotation.impl.WebHealthCheckExtensionInterpreter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Allows the configuration of a health check endpoint so that Consul
 * can verify the state of each of the svc replicas.
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtensionMarker(WebHealthCheckExtensionInterpreter.class)
public @interface WebHealthCheck {

	/**
	 * The path of the health check endpoint in your svc.
	 * Default is {@literal /health-check}
	 * 
	 * @return the health check endpoint
	 */
	String endpoint() default "/health-check";
	
	/**
	 * The web protocol. Default is HTTP.
	 * HTTP and HTTPS are supported 
	 * 
	 * @return the web protocol
	 */
	WebProtocol protocol() default WebProtocol.HTTP;
	
	/**
	 * The port that is exposed by the container (not the one it is mapped to on the host network interface)
	 * Default is 80
	 * 
	 * @return the port number
	 */
	int port() default 80;
	
	/**
	 * The length of the interval (in seconds) Consul will wait before re-checking the svc state.
	 * Default is 1 second.
	 * 
	 * @return the interval in seconds
	 */
	int pollingInterval() default 1;
	
	public static enum WebProtocol {
		HTTP, HTTPS
	}
}
