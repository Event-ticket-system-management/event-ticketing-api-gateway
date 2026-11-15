package com.eventticketing.apigatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtDecoderConfiguration {

    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String encodedSecret){

        byte[] keyBytes = Base64.getDecoder().decode(encodedSecret);

        if (keyBytes.length < 32){
            throw new IllegalArgumentException(
                    "JWT HS256 secret must be at least 256 bits"
            );
        }

        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

}
