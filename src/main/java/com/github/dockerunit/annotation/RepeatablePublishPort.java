package com.github.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.dockerunit.annotation.impl.RepeatablePublishPortExtensionInterpreter;

/**
 * Wrapper annotation to allow multiple declarations of {@linkplain PublishPort}
 */
@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(RepeatablePublishPortExtensionInterpreter.class)
public @interface RepeatablePublishPort {

    PublishPort[] value();

}
