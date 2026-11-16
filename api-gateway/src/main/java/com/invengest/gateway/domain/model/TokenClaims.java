package com.invengest.gateway.domain.model;

import com.invengest.gateway.domain.exception.TokenInvalidoException;

public record TokenClaims(
        String subject,
        String role,
        String userId,
        String issuer
) {
    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_VENDEDOR = "Vendedor";

    public void validarIdentidadCompleta() {
        if (subject == null || subject.isBlank()) {
            throw new TokenInvalidoException("El token JWT no contiene un sujeto (sub) válido");
        }
        if (role == null || role.isBlank()) {
            throw new TokenInvalidoException("El token JWT no contiene el atributo de rol (role)");
        }
        if (userId == null || userId.isBlank() || "null".equalsIgnoreCase(userId.trim())) {
            throw new TokenInvalidoException("El token JWT no contiene el identificador de usuario (userId)");
        }
    }

    public boolean esAdministrador() {
        return ROL_ADMINISTRADOR.equalsIgnoreCase(role != null ? role.trim() : "");
    }

    public boolean esVendedor() {
        return ROL_VENDEDOR.equalsIgnoreCase(role != null ? role.trim() : "");
    }
}
