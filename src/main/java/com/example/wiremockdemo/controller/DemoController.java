package com.example.wiremockdemo.controller;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RestController
public class DemoController {

  private final RestTemplate restTemplate = new RestTemplate();

  @RequestMapping(path = "/demo")
  public ResponseEntity<Object> proxyResponse(RequestEntity<Object> requestEntity) {
    try {
      return restTemplate.exchange("http://localhost:8081/wiremock", requestEntity.getMethod(),
          requestEntity, Object.class);
    } catch (HttpStatusCodeException e) {
      return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
          .body(e.getResponseBodyAsString());
    }
  }
}
