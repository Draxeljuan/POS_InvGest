package com.invengest.gateway.infrastructure.driver_adapters.security;

import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.model.TokenClaims;
import com.invengest.gateway.domain.model.gateway.TokenValidatorGateway;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenValidatorAdapter implements TokenValidatorGateway {

    private final String jwtSecret;
    private final String jwtIssuer;

    public JwtTokenValidatorAdapter(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.issuer:invengest-pos-auth}") String jwtIssuer) {
        this.jwtSecret = jwtSecret;
        this.jwtIssuer = jwtIssuer;
    }

    @Override
    public TokenClaims validarYExtraerClaims(String token) {
        try {
            SecretKey signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            Object userIdRaw = claims.get("userId");
            String userId = userIdRaw != null ? String.valueOf(userIdRaw) : null;

            TokenClaims tokenClaims = new TokenClaims(subject, role, userId, claims.getIssuer());
            tokenClaims.validarIdentidadCompleta();
            return tokenClaims;
        } catch (ExpiredJwtException ex) {
            throw new TokenInvalidoException("El token JWT ha expirado", ex);
        } catch (IncorrectClaimException ex) {
            throw new TokenInvalidoException("El emisor (issuer) del token JWT no es válido", ex);
        } catch (TokenInvalidoException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new TokenInvalidoException("Token JWT inválido o firma criptográfica alterada", ex);
        }
    }
}
