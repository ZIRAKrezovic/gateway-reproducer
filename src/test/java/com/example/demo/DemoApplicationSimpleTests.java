package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationSimpleTests {
    @Autowired
    private WebTestClient webClient;

    @Test
    void contextLoads() {
        webClient.get()
                .uri("/test")
                .header("X-Forwarded-For", "fd00:fefe:1::4, 192.168.0.1")
                // uncomment to compare
                // .header("Forwarded", "for=\"[fd00:fefe:1::4]\", for=192.168.0.1")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
