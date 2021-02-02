package com.github.dockerunit.discovery.consul.annotation;

import com.github.dockerunit.core.annotation.ExtensionMarker;
import com.github.dockerunit.discovery.consul.annotation.impl.TCPHealthCheckExtensionInterpreter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 
 * Allows Consul to probe each replica of your svc. This enables basic
 * discovery by telling Consul about your svc. If
 * you are creating an HTTP/HTTPS svc, you should expose a health-check
 * endpoint and use {@linkplain WebHealthCheck}
 * 
 */
@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(TCPHealthCheckExtensionInterpreter.class)
public @interface TCPHealthCheck {

	/**
	 * The port that is exposed by the container (not the one it is mapped to on the
	 * host network interface) Default is 80
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

	
	/**
	 * The amount of time to wait before Consul performs the first health check.
	 * Default is 0.
	 * 
	 * @return the delay in seconds
	 */
	int initialDelay() default 0;
	
	
	/**
	 * The initial status for the Consul check associated to this svc. Default is PASSING.
	 * 
	 * @return the status of the check
	 */
	CheckStatus checkInitialStatus() default CheckStatus.PASSING;

    
    public enum CheckStatus {
        PASSING, CRITICAL
    }
    
}
