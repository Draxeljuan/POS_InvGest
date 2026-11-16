package com.invengest.auth.domain.exception;

public class UsuarioYaExisteException extends ReglaNegocioException {
    public UsuarioYaExisteException(String message) {
        super(message);
    }
}
