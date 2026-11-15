
package com.eventticketing.apigatewayservice.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JwtAuthenticationConverter
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "ROLE_USER",
            "ROLE_ORGANIZER",
            "ROLE_ADMIN"
    );

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {

        String userId = jwt.getSubject();

        if (userId == null || userId.isBlank()) {
            return Mono.error(new BadCredentialsException(
                    "JWT subject is missing"
            ));
        }

        try {
            UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            return Mono.error(new BadCredentialsException(
                    "JWT subject must be a valid UUID",
                    ex
            ));
        }

        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null) {
            roles = List.of();
        }

        var authorities = roles.stream()
                .filter(ALLOWED_ROLES::contains)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();

        AbstractAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        jwt,
                        authorities
                );

        return Mono.just(authentication);
    }
}
