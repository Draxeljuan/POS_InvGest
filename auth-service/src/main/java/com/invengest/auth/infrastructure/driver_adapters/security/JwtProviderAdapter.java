package com.invengest.auth.infrastructure.driver_adapters.security;

import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.TokenProviderGateway;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProviderAdapter implements TokenProviderGateway {

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final String jwtIssuer;

    public JwtProviderAdapter(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms:86400000}") long jwtExpirationMs,
            @Value("${jwt.issuer:invengest-pos-auth}") String jwtIssuer) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtIssuer = jwtIssuer;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .issuer(jwtIssuer)
                .subject(usuario.getNombreUsuario())
                .claim("role", usuario.getRol().getNombre())
                .claim("userId", usuario.getIdUsuario())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}
