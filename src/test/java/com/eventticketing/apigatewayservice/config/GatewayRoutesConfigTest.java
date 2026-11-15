package com.eventticketing.apigatewayservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutesConfigTest {

    @Autowired
    private RouteLocatorBuilder routeLocatorBuilder;

    @Test
    void definesExpectedRouteIdsAndDestinations() {

        var locator = new GatewayRoutesConfig().gatewayRoutes(
                routeLocatorBuilder,
                "http://localhost:18081",
                "http://localhost:18082",
                "http://localhost:18083"
        );

        StepVerifier.create(locator.getRoutes().collectList())
                .assertNext(routes -> {

                    assertThat(routes).hasSize(3);

                    assertThat(routes)
                            .extracting("id")
                            .containsExactlyInAnyOrder(
                                    "user-service",
                                    "event-service",
                                    "booking-service"
                            );

                    assertThat(routes)
                            .filteredOn(route ->
                                    route.getId().equals("booking-service"))
                            .singleElement()
                            .satisfies(route ->
                                    assertThat(route.getUri().toString())
                                            .isEqualTo("http://localhost:18083")
                            );
                })
                .verifyComplete();
    }
}