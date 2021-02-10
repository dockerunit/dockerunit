package com.github.dockerunit.integration;

import com.github.dockerunit.core.annotation.PublishPorts;
import com.github.dockerunit.core.annotation.Svc;
import com.github.dockerunit.discovery.consul.annotation.WebHealthCheck;

@Svc(name = TestAppDescriptor.DOCKERUNIT_TEST_APP_SVC, image = TestAppDescriptor.DOCKERUNIT_TEST_APP_IMAGE)
@PublishPorts
@WebHealthCheck(port = 8080)
public class TestAppDescriptor {
    public static final String DOCKERUNIT_TEST_APP_SVC = "dockerunit-test-app";
    public static final String DOCKERUNIT_TEST_APP_IMAGE = "dockerunit-test-app:latest";
}
