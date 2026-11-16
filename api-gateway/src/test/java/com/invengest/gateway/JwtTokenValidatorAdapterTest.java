package com.invengest.gateway;

import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.model.TokenClaims;
import com.invengest.gateway.infrastructure.driver_adapters.security.JwtTokenValidatorAdapter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenValidatorAdapterTest {

    private static final String SECRET = "clave_secreta_de_pruebas_unitarias_gateway_256_bits_2026";
    private static final String OTHER_SECRET = "otra_clave_secreta_distinta_para_simular_firma_alterada_2026";
    private static final String ISSUER = "invengest-pos-auth";

    private JwtTokenValidatorAdapter validatorAdapter;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        validatorAdapter = new JwtTokenValidatorAdapter(SECRET, ISSUER);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void validarYExtraerClaims_WhenTokenIsValid_ShouldReturnTokenClaims() {
        // Arrange
        Date now = new Date();
        String token = Jwts.builder()
                .issuer(ISSUER)
                .subject("juand")
                .claim("role", "Administrador")
                .claim("userId", 10)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(signingKey)
                .compact();

        // Act
        TokenClaims claims = validatorAdapter.validarYExtraerClaims(token);

        // Assert
        assertEquals("juand", claims.subject());
        assertEquals("Administrador", claims.role());
        assertEquals("10", claims.userId());
        assertEquals(ISSUER, claims.issuer());
    }

    @Test
    void validarYExtraerClaims_WhenTokenIsExpired_ShouldThrowTokenInvalidoException() {
        // Arrange
        Date past = new Date(System.currentTimeMillis() - 120_000);
        String expiredToken = Jwts.builder()
                .issuer(ISSUER)
                .subject("juand")
                .claim("role", "Administrador")
                .claim("userId", 10)
                .issuedAt(new Date(past.getTime() - 60_000))
                .expiration(past)
                .signWith(signingKey)
                .compact();

        // Act & Assert
        TokenInvalidoException ex = assertThrows(TokenInvalidoException.class, () ->
                validatorAdapter.validarYExtraerClaims(expiredToken));
        assertTrue(ex.getMessage().contains("expirado"));
    }

    @Test
    void validarYExtraerClaims_WhenSignatureIsAltered_ShouldThrowTokenInvalidoException() {
        // Arrange
        SecretKey wrongKey = Keys.hmacShaKeyFor(OTHER_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        String forgedToken = Jwts.builder()
                .issuer(ISSUER)
                .subject("attacker")
                .claim("role", "Administrador")
                .claim("userId", 1)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(wrongKey)
                .compact();

        // Act & Assert
        TokenInvalidoException ex = assertThrows(TokenInvalidoException.class, () ->
                validatorAdapter.validarYExtraerClaims(forgedToken));
        assertTrue(ex.getMessage().contains("firma"));
    }

    @Test
    void validarYExtraerClaims_WhenIssuerIsInvalid_ShouldThrowTokenInvalidoException() {
        // Arrange
        Date now = new Date();
        String wrongIssuerToken = Jwts.builder()
                .issuer("otro-sistema-externo")
                .subject("juand")
                .claim("role", "Administrador")
                .claim("userId", 10)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(signingKey)
                .compact();

        // Act & Assert
        TokenInvalidoException ex = assertThrows(TokenInvalidoException.class, () ->
                validatorAdapter.validarYExtraerClaims(wrongIssuerToken));
        assertTrue(ex.getMessage().contains("issuer"));
    }

    @Test
    void validarYExtraerClaims_WhenClaimsIncompleteOrMalformed_ShouldThrowTokenInvalidoException() {
        // Arrange
        Date now = new Date();
        String tokenWithoutUserId = Jwts.builder()
                .issuer(ISSUER)
                .subject("juand")
                .claim("role", "Administrador")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(signingKey)
                .compact();

        // Act & Assert
        assertThrows(TokenInvalidoException.class, () ->
                validatorAdapter.validarYExtraerClaims(tokenWithoutUserId));
        assertThrows(TokenInvalidoException.class, () ->
                validatorAdapter.validarYExtraerClaims("token.malformado.invalido"));
    }
}
