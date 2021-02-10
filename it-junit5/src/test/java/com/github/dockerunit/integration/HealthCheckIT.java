package com.github.dockerunit.integration;

import java.util.Optional;

import com.github.dockerunit.core.Service;
import com.github.dockerunit.core.annotation.WithSvc;
import com.github.dockerunit.junit5.DockerUnitExtension;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@WithSvc(svc = TestAppDescriptor.class, containerNamePrefix = TestAppDescriptor.DOCKERUNIT_TEST_APP_SVC)
@ExtendWith(DockerUnitExtension.class)
public class HealthCheckIT {

    private String baseUri;

    @BeforeEach
    public void setUp() {
        baseUri = Optional.ofNullable(DockerUnitExtension.getDefaultServiceContext()
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
