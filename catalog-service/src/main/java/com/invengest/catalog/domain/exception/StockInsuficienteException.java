package com.invengest.catalog.domain.exception;

public class StockInsuficienteException extends ReglaNegocioException {
    public StockInsuficienteException(String message) {
        super(message);
    }
}
