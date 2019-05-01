package com.github.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.dockerunit.annotation.impl.PublishPortExtensionInterpreter;

/**
 * Equivalent of {@literal -p} option in docker run.
 * Exposes a container port on the host network interface.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(RepeatablePublishPort.class)
@ExtensionMarker(PublishPortExtensionInterpreter.class)
public @interface PublishPort {

    /**
     * @return the container port
     */
    int container();

    /**
     * @return the host port where the container port is mapped
     */
    int host();

    /**
     * @return the transport protocol. Default is TCP
     */
    Protocol protocol() default Protocol.TCP;

    /**
     * @return a specific host network interface ip.
     */
    String hostIp() default "";

    public static enum Protocol {
        TCP, UDP;
    }

}
