package com.invengest.analytics.infrastructure.entry_points.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private LocalDate fechaConsulta;
    private BigDecimal ventasDelDia;
    private BigDecimal ventasAnuladasDelDia;
    private BigDecimal ventasNetasDelDia;
    private Integer operacionesDelDia;
    private BigDecimal ventasAcumuladasPeriodo;
    private List<ResumenVentaResponseDTO> historialVentasRecientes;
    private List<TopProductoResponseDTO> productosMasVendidos;
    private List<AlertaStockResponseDTO> alertasRecientes;
}
