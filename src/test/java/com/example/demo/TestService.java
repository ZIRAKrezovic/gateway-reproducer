package com.example.demo;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class TestService {
    @GetMapping("/test")
    public String test(ServerWebExchange exchange) {
        var request = exchange.getRequest();
        return request.getRemoteAddress().getHostName();
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
      return builder.routes()
              .route("test", r -> r.path("/testgateway")
                      .filters(f -> f.rewritePath("/testgateway", "/test"))
                      .uri("http://127.0.0.1:8080/test"))
              .build();
    }
}