package com.invengest.gateway.infrastructure.entry_points.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> messages,
        String message,
        String path
) {
    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message != null ? List.of(message) : List.of(), message, path);
    }
}
