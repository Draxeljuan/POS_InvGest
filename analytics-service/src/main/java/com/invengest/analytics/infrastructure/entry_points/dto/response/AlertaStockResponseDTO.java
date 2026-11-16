package com.invengest.analytics.infrastructure.entry_points.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStockResponseDTO {
    private Integer idAlerta;
    private String idProducto;
    private String nombreProducto;
    private Short stockActual;
    private Short stockMinimo;
    private String estado;
    private LocalDateTime fechaAlerta;
}
