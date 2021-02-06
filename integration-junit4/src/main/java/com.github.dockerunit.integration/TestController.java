package com.github.dockerunit.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@RestController
public class TestController {

    public static final String DOCKERUNIT_GREETING_ENV = "DOCKERUNIT_GREETING";
    public static final String DOCKERUNIT_GREETING_DEFAULT = "Hello Dockerunit!";
    public static final String DOCKERUNIT_CONFIG_GREETING_EXPR = "${dockerunit.greeting}";

    @Value(DOCKERUNIT_CONFIG_GREETING_EXPR)
    private String configGreeting;

    @Autowired
    private RestTemplate rest;

    private String greeting = DOCKERUNIT_GREETING_DEFAULT;

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/greeting")
    public ResponseEntity<String> greeting() {
        return ResponseEntity.ok(greeting);
    }

    @PutMapping("/greeting/{value}")
    public ResponseEntity<Void> setGreeting(@PathVariable("value") String newGreeting) {
        this.greeting = newGreeting;
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/env-greeting")
    public ResponseEntity<String> envGreeting() {
        String greeting = Optional.ofNullable(System.getenv(DOCKERUNIT_GREETING_ENV))
                .orElse(DOCKERUNIT_GREETING_DEFAULT);
        return ResponseEntity.ok(greeting);
    }

    @GetMapping("/volume-greeting")
    public ResponseEntity<String> volumeGreeting() {
        String greeting = Optional.ofNullable(configGreeting).orElse(DOCKERUNIT_GREETING_DEFAULT);
        return ResponseEntity.ok(greeting);
    }

    @PostMapping("/requests")
    public void proxyRequest(@RequestBody RequestDTO dto, HttpServletResponse response)
            throws URISyntaxException, IOException {
        ClientHttpResponse remoteResponse = rest.execute(new URI(dto.getUrl()), dto.getMethod(), req -> {
           if (dto.getBody() != null) {
               OutputStream reqBody = req.getBody();
               reqBody.write(dto.getBody());
           }
        }, res -> {
            return res;
        });
        InputStream body = remoteResponse.getBody();
        if (body != null) {
            byte[] chunk = new byte[1024];
            int read = body.read(chunk);
            ServletOutputStream outputStream = response.getOutputStream();
            while (read != -1) {
                outputStream.write(chunk, 0, read);
                read = body.read(chunk);
            }
            response.flushBuffer();
        }
        response.setStatus(remoteResponse.getRawStatusCode());
        remoteResponse.getHeaders()
                .toSingleValueMap()
                .entrySet()
                .forEach(e -> response.setHeader(e.getKey(), e.getValue()));
    }

}
