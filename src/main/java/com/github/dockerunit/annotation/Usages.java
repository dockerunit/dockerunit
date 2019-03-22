package com.github.dockerunit.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Wrapper annotation for repeated use of {@linkplain Use}
 * on a class or a method
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface Usages {

    Use[] value();

}
