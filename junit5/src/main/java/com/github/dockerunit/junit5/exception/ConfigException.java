package com.github.dockerunit.junit5.exception;

public class ConfigException extends RuntimeException {

    private static final long serialVersionUID = 8021806700322024225L;

    public ConfigException(String message) {
        super(message);
    }

}
