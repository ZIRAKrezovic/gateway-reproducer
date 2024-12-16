package com.example.demo;

import com.github.dockerjava.api.command.CreateNetworkCmd;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;

@TestConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class DemoApplicationTests {
    static Consumer<CreateNetworkCmd> createNetworkCmd;

    static {
        System.setProperty("java.net.preferIPv6Addresses", "true");
        var ipam = new com.github.dockerjava.api.model.Network.Ipam();
        var ipamConfig = new com.github.dockerjava.api.model.Network.Ipam.Config();
        ipamConfig.withSubnet("fd00:fefe:1::/48");
        ipam.withConfig(ipamConfig);
        createNetworkCmd = cmd -> cmd.withIpam(ipam);
    }

    @Container
    static Network network = Network.builder().enableIpv6(true).createNetworkCmdModifier(createNetworkCmd).build();

    @Container
    static GenericContainer<?> alpine = new GenericContainer<>("alpine:latest")
            .withNetwork(network)
            .withCommand("/bin/sh", "-c", "apk add curl && sleep infinity");

    @Container
    static GenericContainer<?> haproxy1 = new GenericContainer<>("haproxy:alpine")
            .withNetwork(network)
            .withNetworkAliases("haproxy")
            .withClasspathResourceMapping("haproxy1.cfg", "/usr/local/etc/haproxy/haproxy.cfg", BindMode.READ_ONLY);

    @Container
    static GenericContainer<?> haproxy2 = new GenericContainer<>("haproxy:alpine")
            .withNetwork(network)
            .withNetworkAliases("haproxy-forward")
            // uncomment on linux
            // .withExtraHost("host.docker.internal", "host-gateway")
            .withClasspathResourceMapping("haproxy2.cfg", "/usr/local/etc/haproxy/haproxy.cfg", BindMode.READ_ONLY);

    @BeforeAll
    static void setup() throws Exception {
        haproxy2.start();
        haproxy1.start();
        alpine.start();
    }

    @Test
    @Disabled
    void contextLoads() throws Exception {
        await().atMost(Duration.ofSeconds(30)).until(() -> haproxy1.getLogs().contains("web_servers/s1 is UP"));
        await().atMost(Duration.ofSeconds(30)).until(() -> haproxy2.getLogs().contains("web_servers/s1 is UP"));

        // without gateway
        //var out = alpine.execInContainer("/bin/sh", "-c", "curl -v6 http://haproxy:8080/test").getStdout();

        // with gateway
        var out = alpine.execInContainer("/bin/sh", "-c", "curl -v6 http://haproxy:8080/testgateway").getStdout();

        Assertions.assertTrue(out.contains("Hello World"));
    }
}
