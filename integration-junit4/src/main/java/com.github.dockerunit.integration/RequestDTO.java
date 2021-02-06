package com.github.dockerunit.integration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Getter
@Setter
@NoArgsConstructor
public class RequestDTO {

    private HttpMethod method;
    private String url;
    private byte[] body;

}
