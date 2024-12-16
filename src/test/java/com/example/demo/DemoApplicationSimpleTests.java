package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class DemoApplicationSimpleTests {
    @Autowired
    private WebTestClient webClient;

    @Test
    void contextLoads() {
        webClient.get()
                // working url, bypasses gateway
                //.uri("/test")
                .uri("/testgateway")
                .header("X-Forwarded-For", "fd00:fefe:1::4")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
