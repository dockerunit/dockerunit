package com.github.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.dockerunit.Service;

/**
 * Sets the name for this {@linkplain Service}
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Named {

    String value();

}
