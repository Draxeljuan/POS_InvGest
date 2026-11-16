package com.invengest.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCaja {
    private LocalDate fecha;
    private BigDecimal totalVentasCompletadas;
    private Integer cantidadVentasCompletadas;
    private BigDecimal totalVentasAnuladas;
    private Integer cantidadVentasAnuladas;
    private BigDecimal efectivoEsperado;
    private List<ResumenMetodoPago> totalesPorMetodoPago = new ArrayList<>();
    private Boolean cierreYaRegistrado;
}
