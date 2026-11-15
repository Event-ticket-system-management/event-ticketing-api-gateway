package com.eventticketing.apigatewayservice.integration;

import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRoutingIntegrationTest extends GatewayTestSupport {
    @Test void userRequestReachesUserServiceWithoutPathRewrite() throws Exception {
        respond(userBackend(), 200);
        client.post().uri("/api/v1/user/visitor/login").bodyValue("{}")
                .exchange().expectStatus().isOk();
        RecordedRequest request = userBackend().takeRequest(3, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/v1/user/visitor/login");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test void eventRequestReachesEventService() throws Exception {
        respond(eventBackend(), 200);
        client.get().uri("/api/v1/events/123").exchange().expectStatus().isOk();
        RecordedRequest request = eventBackend().takeRequest(3, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/v1/events/123");
    }

    @Test void pluralBookingPathReachesBookingService() throws Exception {
        respond(bookingBackend(), 200);
        client.get().uri("/api/v1/bookings/my-bookings")
                .headers(h -> h.setBearerAuth(valid("ROLE_USER")))
                .exchange().expectStatus().isOk();
        RecordedRequest request = bookingBackend().takeRequest(3, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/v1/bookings/my-bookings");
    }
}
