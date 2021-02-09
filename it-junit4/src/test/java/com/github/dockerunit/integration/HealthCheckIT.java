package com.github.dockerunit.integration;

import com.github.dockerunit.core.Service;
import com.github.dockerunit.core.annotation.WithSvc;
import com.github.dockerunit.junit4.DockerUnitRule;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

@WithSvc(svc = TestAppDescriptor.class, containerNamePrefix = TestAppDescriptor.DOCKERUNIT_TEST_APP_SVC)
public class HealthCheckIT {

    @Rule
    public DockerUnitRule rule = new DockerUnitRule();

    private String baseUri;

    @Before
    public void setUp() {
        baseUri = Optional.ofNullable(DockerUnitRule.getDefaultServiceContext()
                .getService(TestAppDescriptor.DOCKERUNIT_TEST_APP_SVC))
                .map(Service::getInstances)
                .orElseThrow(() -> new RuntimeException("Could not find service."))
                .stream()
                .findAny()
                .map(si -> "http://" + si.getGatewayAddress() + ":" + si.getGatewayPort())
                .orElseThrow(() -> new RuntimeException("Could not find service instance."));
    }

    @Test
    public void healthCheckShouldReturn200() {
        RestAssured
                .given()
                    .baseUri(baseUri)
                    .basePath("/health-check")
                .when()
                    .get()
                .then()
                    .statusCode(200);
    }

}
