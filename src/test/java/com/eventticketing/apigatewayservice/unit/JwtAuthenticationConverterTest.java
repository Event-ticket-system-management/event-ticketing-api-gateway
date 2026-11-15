package com.eventticketing.apigatewayservice.unit;

import com.eventticketing.apigatewayservice.security.JwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
class JwtAuthenticationConverterTest {
    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    private Jwt jwt(String subject, List<String> roles) {
        var builder = Jwt.withTokenValue("fake-token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        if (subject != null) builder.subject(subject);
        if (roles != null) builder.claim("roles", roles);
        return builder.build();
    }

    @Test void mapsUserRoleAndPrincipal() {
        String id = UUID.randomUUID().toString();
        var auth = converter.convert(jwt(id, List.of("ROLE_USER"))).block();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(id);
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test void mapsMultipleRoles() {
        var auth = converter.convert(jwt(UUID.randomUUID().toString(),
                List.of("ROLE_USER", "ROLE_ADMIN"))).block();
        assertThat(auth.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test void missingRolesCreatesNoAuthorities() {
        var auth = converter.convert(jwt(UUID.randomUUID().toString(), null)).block();
        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test void filtersMalformedRoles() {
        var auth = converter.convert(jwt(UUID.randomUUID().toString(),
                List.of("ROLE_USER", "ADMIN", "role_admin", "ROLE_BAD-ROLE"))).block();
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test void missingSubjectIsRejected() {
        assertThatThrownBy(() -> converter.convert(jwt(null, List.of("ROLE_USER"))).block())
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test void blankSubjectIsRejected() {
        assertThatThrownBy(() -> converter.convert(jwt(" ", List.of("ROLE_USER"))).block())
                .isInstanceOf(BadCredentialsException.class);
    }
}
