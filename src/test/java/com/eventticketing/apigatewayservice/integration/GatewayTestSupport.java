
package com.eventticketing.apigatewayservice.integration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
abstract class GatewayTestSupport {

    protected static final byte[] SECRET =
            "0123456789abcdef0123456789abcdef"
                    .getBytes(StandardCharsets.UTF_8);

    private static final MockWebServer USER = start();
    private static final MockWebServer EVENT = start();
    private static final MockWebServer BOOKING = start();

    @LocalServerPort
    private int port;

    protected WebTestClient client;

    private static MockWebServer start() {
        try {
            MockWebServer server = new MockWebServer();
            server.start();
            return server;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "jwt.secret",
                () -> Base64.getEncoder().encodeToString(SECRET)
        );

        registry.add(
                "services.user.url",
                () -> USER.url("/").toString()
        );

        registry.add(
                "services.event.url",
                () -> EVENT.url("/").toString()
        );

        registry.add(
                "services.booking.url",
                () -> BOOKING.url("/").toString()
        );
    }

    @BeforeEach
    void createClient() {

        drainRequests(USER);
        drainRequests(EVENT);
        drainRequests(BOOKING);

        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @AfterEach
    void verifyBackendRequests() {
        // Individual tests should assert whether routing occurred.
        // This hook is intentionally empty.
    }

    protected static String token(
            List<String> roles,
            Instant expiration
    ) {
        return signedToken(SECRET, roles, expiration);
    }

    protected static String signedToken(
            byte[] secret,
            List<String> roles,
            Instant expiration
    ) {
        try {
            var claims = new JWTClaimsSet.Builder()
                    .subject(UUID.randomUUID().toString())
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiration))
                    .claim("roles", roles)
                    .build();

            var jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            jwt.sign(new MACSigner(secret));

            return jwt.serialize();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to generate test JWT", e
            );
        }
    }

    protected static String valid(String role) {
        return token(
                List.of(role),
                Instant.now().plusSeconds(3600)
        );
    }

    protected static void respond(
            MockWebServer server,
            int statusCode
    ) {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(statusCode)
                        .setHeader(
                                "Content-Type",
                                "application/json"
                        )
                        .setBody("{\"backend\":\"ok\"}")
        );
    }

    protected static void assertNoBackendRequest(
            MockWebServer server
    ) {
        try {
            var request = server.takeRequest(
                    200,
                    TimeUnit.MILLISECONDS
            );

            assertThat(request)
                    .as("Unauthorized request must not reach backend")
                    .isNull();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    protected static MockWebServer userBackend() {
        return USER;
    }

    protected static MockWebServer eventBackend() {
        return EVENT;
    }

    protected static MockWebServer bookingBackend() {
        return BOOKING;
    }

    private static void drainRequests(MockWebServer server) {
        try {
            while (server.takeRequest(
                    1, TimeUnit.MILLISECONDS
            ) != null) {
                // Clear recorded requests.
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
