package com.invengest.gateway.domain.exception;

public class TokenInvalidoException extends ReglaNegocioException {

    public TokenInvalidoException(String message) {
        super(message);
    }

    public TokenInvalidoException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
