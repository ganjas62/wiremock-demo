package com.example.wiremockdemo.controller;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class DemoControllerTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
      .options(new WireMockConfiguration().port(8081).notifier(new Slf4jNotifier(true)))
      .build();

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testDemo() throws Exception {
    WireMock wireMock = wireMockExtension.getRuntimeInfo().getWireMock();
    wireMock.register(WireMock.get("/wiremock")
        .willReturn(ResponseDefinitionBuilder.okForJson("{\"value\": true}")));
    mockMvc.perform(MockMvcRequestBuilders.get("/demo"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.value", Matchers.is(true)));
    wireMock.verifyThat(WireMock.getRequestedFor(WireMock.urlEqualTo("/wiremock")));
  }

  @Test
  public void testPostRequest() throws Exception {
    WireMock wireMock = wireMockExtension.getRuntimeInfo().getWireMock();
    wireMock.register(WireMock.post("/wiremock")
        .atPriority(1)
        .withHeader("customHeader", WireMock.equalTo("headerValue"))
        .withRequestBody(WireMock.matchingJsonPath("$.bodyKey", WireMock.matching("^\\w+$")))
        .willReturn(ResponseDefinitionBuilder.okForJson("{\"value\": true}")));
    wireMock.register(WireMock.post("/wiremock")
        .atPriority(2)
        .willReturn(WireMock.aResponse().withStatus(500)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody("{\"value\": false}")));
    mockMvc.perform(MockMvcRequestBuilders.post("/demo")
            .header("customHeader", "headerValue")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bodyKey\": \"value\"}"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.value", Matchers.is(true)));
    mockMvc.perform(MockMvcRequestBuilders.post("/demo")
            .header("customHeader", "wrongHeader")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bodyKey\": \"value\"}"))
        .andExpect(MockMvcResultMatchers.status().is(500))
        .andExpect(MockMvcResultMatchers.jsonPath("$.value", Matchers.is(false)));
    mockMvc.perform(MockMvcRequestBuilders.post("/demo")
            .header("customHeader", "headerValue")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bodyKey\": \"!!\"}"))
        .andExpect(MockMvcResultMatchers.status().is(500))
        .andExpect(MockMvcResultMatchers.jsonPath("$.value", Matchers.is(false)));
    wireMock.verifyThat(3, WireMock.postRequestedFor(WireMock.urlEqualTo("/wiremock")));
  }
}
