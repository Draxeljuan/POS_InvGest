package com.invengest.transaction.domain.exception;

public class StockInsuficienteException extends ReglaNegocioException {
    public StockInsuficienteException(String message) {
        super(message);
    }
}
