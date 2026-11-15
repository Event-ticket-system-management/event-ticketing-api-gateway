package com.eventticketing.apigatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder,
                                      @Value("${services.user.url}") String userServiceUrl,
                                      @Value("${services.event.url}") String eventServiceUrl,
                                      @Value("${services.booking.url}") String bookingServiceUrl
    )
    {
        return builder.routes()
                .route("user-service", route -> route
                        .path("/api/v1/user/**")
                        .uri(userServiceUrl))
                .route("event-service", route -> route
                        .path("/api/v1/events/**")
                        .uri(eventServiceUrl))
                .route("booking-service", route -> route
                        .path("/api/v1/bookings/**")
                        .uri(bookingServiceUrl))
                .build();
    }

}
