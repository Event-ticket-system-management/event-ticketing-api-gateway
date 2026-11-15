
package com.eventticketing.apigatewayservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewaySecurityIntegrationTest extends GatewayTestSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void securityFilterChainIsRegistered() {

        var chains = applicationContext
                .getBeansOfType(SecurityWebFilterChain.class);

        assertThat(chains)
                .as("SecurityWebFilterChain must be registered")
                .hasSize(1);
    }

    @Test
    void publicLoginWithoutJwtAllowed() {

        respond(userBackend(), 200);

        client.post()
                .uri("/api/v1/user/visitor/login")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void publicRegisterWithoutJwtAllowed() {

        respond(userBackend(), 201);

        client.post()
                .uri("/api/v1/user/visitor/register")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void publicEventListWithoutJwtAllowed() {

        respond(eventBackend(), 200);

        client.get()
                .uri("/api/v1/events")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void organizerCanCreateEvent() {

        respond(eventBackend(), 201);

        client.post()
                .uri("/api/v1/events")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_ORGANIZER"))
                )
                .bodyValue("{}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void userCannotCreateEvent() {

        client.post()
                .uri("/api/v1/events")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_USER"))
                )
                .bodyValue("{}")
                .exchange()
                .expectStatus().isForbidden();

        assertNoBackendRequest(eventBackend());
    }

    @Test
    void anonymousCannotCreateEvent() {

        client.post()
                .uri("/api/v1/events")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();

        assertNoBackendRequest(eventBackend());
    }

    @Test
    void organizerCanUpdateEvent() {

        respond(eventBackend(), 200);

        client.put()
                .uri("/api/v1/events/123")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_ORGANIZER"))
                )
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void userCannotDeleteEvent() {

        client.delete()
                .uri("/api/v1/events/123")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_USER"))
                )
                .exchange()
                .expectStatus().isForbidden();

        assertNoBackendRequest(eventBackend());
    }

    @Test
    void userCanCreateBooking() {

        respond(bookingBackend(), 201);

        client.post()
                .uri("/api/v1/bookings")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_USER"))
                )
                .bodyValue("{}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void userCanReadOwnBookingList() {

        respond(bookingBackend(), 200);

        client.get()
                .uri("/api/v1/bookings/my-bookings")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_USER"))
                )
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void userCannotReadAllBookings() {

        client.get()
                .uri("/api/v1/bookings")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_USER"))
                )
                .exchange()
                .expectStatus().isForbidden();

        assertNoBackendRequest(bookingBackend());
    }

    @Test
    void adminCanReadAllBookings() {

        respond(bookingBackend(), 200);

        client.get()
                .uri("/api/v1/bookings")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_ADMIN"))
                )
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void userCanCancelBooking() {

        respond(bookingBackend(), 200);

        client.patch()
                .uri("/api/v1/bookings/123/cancel")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_USER"))
                )
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void invalidSignatureReturns401() {

        byte[] wrongSecret =
                "abcdef0123456789abcdef0123456789"
                        .getBytes(StandardCharsets.UTF_8);

        String jwt = signedToken(
                wrongSecret,
                List.of("ROLE_ADMIN"),
                Instant.now().plusSeconds(3600)
        );

        client.get()
                .uri("/api/v1/bookings")
                .headers(h -> h.setBearerAuth(jwt))
                .exchange()
                .expectStatus().isUnauthorized();

        assertNoBackendRequest(bookingBackend());
    }

    @Test
    void expiredJwtReturns401() {

        String expiredJwt = token(
                List.of("ROLE_ADMIN"),
                Instant.now().minusSeconds(600)
        );

        client.get()
                .uri("/api/v1/bookings")
                .headers(h -> h.setBearerAuth(expiredJwt))
                .exchange()
                .expectStatus().isUnauthorized();

        assertNoBackendRequest(bookingBackend());
    }

    @Test
    void undefinedEndpointDenied() {

        client.get()
                .uri("/api/v1/unknown")
                .headers(h ->
                        h.setBearerAuth(valid("ROLE_ADMIN"))
                )
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void diagnoseAuthorization() {

        client.post()
                .uri("/api/v1/events")
                .headers(headers ->
                        headers.setBearerAuth(valid("ROLE_USER"))
                )
                .bodyValue("{}")
                .exchange()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(
                            "STATUS: " + result.getStatus()
                    );
                    System.out.println(
                            "BODY: " + result.getResponseBody()
                    );
                });

        System.out.println(
                "EVENT BACKEND REQUEST COUNT: "
                        + eventBackend().getRequestCount()
        );
    }
}
