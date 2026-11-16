package com.invengest.transaction.infrastructure.entry_points.dto.response;

public record ClienteResponse(
        Integer idCliente,
        TipoDocumentoResponse tipoDocumento,
        String documentoIdentidad,
        String primerNombre,
        String segundoNombre,
        String primerApellido,
        String segundoApellido,
        String nombreCompleto,
        String telefono,
        String email
) {}
