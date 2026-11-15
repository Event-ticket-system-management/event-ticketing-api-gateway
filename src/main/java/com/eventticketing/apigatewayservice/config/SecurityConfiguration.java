package com.eventticketing.apigatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/v1/user/visitor/login",
                                "/api/v1/user/visitor/register"
                        ).permitAll()

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/events",
                                "/api/v1/events/**"
                        ).permitAll()

                        // Until JWT authentication is implemented
                        .anyExchange().denyAll()
                )

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse()
                                    .setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            exchange.getResponse()
                                    .setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )

                .build();
    }

}
