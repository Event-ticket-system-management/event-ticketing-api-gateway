package com.eventticketing.apigatewayservice.unit;

import com.eventticketing.apigatewayservice.config.JwtDecoderConfiguration;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
class JwtDecoderConfigurationTest {
    private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    private static final byte[] WRONG_KEY = "abcdef0123456789abcdef0123456789".getBytes(StandardCharsets.UTF_8);
    private final JwtDecoderConfiguration config = new JwtDecoderConfiguration();

    private String sign(byte[] key, Instant expiry) throws Exception {
        var claims = new JWTClaimsSet.Builder()
                .subject(UUID.randomUUID().toString())
                .issueTime(new Date())
                .expirationTime(Date.from(expiry))
                .claim("roles", List.of("ROLE_USER"))
                .build();
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(key));
        return jwt.serialize();
    }

    @Test void validSignatureDecodes() throws Exception {
        var decoder = config.jwtDecoder(Base64.getEncoder().encodeToString(KEY));
        var decoded = decoder.decode(sign(KEY, Instant.now().plusSeconds(3600))).block();
        assertThat(decoded).isNotNull();
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly("ROLE_USER");
    }

    @Test void wrongSignatureRejected() throws Exception {
        var decoder = config.jwtDecoder(Base64.getEncoder().encodeToString(KEY));
        assertThatThrownBy(() -> decoder.decode(sign(WRONG_KEY, Instant.now().plusSeconds(3600))).block())
                .isInstanceOf(JwtException.class);
    }

    @Test void expiredTokenRejected() throws Exception {
        var decoder = config.jwtDecoder(Base64.getEncoder().encodeToString(KEY));
        assertThatThrownBy(() -> decoder.decode(sign(KEY, Instant.now().minusSeconds(600))).block())
                .isInstanceOf(JwtException.class);
    }

    @Test void shortSecretRejectedAtStartup() {
        String shortKey = Base64.getEncoder().encodeToString("short-secret".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> config.jwtDecoder(shortKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("256 bits");
    }

    @Test void invalidBase64RejectedAtStartup() {
        assertThatThrownBy(() -> config.jwtDecoder("not-base64!!!"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
