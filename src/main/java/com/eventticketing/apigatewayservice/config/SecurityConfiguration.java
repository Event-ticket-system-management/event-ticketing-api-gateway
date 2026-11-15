
package com.eventticketing.apigatewayservice.config;

import com.eventticketing.apigatewayservice.security.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;

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

                        .pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

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

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/v1/events"
                        ).hasRole("ORGANIZER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/v1/events/*"
                        ).hasRole("ORGANIZER")

                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/events/*"
                        ).hasRole("ORGANIZER")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/v1/bookings"
                        ).hasAnyRole("USER", "ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/bookings/my-bookings"
                        ).hasAnyRole("USER", "ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/bookings"
                        ).hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/bookings/*"
                        ).hasAnyRole("USER", "ADMIN")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/bookings/{id}/cancle"
                        ).hasAnyRole("USER", "ADMIN")

                        .anyExchange().denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        new JwtAuthenticationConverter()
                                )
                        )
                )

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(
                                new HttpStatusServerEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                        .accessDeniedHandler(
                                new HttpStatusServerAccessDeniedHandler(
                                        HttpStatus.FORBIDDEN
                                )
                        )
                )

                .build();
    }
}
