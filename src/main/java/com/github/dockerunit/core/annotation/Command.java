package com.github.dockerunit.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.dockerunit.core.annotation.impl.CommandExtensionInterpreter;

/**
 * Provides a command that should be executed when running the container.
 * Equivalent to providing a command after
 *
 * <pre>
 * docker run image_name
 * </pre>
 */
@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(CommandExtensionInterpreter.class)
public @interface Command {

    String[] value();

}
