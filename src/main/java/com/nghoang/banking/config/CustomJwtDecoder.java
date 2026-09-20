package com.nghoang.banking.config;

import com.nghoang.banking.dto.request.IntrospectRequest;
import com.nghoang.banking.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    private NimbusJwtDecoder nimbusJwtDecoder  = null;
    @Value("${jwt.signerKey}")
    private String signerKey;
    @Autowired
    AuthenticationService authenticationService;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var response = authenticationService.introspect(IntrospectRequest
                    .builder()
                    .token(token)
                    .build());
            if (!response.isValid()) {
                throw new JwtException("Token invalid");
            }
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Token introspect failed", e);
        }
        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(StandardCharsets.UTF_8), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
        return nimbusJwtDecoder.decode(token);
    }
}
